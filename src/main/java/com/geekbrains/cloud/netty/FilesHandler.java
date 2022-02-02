package com.geekbrains.cloud.netty;

import com.geekbrains.cloud.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j

public class FilesHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Path root = Paths.get("serverDir");
    private Path currentDir;
    private static String login;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendList(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        try {
            log.info(String.valueOf(message.getType()));
            switch (message.getType()) {
                case AUTH_REQUEST:
                    AuthRequest ar = (AuthRequest) message;
                    if (ar.getLogin().trim().equals("login") && ar.getPassword().trim().equals("password")) {
                        AuthRequest authRequest = new AuthRequest(true, ar.getLogin(), ar.getPassword(), "client");
                        login = ar.getLogin();
                        currentDir = root.resolve(login);
                        ctx.writeAndFlush(authRequest);
                        log.info("user " + ar.getLogin() + " connected");
                    } else if (ar.getLogin().trim().equals("root") && ar.getPassword().trim().equals("root")) {
                        AuthRequest authRequest = new AuthRequest(true, ar.getLogin(), ar.getPassword(), "client");
                        login = ar.getLogin();
                        currentDir = root.resolve(login);
                        ctx.writeAndFlush(authRequest);
                        sendList(ctx);
                        log.info("user " + ar.getLogin() + " connected");
                    } else log.info("Неверные логин/пароль");
                    sendList(ctx);
                    break;
                case FILE_REQUEST:
                    FileRequest fileRequest = (FileRequest) message;
                    ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getFileName())));
                    break;
                case FILE_MESSAGE:
                    FileMessage fileMessage = (FileMessage) message;
                    Files.write(currentDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    sendList(ctx);
                    break;
                case REFRESH_REQUEST:
                    RefreshRequest refreshRequest = (RefreshRequest) message;

                    if (Files.isDirectory(Paths.get(refreshRequest.getDir()))) {
                        currentDir = Paths.get(refreshRequest.getDir());
                        sendList(ctx);
                    }
                  ctx.writeAndFlush(refreshRequest);
                    break;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        if (currentDir != null && Files.isDirectory(currentDir)) {
            FilesList fl = new FilesList(currentDir);
            ctx.writeAndFlush(fl);
        }
    }
}
