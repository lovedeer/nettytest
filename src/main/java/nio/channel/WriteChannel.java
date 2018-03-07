package nio.channel;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class WriteChannel {
    public static void main(String[] args) throws Exception {
//        method1();
        method2();
    }

    private static void method1() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        //  获取缓冲区的视图，但与ByteBuffer的mark、position、limit互相独立
        CharBuffer charBuff = buffer.asCharBuffer();
        //  更容易进行字符操作
        charBuff.put("Hello,你好!");
        //  创建输出通道
        WritableByteChannel outChannel = Channels.newChannel(System.out);
        //  写入缓冲区数据
        outChannel.write(buffer);
        outChannel.close();
    }

    private static void method2() throws IOException {
        String content = "Hello,你好!";
        byte[] bytes = content.getBytes("GB2312");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        WritableByteChannel outChannel = Channels.newChannel(System.out);
        outChannel.write(buffer);
        outChannel.close();
    }

}
