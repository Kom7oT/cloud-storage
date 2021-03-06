package com.geekbrains.cloud.netty;

import com.geekbrains.cloud.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j

public class FilesHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Path root = Paths.get("serverDir");
    private Path currentDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendList(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        try {
            log.info(String.valueOf(message.getType()));
            String login;
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
                    } else log.info("???????????????? ??????????/????????????");
                    sendList(ctx);
                    break;
                case FILE_REQUEST:
                    FileRequest fileRequest = (FileRequest) message;
                    String fileName = fileRequest.getFileName();
                    //?????????????????? ?????????????????? ???? ??????????
                    byte[] buffer = new byte[32 * 1024];
                    try (InputStream is = new FileInputStream(currentDir.resolve(fileName).toFile())) {
                        while (is.available() > 0) {
                            int cnt = is.read(buffer);
                            if (cnt < 32 * 1024) {
                                byte[] tmp = new byte[cnt];
                                if (cnt >= 0) System.arraycopy(buffer, 0, tmp, 0, cnt);
                                ctx.writeAndFlush(new FileMessage(fileName, tmp.clone()));
                            } else {
                                ctx.writeAndFlush(new FileMessage(fileName, buffer.clone()));
                            }
                        }
                    }
                    break;
                case FILE_MESSAGE:
                    FileMessage fileMessage = (FileMessage) message;
                    Path cur = currentDir.resolve(fileMessage.getFileName());
                    if (!Files.exists(cur)) {
                        Files.createFile(cur);
                    }
                    Files.write(currentDir.resolve(fileMessage.getFileName()),
                            fileMessage.getBytes(),
                            StandardOpenOption.APPEND
                    );
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
                case DELETE_REQUEST:
                    DeleteRequest deleteRequest = (DeleteRequest) message;
                    Files.delete(currentDir.resolve(deleteRequest.getFilename()));
                    sendList(ctx);
                    break;
                case MKDIR_REQUEST:
                    MkDirRequest mkDirRequest = (MkDirRequest) message;
                    if (!Files.exists(currentDir.resolve(mkDirRequest.getDirName()))) {
                        Files.createDirectory(currentDir.resolve(mkDirRequest.getDirName()));
                        sendList(ctx);
                    } else log.info("Dir already exists!");
                    break;
                case RENAME_REQUEST:
                    RenameRequest renameRequest = (RenameRequest) message;
                    if (!Files.exists(currentDir.resolve(renameRequest.getTargetFileName()))) {
                        Files.move(currentDir.resolve(renameRequest.getFileName()), currentDir.resolve(renameRequest.getTargetFileName()));
                    } else log.info("File already exists!");
                    sendList(ctx);
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
