package com.geekbrains.cloud.server;

import com.geekbrains.cloud.utils.SenderUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.file.Path;

public class FileProcessorHandler implements Runnable {

    private File currentDir;
    private static final int SIZE = 256;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;

    public FileProcessorHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[SIZE];
        currentDir = new File("serverDir");
        SenderUtils.sendFilesListToOutputStream(os, currentDir);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Received: " + command);
                if (command.equals("#SEND#FILE")) {
                    SenderUtils.getFileFromInputStream(is, currentDir);
                    //server state updated
                    SenderUtils.sendFilesListToOutputStream(os, currentDir);
                }
                if (command.equals("#GET#FILE")) {
                    String fileName = is.readUTF();
                    File file = currentDir.toPath().resolve(fileName).toFile();
                    SenderUtils.loadFileToOutputStream(os, file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
