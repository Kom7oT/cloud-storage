package com.geekbrains.cloud.model;

public class AuthRequest extends AbstractMessage {

    private boolean authorization;
    private String login;
    private String password;
    private String nickname;

    public AuthRequest(boolean authorization, String login, String password, String nickname) {
        this.authorization = authorization;
        this.login = login;
        this.password = password;
        this.nickname = nickname;
    }

    public AuthRequest(boolean authorization) {
        this.authorization = authorization;
    }

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean isAuthorization() {
        return authorization;
    }

    public void setAuthorization(boolean authorization) {
        this.authorization = authorization;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_REQUEST;
    }
}