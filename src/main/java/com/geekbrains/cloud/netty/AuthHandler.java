package com.geekbrains.cloud.netty;

import com.geekbrains.cloud.model.AbstractMessage;
import com.geekbrains.cloud.model.AuthRequest;
import com.geekbrains.cloud.model.CommandType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class AuthHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New unauthorized client connected...");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        if (message.getType() == CommandType.AUTH_REQUEST) {

            AuthRequest ar = (AuthRequest) message;
            log.info("user " + ar.getLogin() + " connected");
            if (ar.getLogin().equals("login") && ar.getPassword().equals("password")) {
                AuthRequest authRequest = new AuthRequest(true, ar.getLogin(), ar.getPassword(), "client");
                ctx.writeAndFlush(authRequest);
            } else log.info("Неверные логин/пароль");
        }
    }
}