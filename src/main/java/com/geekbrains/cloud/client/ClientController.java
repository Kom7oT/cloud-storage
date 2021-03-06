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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    public TextField clientPath;
    public TextField serverPath;
    public Button clientUp;
    public Button serverUp;
    public TextField inputText;
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

    private String getServerFilesDetails() {
        File[] files = serverDir.toFile().listFiles();
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
            serverView.getSelectionModel().clearSelection();
            if (e.getClickCount() == 2) {
                System.out.println("???????????? ????????: " + fileName);
                Path path = clientDir.resolve(fileName);
                if (Files.isDirectory(path)) {
                    clientDir = path;
                    ClientController.this.fillCurrentDirFiles();
                }
            }
        });

    }

    private void serverInitClickListener() {
        serverView.setOnMouseClicked(e -> {
            String fileName = serverView.getSelectionModel().getSelectedItem();
            clientView.getSelectionModel().clearSelection();
            if (e.getClickCount() == 2) {
                System.out.println("???????????? ????????: " + fileName);
                Path path = serverDir.resolve(fileName);
                sendPath(path);
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
                            Path cur = clientDir.resolve(fileMessage.getFileName());
                            if (!Files.exists(cur)){
                                Files.createFile(cur);
                            }
                            Files. write(
                                    clientDir.resolve(fileMessage.getFileName()),
                                    fileMessage.getBytes(),
                                    StandardOpenOption.APPEND //?????????????? ??????????????????
                            );
                            fillCurrentDirFiles();
                            break;
                        case LIST:
                            FilesList list = (FilesList) message;
                            serverDir = Paths.get(list.getCurrentDir());
                            updateServerView(list.getList());
                            break;
                    }

                }

            } catch (SocketException e){
                connect();
            }
            catch (ClassNotFoundException | IOException e){
                e.printStackTrace();
            } catch (NullPointerException e) {

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
            serverPath.setText(String.valueOf(serverDir));
            serverLabel.setText(getServerFilesDetails());
        });
    }

    //???????????????? ?????????????? ???????? ?????? ???????????????????? ????????????????
    private void sendPath(Path dir) {
        RefreshRequest refreshRequest = new RefreshRequest(String.valueOf(dir));
        Network.sendMsg(refreshRequest);
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        Network.sendMsg(new FileRequest(fileName));
    }

    public void upload(ActionEvent actionEvent) throws IOException {


        //?????????????????? ?????????????????? ???? ??????????
        byte[] buffer = new byte[32 * 1024];
        String fileName = clientView.getSelectionModel().getSelectedItem();
        try (InputStream is = new FileInputStream(clientDir.resolve(fileName).toFile())) {
            while (is.available() > 0) {
                int cnt = is.read(buffer);
                if (cnt < 8192) {
                    byte[] tmp = new byte[cnt];
                    if (cnt >= 0) System.arraycopy(buffer, 0, tmp, 0, cnt);
                    Network.sendMsg(new FileMessage(fileName, tmp.clone()));
                } else {
                    Network.sendMsg(new FileMessage(fileName, buffer.clone()));
                }
            }
        }
        sendPath(serverDir);
    }

    public void delete(ActionEvent actionEvent) throws IOException {
        String fileName;
        if (clientView.getSelectionModel().getSelectedItem() != null) {
            fileName = (clientView.getSelectionModel().getSelectedItem());
            Files.delete(clientDir.resolve(fileName));
            log.info("???????? " + fileName + " ????????????");
            fillCurrentDirFiles();
        } else if (serverView.getSelectionModel().getSelectedItem() != null) {
            fileName = (serverView.getSelectionModel().getSelectedItem());
            Network.sendMsg(new DeleteRequest(fileName));
        }
    }

    public void mkDir(ActionEvent actionEvent) throws IOException {
        String dirName = inputText.getText();
        if (clientView.getSelectionModel().getSelectedItem() != null) {
            if (!Files.exists(clientDir.resolve(dirName))) {
                Files.createDirectory(clientDir.resolve(dirName));
                log.info("???????????????????? " + dirName + " ??????????????");
                fillCurrentDirFiles();
            } else log.info("???????????????????? ?????? ????????????????????!");
        } else if (serverView.getSelectionModel().getSelectedItem() != null) {
            Network.sendMsg(new MkDirRequest(dirName));
        }
    }

    public void rename(ActionEvent actionEvent) throws IOException {
        String fileName;
        String targetFileName = inputText.getText();
        if (clientView.getSelectionModel().getSelectedItem() != null) {
            fileName = (clientView.getSelectionModel().getSelectedItem());
            if (!Files.exists(clientDir.resolve(targetFileName))) {
                Files.move(clientDir.resolve(fileName), clientDir.resolve(targetFileName));
                fillCurrentDirFiles();
            } else log.info("???????? ?? ?????????? ???????????? ?????? ????????????????????!");
        } else if (serverView.getSelectionModel().getSelectedItem() != null) {
            fileName = (serverView.getSelectionModel().getSelectedItem());
            Network.sendMsg(new RenameRequest(fileName, targetFileName));
        }
    }

    public void tryToAuth() throws RuntimeException {
        Network.start();
        AuthRequest authRequest = new AuthRequest(loginField.getText(), passwordField.getText());
        Network.sendMsg(authRequest);
        connect();
    }

    public void toParentClientDir(ActionEvent actionEvent) {
        Path root = clientDir.getRoot();
        clientDir = clientDir.getParent();
        if (clientDir != null) {
            fillCurrentDirFiles();
        } else clientDir = root;
    }

    //???????????????? ?????????????? ?????????????????????????? ???????? ???? ????????????????
    public void toParentServerDir(ActionEvent actionEvent) {
        Path root = serverDir;  //???????????????????? ???????????????? ??????????????, ???????? ???????????????? ???? ????????????????????, ?????????? ???? ?????????????? NPE
        serverDir = serverDir.getParent();
        if (serverDir != null) {
            sendPath(serverDir);
        } else serverDir = root;
    }
}

