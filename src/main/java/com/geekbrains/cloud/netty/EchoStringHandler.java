package com.geekbrains.cloud.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String dst = null;
        log.info("received: {}", s);
        if (s.startsWith("ls")) {
            ctx.writeAndFlush("from server " + s + "\n\r");
            String listFilesResponse = Files.list(currentDir)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("\n\r")) + "\n\r";
            ctx.writeAndFlush(listFilesResponse);
        }
    }
}
