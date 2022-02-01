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

    private Path root = Paths.get("serverDir");
    private Path currentDir;
    private static String login;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // selectStartDir(getLogin());
        sendList(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        try {
            switch (message.getType()) {
                case AUTH_REQUEST:
                    AuthRequest ar = (AuthRequest) message;
                    if (ar.getLogin().trim().equals("login") && ar.getPassword().trim().equals("password")) {
                        AuthRequest authRequest = new AuthRequest(true, ar.getLogin(), ar.getPassword(), "client");
                        login = ar.getLogin();
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
                case REFRESH_REQUEST:
                    RefreshRequest refreshRequest = (RefreshRequest) message;
                    currentDir = root.resolve(Paths.get(refreshRequest.getDir()));
                    sendList(ctx);
                    break;
            }
            System.out.println(message);
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NullPointerException e ){

        }
    }

    private Path selectStartDir(String login) {
        switch (login) {
            case "root":
                currentDir = Paths.get("serverDir");
                break;
            case "login":
                currentDir = Paths.get("serverDir/asd");
                break;
        }
        return currentDir;
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        FilesList fl = new FilesList("", currentDir);
        ctx.writeAndFlush(fl);
        System.out.println("Current Dir = " + currentDir);
    }
}
