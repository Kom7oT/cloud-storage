package com.geekbrains.cloud.utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class SenderUtils {

    private static final int SIZE = 256;

    public static void getFileFromInputStream(DataInputStream is, File currentDir) throws IOException {
        String fileName = is.readUTF();
        byte[] buf = new byte[SIZE];
        long size = is.readLong();
        System.out.println("Created file: " + fileName);
        System.out.println("File size: " + size);
        Path currentPath = currentDir.toPath().resolve(fileName);
        try (FileOutputStream fos = new FileOutputStream(currentPath.toFile())) {
            for (int i = 0; i < (size + SIZE - 1) / SIZE; i++) {
                int read = is.read(buf);
                fos.write(buf, 0, read);
            }
        }
    }

    public static void loadFileToOutputStream(DataOutputStream os, File file) throws IOException {
        byte[] buf = new byte[SIZE];
        os.writeUTF("#SEND#FILE");
        os.writeUTF(file.getName());
        os.writeLong(file.length());
        try (FileInputStream is = new FileInputStream(file)) {
            while (true) {
                int read = is.read(buf);
                if (read == -1) {
                    break;
                }
                os.write(buf, 0, read);
            }
        }
        os.flush();
    }

    public static void sendFilesListToOutputStream(DataOutputStream os, File currentDir) throws IOException {
        String[] files = currentDir.list();
        if (files != null) {
            os.writeUTF("#LIST");
            os.writeInt(files.length);
            for (String file : files) {
                os.writeUTF(file);
            }
        }
    }

}
