package com.geekbrains.cloud.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j

public class ServerTest {

    public static void main(String[] args) throws IOException {
        NioEchoServer server = new NioEchoServer(new TelnetTerminalProcessor());
        server.start();
    }
}
