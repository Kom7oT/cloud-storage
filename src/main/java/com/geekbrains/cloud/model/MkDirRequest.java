package com.geekbrains.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor

public class MkDirRequest extends AbstractMessage {

    private String dirName;

    @Override
    public CommandType getType() {
        return CommandType.MKDIR_REQUEST;
    }
}

