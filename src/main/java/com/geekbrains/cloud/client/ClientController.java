package com.geekbrains.cloud.client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import com.geekbrains.cloud.utils.SenderUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ClientController implements Initializable {

    private static final int SIZE = 256;

    public ListView<String> clientView;
    public ListView<String> serverView;

    public Label clientLabel;
    public Label serverLabel;

    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;

    private byte[] buf;

    private void read() {
        try {
            while (true) {
                String command = is.readUTF();
                if (command.equals("#LIST")) {
                    Platform.runLater(()-> serverView.getItems().clear());
                    int count = is.readInt();
                    for (int i = 0; i < count; i++) {
                        String fileName = is.readUTF();
                        Platform.runLater(() -> serverView.getItems().add(fileName));
                    }
                }
                if (command.equals("#SEND#FILE ")) {
                    SenderUtils.getFileFromInputStream(is, currentDir);
                    //client state updated
                    Platform.runLater(this::fillCurrentDirFiles);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // reconnect to server
        }
    }

    private void fillCurrentDirFiles() {
        clientView.getItems().clear();
        clientView.getItems().add("..");
        clientView.getItems().addAll(currentDir.list());
        clientLabel.setText(getClientFilesDetails());
    }

    private String getClientFilesDetails() {
        File[] files = currentDir.listFiles();
        long size = 0;
        String label;
        if (files != null) {
            label = files.length + " files in this dir. ";
            for (File file : files) {
                size+=file.length();
            }
            label+="Summary size: " + size/1024 + " Kb ";
        }else {
            label= "Current dir is empty.";
        }
        return label;
    }

    private void initClickListener() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = clientView.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                    fillCurrentDirFiles();
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buf = new byte[256];
            currentDir = new File(System.getProperty("user.home"));
            fillCurrentDirFiles();
            initClickListener();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF("#GET#FILE");
        os.writeUTF(fileName);
        os.flush();
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        SenderUtils.loadFileToOutputStream(os, currentFile);
    }
}
