package example.number.client;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class NumberClient {
    private static final Logger LOG = LoggerFactory.getLogger(NumberClient.class);

    public static void main(String... args) throws Exception {
        RSocket rSocket = RSocketFactory.connect()
                .transport(TcpClientTransport.create(7000))
                .start()
                .block();

        Random rand = new Random(System.currentTimeMillis());

        CountDownLatch latch = new CountDownLatch(1);

        Flux<Payload> intPayloads = Flux.range(1, 50)
                .delayElements(Duration.ofMillis(500))
                .map(cnt -> rand.nextInt(11))
                .map(i -> {
                    LOG.info("Sending: {}", i);
                    return DefaultPayload.create(BigInteger.valueOf(i).toByteArray());
                })
                .doOnComplete(() -> {
                    LOG.info("Done");
                    latch.countDown();
                });

        rSocket.requestChannel(intPayloads)
                .subscribe(payload -> {
                    byte[] bytes = new byte[payload.data().readableBytes()];
                    payload.data().readBytes(bytes);

                    long total = new BigInteger(bytes).longValue();

                    LOG.info("Total Even Number Count: {}", total);
                });

        latch.await();
    }
}
