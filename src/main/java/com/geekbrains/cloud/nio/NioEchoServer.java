package com.geekbrains.cloud.nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioEchoServer {

    /**
     * Сделать терминал, которые умеет обрабатывать команды:
     * ls - список файлов в директории
     * cd dir_name - переместиться в директорию
     * cat file_name - распечатать содержание файла на экран
     * mkdir dir_name - создать директорию в текущей
     * touch file_name - создать пустой файл в текущей директории
     * */

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer buf;
    private final String rootPath = "serverDir";

    public NioEchoServer() throws IOException {
        buf = ByteBuffer.allocate(1024);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started...");
        while (serverChannel.isOpen()) {
            selector.select(); // block
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buf);
        if (read == -1) {
            System.out.println(-1);
            channel.close();
            return;
        }
        if (read == 0) {
            System.out.println(0);
            return;
        }
        buf.flip();
        byte[] buffer = new byte[read];
        int pos = 0;
        while (buf.hasRemaining()) {
            buffer[pos++] = buf.get();
        }
        buf.clear();
        String command = new String(buffer, StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "");
        if (command.equals("--help")) {
            channel.write(ByteBuffer.wrap("ls - show file list\n\r".getBytes()));
            System.out.println("Received command: " + command);
        }
        if (command.equals("ls")) {
            channel.write(ByteBuffer.wrap(getFilesList().getBytes()));
            channel.write(ByteBuffer.wrap("\n\r".getBytes()));
            System.out.println("Received command: " + command);
        }
    }

    private String getFilesList() {
        return String.join(" ", new File(rootPath).list());
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Hello user! Welcome to terminal\n\r".getBytes()));
        System.out.println("Client accepted...");
    }

    public static void main(String[] args) throws IOException {
        new NioEchoServer();
    }
}