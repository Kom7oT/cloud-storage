package com.geekbrains.cloud.client;

import com.geekbrains.cloud.model.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

@Slf4j

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream os;
    private static ObjectDecoderInputStream is;

    public static void start() {
        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (ConnectException e) {
            log.error("Сервер недоступен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    public static void sendMsg(AbstractMessage msg){
        try {
            os.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){

        }
    }

    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        try {
            Object obj = is.readObject();
            return (AbstractMessage) obj;
        } catch (NullPointerException e) {
    return null;
        }
    }
}