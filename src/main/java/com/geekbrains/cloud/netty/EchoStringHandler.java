package com.geekbrains.cloud.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Slf4j
public class EchoStringHandler extends SimpleChannelInboundHandler<String> {

    private static Path currentDir;

    public EchoStringHandler() {
        currentDir = Paths.get("serverDir");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String dst = null;
        msg = msg.trim();
        log.info("received: {}", msg);

        //Содержимое директории
        if (msg.startsWith("ls")) {
            String listFilesResponse = Files.list(currentDir)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("\n\r")) + "\n\r";
            ctx.writeAndFlush(listFilesResponse);
        } else dst = msg.split(" +")[1];

        //Переход в директорию
        if (msg.startsWith("cd")) {
            if (Files.isDirectory(currentDir.resolve(dst))) {
                currentDir = currentDir.resolve(dst);
            }
        }

        //Содержимое файла
        else if (msg.startsWith("cat")) {
            String result;
            if (Files.isRegularFile(Paths.get(String.valueOf(currentDir.resolve(dst))))) {
                result = Files.lines(Paths.get(String.valueOf(currentDir.resolve(dst))), StandardCharsets.UTF_8)
                        .collect(Collectors.toList()) + "\n\r";
            } else
                result = "File Not Found\n\r";
            log.warn("File " + dst + " not found");
            ctx.writeAndFlush(result);
        }

        //Создание директории
        else if (msg.startsWith("mkdir")) {
            try {
                Files.createDirectory(Paths.get(String.valueOf(currentDir.resolve(dst))));
                if (Files.exists(Paths.get(String.valueOf(currentDir.resolve(dst))))) {
                    log.info(currentDir.resolve(dst) + " created");
                    ctx.writeAndFlush(currentDir.resolve(dst) + " created\n\r");
                }
            } catch (FileAlreadyExistsException e) {
                log.warn(currentDir.resolve(dst) + " already exists");
                ctx.writeAndFlush(currentDir.resolve(dst) + " already exists!!\n\r");
            }
        }

        //Создание файла
        else if (msg.startsWith("touch")){
            String result = null;
            try {
                Files.createFile(Paths.get(String.valueOf(currentDir.resolve(dst))));
                if (Files.exists(Paths.get(String.valueOf(currentDir.resolve(dst))))) {
                    result = "File " + currentDir.resolve(dst) + " created\n\r";
                }
            } catch (FileAlreadyExistsException e) {
                result = "File " + currentDir.resolve(dst) + " already exists\n\r";
            }
        finally {
                log.info(result);
                ctx.writeAndFlush(result);
            }
        }
        String prefix = currentDir.getFileName().toString() + "> ";
        ctx.writeAndFlush(prefix);
    }
}
