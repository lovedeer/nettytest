package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public class NioClient {
    private final int port;
    private final String host;

    public NioClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    private void start() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Selector selector =null;
        SocketChannel sc = null;
        try {
            sc = SocketChannel.open();
            selector = Selector.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(host, port));
            sc.register(selector, SelectionKey.OP_READ);
            if (sc.finishConnect()) {
                int count = 0;
                while (true) {
                    TimeUnit.SECONDS.sleep(3);
                    String info = "Client " + count++ + " information";
                    buffer.clear();
                    buffer.put(info.getBytes());
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        sc.write(buffer);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sc != null) {
                    sc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 7878;
        String host = "127.0.0.1";
        new NioClient(port, host).start();
    }
}
