package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServer {
    private final int port;
    private final String host;
    private static final int TIMEOUT = 3000;

    public NioServer(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public static void main(String[] args) throws IOException {
        int port = 7878;
        String host = "127.0.0.1";
        new NioServer(port, host).start();
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void handleWrite(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        SocketChannel sc = (SocketChannel) key.channel();
        buffer.put("hello".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            sc.write(buffer);
        }
        buffer.compact();
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        SocketChannel sc = (SocketChannel) key.channel();
        int size = sc.read(buffer);
        while (size > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }
            System.out.println();
            buffer.clear();
            size = sc.read(buffer);
        }
        if (size == -1) {
            sc.close();
        }
    }

    private void start() {
        ServerSocketChannel ssc = null;
        Selector selector = null;
        try {
            ssc = ServerSocketChannel.open();
            selector = Selector.open();
            ssc.bind(new InetSocketAddress(host, port));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                if (selector.select(TIMEOUT) == 0) {
                    System.out.println("......");
                    continue;
                }
                for (SelectionKey selectionKey : selector.selectedKeys()) {
                    if (selectionKey.isAcceptable()) {
                        handleAccept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        handleRead(selectionKey);
                    } else if (selectionKey.isWritable() && selectionKey.isValid()) {
                        handleWrite(selectionKey);
                    } else if (selectionKey.isConnectable()) {
                        System.out.println("Server is connectable!");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ssc != null) {
                    ssc.close();
                }
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

