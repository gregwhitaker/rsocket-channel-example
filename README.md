# rsocket-channel-example
![Build](https://github.com/gregwhitaker/rsocket-channel-example/workflows/Build/badge.svg)

An example of sending bi-directional messages between applications with the channel interaction model in [RSocket](http://rsocket.io).

In this example the `number-client` will stream `50` integers to the `number-service`. The service will count the even integers
it receives and stream back the total count of even numbers received at a one-second interval to the client. All communication is
taking place over a single RSocket channel.

## Building the Example
Run the following command to build the example:

    ./gradlew clean build
    
## Running the Example
Follow the steps below to run the example:

1. Run the following command to start the `number-service`:

        ./gradlew :number-service:run
        
    If the service has started successfully you will see the following in the terminal:
    
        > Task :number-service:run
        [main] INFO example.number.service.NumberService - RSocket server started on port: 7000
        
2. In a new terminal, run the following command to start streaming integers with the `number-client`:

        ./gradlew :number-client:run
        
    If successful, you will see integers being streamed out to the service and the total count of even numbers being streamed
    back in the terminal:
    
        [reactor-tcp-nio-1] INFO example.number.client.NumberClient - Total Even Number Count: 26
        [parallel-8] INFO example.number.client.NumberClient - Sending: 9
        [parallel-1] INFO example.number.client.NumberClient - Sending: 4
        [reactor-tcp-nio-1] INFO example.number.client.NumberClient - Total Even Number Count: 27
        [parallel-2] INFO example.number.client.NumberClient - Sending: 3
        [parallel-3] INFO example.number.client.NumberClient - Sending: 7
        [parallel-3] INFO example.number.client.NumberClient - Done
    
    Once the client has streamed `50` integers it will complete.

## Bugs and Feedback
For bugs, questions, and discussions please use the [Github Issues](https://github.com/gregwhitaker/rsocket-channel-example/issues).

## License
MIT License

Copyright (c) 2020 Greg Whitaker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
