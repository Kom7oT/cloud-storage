package com.geekbrains.cloud.client;

import com.geekbrains.cloud.model.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream os;
    private static ObjectDecoderInputStream is;

    public static void start() {
        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMsg(AbstractMessage msg) {
        try {
            os.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = is.readObject();
        return (AbstractMessage) obj;
    }
}