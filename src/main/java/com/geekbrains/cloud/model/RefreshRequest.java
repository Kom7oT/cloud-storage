package com.geekbrains.cloud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
@EqualsAndHashCode(callSuper = true)
@Data
public class RefreshRequest extends AbstractMessage {


    private String root;
    private String dir;

    public RefreshRequest(String root, String dir) {
        this.root=root;
        this.dir = dir;
    }
    public String getDir() {
        return dir;
    }

    public String getRoot() {
        return dir;
    }

    public RefreshRequest(String dir) {
        this.dir = dir;
    }

    @Override
    public CommandType getType() {
        return CommandType.REFRESH_REQUEST;
    }
}