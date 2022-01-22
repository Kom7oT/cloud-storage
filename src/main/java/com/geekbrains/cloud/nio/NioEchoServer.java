package com.geekbrains.cloud.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioEchoServer {

    /**
     * Сделать терминал, которые умеет обрабатывать команды:
     * ls - список файлов в директории
     * cd dir_name - переместиться в директорию
     * cat file_name - распечатать содержание файла на экран
     * mkdir dir_name - создать директорию в текущей
     * touch file_name - создать пустой файл в текущей директории
     */

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final ByteBuffer buf;
    private final String rootPath = "serverDir";
    private String currentDir = rootPath;
    private final ClientProcessor clientProcessor;

    public void start() throws IOException {
        log.info("Server started...");
        try {
            while (serverChannel.isOpen()) {
                selector.select(); // block
                log.info("Keys selected...");
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
        } catch (Throwable ex) {
            clientProcessor.onExceptionCaught(ex);
        }
    }

    public NioEchoServer(ClientProcessor clientProcessor) throws IOException {
        this.clientProcessor = clientProcessor;
        buf = ByteBuffer.allocate(1024);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

    }

    private void handleRead(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        int read;
        while (true) {
            read = channel.read(buf);
            if (read == 0) {
                break;
            }
            if (read < 0) {
                clientProcessor.onClientDisconnected(channel);
                channel.close();
                return;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }
        clientProcessor.onMessageReceived(s.toString(), channel);
    }


    public void processMessage(SocketChannel channel, String message) {
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        clientProcessor.onClientAccepted(channel);
    }

    public static void main(String[] args) throws IOException {
    }
}