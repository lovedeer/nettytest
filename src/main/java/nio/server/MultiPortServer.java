package nio.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class MultiPortServer {
    private final int[] ports;
    private ByteBuffer buf = ByteBuffer.allocate(1024);

    public MultiPortServer(int[] ports) {
        this.ports = ports;
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        buf.clear();
        int size = sc.read(buf);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) sc.getLocalAddress();
        int port = inetSocketAddress.getPort();
        String host = inetSocketAddress.getHostString();
        System.out.print(host + " " + port + " : ");
        while (size > 0) {
            buf.flip();
            while (buf.hasRemaining())
                System.out.print((char) buf.get());
            System.out.println();
            buf.clear();
            size = sc.read(buf);
        }
        if (size == -1)
            sc.close();

    }

    public void start() throws IOException {
        Selector selector = null;
        ServerSocketChannel[] ssc = new ServerSocketChannel[ports.length];
        try {
            selector = Selector.open();
            for (int i = 0; i < ports.length; ++i) {
                ssc[i] = ServerSocketChannel.open();
                ssc[i].configureBlocking(false);
                ssc[i].bind(new InetSocketAddress(ports[i]));
                ssc[i].register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("going to listen on port " + ports[i]);
            }

            while (true) {
                if (selector.select() == 0)
                    continue;
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (selector != null)
                    selector.close();
                for (ServerSocketChannel serverSocketChannel : ssc) {
                    if (serverSocketChannel != null)
                        serverSocketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int[] ports = new int[]{7077, 7078};
        new MultiPortServer(ports).start();
    }
}
