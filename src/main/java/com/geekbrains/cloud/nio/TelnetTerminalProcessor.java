package com.geekbrains.cloud.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Slf4j
public class TelnetTerminalProcessor implements ClientProcessor {

    private static Path currentDir;

    public TelnetTerminalProcessor() {
        currentDir = Paths.get("serverDir");
    }

    @Override
    public void onMessageReceived(String msg, SocketChannel channel) throws IOException {
        log.info("received: {}", msg);
        msg = msg.trim();
        String dst = null;
        //Содержимое директории
        if (msg.equals("ls")) {
            String listFilesResponse = Files.list(currentDir)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("\n\r")) + "\n\r";
            channel.write(ByteBuffer.wrap(
                    listFilesResponse.getBytes(StandardCharsets.UTF_8)
                    )
            );
        } else
            dst = msg.split(" +")[1];//Второе слово

        //Переход в директорию
        if (msg.startsWith("cd")) {
            if (Files.isDirectory(currentDir.resolve(dst))) {
                currentDir = currentDir.resolve(dst);
            }

        }
        //Содержимое файла
        else if (msg.startsWith("cat")) {
            if (Files.isRegularFile(Paths.get(String.valueOf(currentDir.resolve(dst))))) {
                String result = Files.lines(Paths.get(String.valueOf(currentDir.resolve(dst))), StandardCharsets.UTF_8)
                        .collect(Collectors.toList()) + "\n\r";
                channel.write(ByteBuffer.wrap(
                        result.getBytes(StandardCharsets.UTF_8)));
            } else channel.write(ByteBuffer.wrap("File Not Found\n\r".getBytes(StandardCharsets.UTF_8)));
        }

        //Создание директории
        else if (msg.startsWith("mkdir")) {
            try {
                Files.createDirectory(Paths.get(String.valueOf(currentDir.resolve(dst))));
                if (Files.exists(Paths.get(String.valueOf(currentDir.resolve(dst))))) {
                    log.info(currentDir.resolve(dst) + " created");
                    channel.write(ByteBuffer.wrap((currentDir.resolve(dst) + " created\n\r").getBytes(StandardCharsets.UTF_8)));
                }
            } catch (FileAlreadyExistsException e) {
                log.error(currentDir.resolve(dst) + " already exists");
                channel.write(ByteBuffer.wrap((currentDir.resolve(dst) + " already exists!!\n\r").getBytes(StandardCharsets.UTF_8)));
            }
        }

        //Создание файла
        else if (msg.startsWith("touch")){
            String result;
            try {
                Files.createFile(Paths.get(String.valueOf(currentDir.resolve(dst))));
                if (Files.exists(Paths.get(String.valueOf(currentDir.resolve(dst))))) {
                     result = "File " + currentDir.resolve(dst) + " created\n\r";
                     log.info(result);
                    channel.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
                }
            } catch (FileAlreadyExistsException e) {
                result = "File " + currentDir.resolve(dst) + " already exists\n\r";
                log.info(result);
                channel.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
            }
        }
            String prefix = currentDir.getFileName().toString() + "> ";
            channel.write(ByteBuffer.wrap(prefix.getBytes(StandardCharsets.UTF_8)));

    }

    @Override
    public void onClientDisconnected(SocketChannel channel) throws IOException {
        log.info("Client disconnected...");
    }

    @Override
    public void onClientAccepted(SocketChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(
                "Hello user. Welcome to our terminal\n\r".getBytes(StandardCharsets.UTF_8)
        ));
        log.info("Client accepted...");
        String prefix = currentDir.getFileName().toString() + "> ";
        channel.write(ByteBuffer.wrap(prefix.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void onExceptionCaught(Throwable ex) throws IOException {
        log.error("error: ", ex);
    }
}