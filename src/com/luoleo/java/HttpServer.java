package com.luoleo.java;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class HttpServer {
    private static int input_buffersize=3;
    private static ByteBuffer getbuffer(){
        ByteBuffer obuffer=ByteBuffer.allocate(10240);
        obuffer.put("HTTP/1.1 200 OK\r\n".getBytes());
        obuffer.put("Content-Type: text/html;charset=UTF-8\r\n".getBytes());
        obuffer.put("Content-Length:40\r\n".getBytes());
        obuffer.put("Server:luoserver/1.1\r\n".getBytes());
        obuffer.put(("Date:"+new Date()+"\r\n").getBytes());
        obuffer.put("\r\n".getBytes());
        obuffer.put("<h1>hello!</h1>\r\n".getBytes());
        obuffer.put("<h3>HTTP服务器!</h3>\r\n".getBytes());
        obuffer.flip();
        return obuffer;
    }
    public static void server_run() throws IOException{
        //选择器
        Selector selector=Selector.open();
        //绑定端口，非阻塞
        ServerSocketChannel ssc=ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(80));
        //将选择器注册监听ACCEPT事件
        //register注册：不支持阻塞注册，注册前需要设定非阻塞
        SelectionKey selectionKey=ssc.register(selector,ssc.validOps(),null);
        //监听端口
        while(true){
            //阻塞直到有访问
            System.out.println("------连接中-----");
            int noOfKeys=selector.select();
            Set selectedKeys= selector.selectedKeys();
            Iterator itr=selectedKeys.iterator();
            System.out.println("------有"+noOfKeys+"连接进入-----");
            //遍历访问
            //监听事件：OP_READ、OP_WRITE、OP_CONNECT、OP_ACCEPT
            while(itr.hasNext()){
                SelectionKey key=(SelectionKey) itr.next();
                if(key.channel().isOpen()&&key.isAcceptable()){
                    handleAccept(key);
                }
                if(key.channel().isOpen()&&key.isReadable()) {
                    handleRead(key);
                }
                if (key.channel().isOpen()&&key.isWritable()) {
                    handleWrite(key);
                }
                itr.remove();
            }
        }
    }
    public static void handleAccept(SelectionKey key)throws IOException {
        ServerSocketChannel ssc=(ServerSocketChannel)key.channel();
        SocketChannel client=ssc.accept();
        client.configureBlocking(false);
        client.register(key.selector(),SelectionKey.OP_READ);
        System.out.println("-----连接已经接收------");
    }
    public static void handleRead(SelectionKey key)throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(input_buffersize);
        //将socket读出
        while (true) {
            buffer.clear();
            int r;
            r=client.read(buffer);
            if(r<0){
                client.close();
                key.cancel();
                break;
            }else if(r==0){
                break;
            } else if (r == input_buffersize) {
                String input = new String(buffer.array());
                System.out.print(input);
            }else {
                String input = new String(buffer.array());
                for(int i=0;i<r;i++)System.out.print(input.charAt(i));
            }
        }
        System.out.println("-------读取完毕-------");
        //注册写事件
        if(client.isOpen())client.register(key.selector(), SelectionKey.OP_WRITE);
    }
    public static void handleWrite(SelectionKey key)throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        boolean flag_finish_write=true;
        ByteBuffer obuffer=getbuffer();
        while(obuffer.hasRemaining()){
            int len=client.write(obuffer);
            if(len<0){
                throw new EOFException();
            }else if(len==0){
                flag_finish_write = false ;
                break;
            }
        }
        if(flag_finish_write){
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            System.out.println("--------向连接传递消息完毕-----");
            client.close();
            System.out.println("--------连接已关闭-----------");
        }
    }
    public static void main(String[] args)  {
        try {
            server_run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}