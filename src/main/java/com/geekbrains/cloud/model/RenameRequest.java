package com.geekbrains.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor

public class RenameRequest extends AbstractMessage {

    private String fileName;
    private String targetFileName;

    @Override
    public CommandType getType() {
        return CommandType.RENAME_REQUEST;
    }
}
