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
import java.nio.file.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

        try {


            if (command.equals("--help")) {
                channel.write(ByteBuffer.wrap("---------------------------\n\r".getBytes()));
                channel.write(ByteBuffer.wrap("*ls              - show file list\n\r".getBytes()));
                channel.write(ByteBuffer.wrap("*cd dir_name     - move to directory\n\r".getBytes()));
                channel.write(ByteBuffer.wrap("*cat file_name   - open file\n\r".getBytes()));
                channel.write(ByteBuffer.wrap("*mkdir dir_name  - create directory\n\r".getBytes()));
                channel.write(ByteBuffer.wrap("*touch file_name - create empty file in this dir\n\r".getBytes()));
                channel.write(ByteBuffer.wrap("---------------------------\n\r".getBytes()));
                System.out.println("Received command: " + command);
            }
            //Содрежимое каталога
            if (command.equals("ls")) {
                channel.write(ByteBuffer.wrap(("---------------------------\n\r" + getFilesList(currentDir) + "\n\r---------------------------\n\r").getBytes()));
                System.out.println("Received command: " + command);
            }
            //Отделение второго слова команды
            String[] words = command.split(" ");
            String secondWord = words[1];

            //Переместиться в директорию
            if (command.equals("cd " + secondWord) && getDirList().contains(secondWord)) {
                currentDir = secondWord;
                channel.write(ByteBuffer.wrap(("---------------------------\n\r" + getFilesList(currentDir) + "\n\r---------------------------\n\r").getBytes()));
                System.out.println("Received command: " + command);
            }

            //Распечатать содержимое файла
            if (command.equals("cat " + secondWord)) {
                channel.write(ByteBuffer.wrap(("---------------------------\n\r" + (fileContent(currentDir, secondWord) + "\n\r---------------------------\n\r")).getBytes(StandardCharsets.UTF_8)));
                System.out.println("Received command: " + command);
            }

            //Создание директории
            if (command.equals("mkdir " + secondWord)) {
                System.out.println("Received command: " + command);
                System.out.println(createDir(currentDir, secondWord));
            }

            //Создание файла
            if (command.equals("touch " + secondWord)) {
                System.out.println("Received command: " + command);
                System.out.println(createFile(currentDir, secondWord));
            }

        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    private String getFilesList(String dirPath) {
        String result = "";
        try {
            result = String.join("\n\r", Objects.requireNonNull(new File(dirPath).list()));
        } catch (NullPointerException e) {

        }
        return result;
    }

    private String getDirList() throws IOException {
        return (String.valueOf(Files.walk(Paths.get(rootPath), 1)
                .filter(Files::isDirectory)
                .collect(Collectors.toList())));
    }

    private String fileContent(String dirPath, String fileName) throws IOException {
        String result;
        if (Files.isRegularFile(Paths.get(dirPath + "\\" + fileName))) {
            result = String.valueOf(Files.lines(Paths.get(dirPath + "\\" + fileName), StandardCharsets.UTF_8)
                    .collect(Collectors.toList()));
        } else result = "File not found";
        return result;
    }

    private String createDir(String dirPath, String dirName) throws IOException {
        String result = "";
        try {
            Files.createDirectory(Paths.get(dirPath + "\\" + dirName));
            if (Files.exists(Paths.get(dirPath + "\\" + dirName))) {
                result = "Directory " + dirPath + "\\" + dirName + " created";
            }
        } catch (FileAlreadyExistsException e) {
            result = "Directory already exists";
        }
        return result;
    }

    public static String createFile(String dirPath, String fileName) throws IOException {
        String result = "";
        try {
            Files.createFile(Paths.get(dirPath + "\\" + fileName));
            if (Files.exists(Paths.get(dirPath + "\\" + fileName))) {
                result = "File " + dirPath + "\\" + fileName + " created";
            }
        } catch (FileAlreadyExistsException e) {
            result = "File " + dirPath + "\\" + fileName + " already exists";
        }
        return result;
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