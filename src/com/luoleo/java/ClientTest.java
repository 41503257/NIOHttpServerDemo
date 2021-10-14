package com.luoleo.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientTest {
    public static int buffersize=1024;
    public static void main(String[] args) {
        SocketChannel socketChannel=null;
        try {
            socketChannel=SocketChannel.open();
            socketChannel.configureBlocking(false);
            InetSocketAddress addr=new InetSocketAddress(80);
            socketChannel.connect(addr);
            while(!socketChannel.finishConnect());
            ByteBuffer outputbuffer=ByteBuffer.allocate(buffersize);
            outputbuffer.put("hello".getBytes());
            outputbuffer.flip();
            while (outputbuffer.hasRemaining()){
                System.out.println(outputbuffer);
                //将buffer信息写入通道
                socketChannel.write(outputbuffer);
            }
            Thread.sleep(1000);
            ByteBuffer inputbuffer=ByteBuffer.allocate(buffersize);
            while (true) {
                inputbuffer.clear();
                int r;
                r=socketChannel.read(inputbuffer);
                if(r<=0){
                    break;
                } else if (r == buffersize) {
                    String input = new String(inputbuffer.array());
                    System.out.print(input);
                }else {
                    String input = new String(inputbuffer.array());
                    for(int i=0;i<r;i++)System.out.print(input.charAt(i));
                }
            }
            outputbuffer.clear();
            outputbuffer.put("hello".getBytes());
            outputbuffer.flip();
            while (outputbuffer.hasRemaining()){
                System.out.println(outputbuffer);
                //将buffer信息写入通道
                socketChannel.write(outputbuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(socketChannel!=null)try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
