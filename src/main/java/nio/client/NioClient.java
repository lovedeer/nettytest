package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class NioClient {
    private final int port;
    private final String host;

    public NioClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    private void start() throws Exception {
        Selector selector;
        SocketChannel sc = null;
        try {
            sc = SocketChannel.open();
            selector = Selector.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(host, port));
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            sc.finishConnect();
            while (true) {
                if (selector.select() == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        handleRead(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        handleWrite(selectionKey);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
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

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel sc = (SocketChannel) key.channel();
        int size = sc.read(buffer);
        while (size > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }
            buffer.clear();
            System.out.println();
            size = sc.read(buffer);
        }
        if (size == -1) {
            sc.close();
        }
    }

    private void handleWrite(SelectionKey key) throws Exception {
        TimeUnit.SECONDS.sleep(3);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel sc = (SocketChannel) key.channel();
        String info = "Client : 你好!";
        buffer.put(info.getBytes("GBK"));
        buffer.flip();
        while (buffer.hasRemaining()) {
            sc.write(buffer);
        }
        buffer.compact();
    }

    public static void main(String[] args) throws Exception {
        int port = 7878;
        String host = "127.0.0.1";
        new NioClient(port, host).start();
    }
}
