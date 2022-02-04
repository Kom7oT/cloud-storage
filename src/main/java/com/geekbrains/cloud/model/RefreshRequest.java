package com.geekbrains.cloud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
@EqualsAndHashCode(callSuper = true)
@Data
public class RefreshRequest extends AbstractMessage {


    private String dir;

    public String getDir() {
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