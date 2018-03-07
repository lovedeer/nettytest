package aio;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AioServer {
    private final  int port;

    public AioServer(int prot) {
        this.port = prot;
    }

    public void start() {
        AsynchronousServerSocketChannel assc = null;
        try {
            assc = AsynchronousServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (assc != null)
                    assc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
