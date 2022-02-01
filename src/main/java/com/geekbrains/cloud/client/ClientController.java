package com.geekbrains.cloud.client;

import com.geekbrains.cloud.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j

public class ClientController {

    public ListView<String> clientView;
    public ListView<String> serverView;
    public Label clientLabel;
    public Label serverLabel;
    public HBox authorization;
    public AnchorPane mainBox;
    public Button download;
    public Button upload;
    public Label clientPath;
    public Button clientUp;
    private Path clientDir;
    private Path serverDir;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    // sync mode
    // recommended mode
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    private void fillCurrentDirFiles() {

        Platform.runLater(() -> {
            try {
                clientView.getItems().clear();
                clientView.getItems().addAll(clientDir.toFile().list());
                clientLabel.setText(getClientFilesDetails());
                clientPath.setText(String.valueOf(clientDir));
            } catch (NullPointerException e) {
            }
        });
    }

    public void setAuthorized(boolean isAuthorized) {
        if (!isAuthorized) {
            mainBox.setVisible(false);
            mainBox.setManaged(false);
            authorization.setVisible(true);
            authorization.setManaged(true);
        } else {
            mainBox.setVisible(true);
            mainBox.setManaged(true);
            authorization.setVisible(false);
            authorization.setManaged(false);
        }
    }

    private String getClientFilesDetails() {
        File[] files = clientDir.toFile().listFiles();
        long size = 0;
        String label;
        if (files != null) {
            label = files.length + " files in current dir. ";
            for (File file : files) {
                size += file.length();
            }
            label += "Summary size: " + size / 1024 + " Kb.";
        } else {
            label = "Current dir is empty";
        }
        return label;
    }

    private void clientInitClickListener() {
        clientView.setOnMouseClicked(e -> {
            String fileName = clientView.getSelectionModel().getSelectedItem();
            if (e.getClickCount() == 2) {
                System.out.println("Выбран файл: " + fileName);
                Path path = clientDir.resolve(fileName);

                if (Files.isDirectory(path)) {
                    clientDir = path;
                    ClientController.this.fillCurrentDirFiles();
                }
            }
        });
    }private void serverInitClickListener() {
        serverView.setOnMouseClicked(e -> {
            String fileName = serverView.getSelectionModel().getSelectedItem();
            if (e.getClickCount() == 2) {
                System.out.println("Выбран файл: " + fileName);
                Path path = Paths.get("serverDir").resolve(serverDir).resolve(fileName);
                System.out.println("server dir = " + path);
                RefreshRequest refreshRequest = new RefreshRequest(String.valueOf(serverDir));
                System.out.println(refreshRequest);
                Network.sendMsg(refreshRequest);

//                FileRequest fileRequest = new FileRequest("serverDir/asd");
//                Network.sendMsg(fileRequest);

                if (Files.isRegularFile(path)) {
                    System.out.println("YESS");
                }
            }
        });
    }

    public void connect() {

        Thread t = new Thread(() -> {

            clientDir = Paths.get(System.getProperty("user.home"));
            fillCurrentDirFiles();
            clientInitClickListener();
            serverInitClickListener();
            try {

                while (true) {
                    AbstractMessage message = Network.readObject();
                    log.info(String.valueOf(message.getType()));
                    switch (message.getType()) {
                        case AUTH_REQUEST:
                            AuthRequest ar = (AuthRequest) message;
                            if (ar.isAuthorization()) {
                                setAuthorized(ar.isAuthorization());
                                serverDir = Paths.get(ar.getLogin());
                            }
                            break;
                        case FILE_MESSAGE:
                            FileMessage fileMessage = (FileMessage) message;
                            Files.write(clientDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                            fillCurrentDirFiles();
                            break;
                        case LIST:
                            FilesList list = (FilesList) message;
                            updateServerView(list.getList());
                    }

                }

            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void updateServerView(List<String> names) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(names);
        });
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        Network.sendMsg(new FileRequest(fileName));
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        FileMessage fileMessage = new FileMessage(clientDir.resolve(fileName));
        Network.sendMsg(fileMessage);
    }

    public void tryToAuth() throws IOException, ClassNotFoundException {
        Network.start();
        AuthRequest authRequest = new AuthRequest(loginField.getText(), passwordField.getText());
        Network.sendMsg(authRequest);
        connect();
    }

    public void toParentDir(ActionEvent actionEvent) {
        clientDir = clientDir.getParent();
        fillCurrentDirFiles();
    }
}