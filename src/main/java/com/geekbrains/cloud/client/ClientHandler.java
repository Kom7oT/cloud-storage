package com.geekbrains.cloud.client;

import com.geekbrains.cloud.model.AbstractMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Callback callback;

    public ClientHandler(Callback callback) {
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        callback.onMessageReceived(message);
    }
}