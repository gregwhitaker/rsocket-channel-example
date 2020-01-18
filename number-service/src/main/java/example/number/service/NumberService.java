package example.number.service;

import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service that receives a stream of integers, counts the even numbers, and streams back the total count
 * of even numbers received at a one second interval.
 */
public class NumberService {
    private static final Logger LOG = LoggerFactory.getLogger(NumberService.class);

    public static void main(String... args) throws Exception {
        RSocketFactory.receive()
                .frameDecoder(PayloadDecoder.DEFAULT)
                .acceptor(new SocketAcceptor() {
                    @Override
                    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
                        return Mono.just(new AbstractRSocket() {
                            final AtomicLong evenCnt = new AtomicLong(0);

                            @Override
                            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                                Flux<Payload> totalFlux = Flux.interval(Duration.ofSeconds(1))
                                        .map(i -> DefaultPayload.create(BigInteger.valueOf(evenCnt.get()).toByteArray()));

                                // Subscribe to the incoming publisher to receive data from number-client
                                payloads.subscribe(new Subscriber<Payload>() {
                                    @Override
                                    public void onSubscribe(Subscription s) {
                                        // No backpressure
                                        s.request(Long.MAX_VALUE);
                                    }

                                    @Override
                                    public void onNext(Payload payload) {
                                        byte[] bytes = new byte[payload.data().readableBytes()];
                                        payload.data().readBytes(bytes);

                                        int num = new BigInteger(bytes).intValue();

                                        LOG.info("Received: {}", num);

                                        if (num % 2 == 0) {
                                            evenCnt.incrementAndGet();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        LOG.error(t.getMessage(), t);
                                    }

                                    @Override
                                    public void onComplete() {
                                        LOG.info("Done");
                                    }
                                });

                                // Return this flux that will emit the odd and even tallies at a fixed interval
                                return Flux.interval(Duration.ofSeconds(1))
                                        .map(i -> {
                                            final long total = evenCnt.get();

                                            LOG.info("Sending Total: {}", total);

                                            return DefaultPayload.create(BigInteger.valueOf(total).toByteArray());
                                        });
                            }
                        });
                    }
                })
                .transport(TcpServerTransport.create(7000))
                .start()
                .block();

        LOG.info("RSocket server started on port: 7000");

        Thread.currentThread().join();
    }
}
