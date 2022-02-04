package com.geekbrains.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class FileRequest extends AbstractMessage {

    private String fileName;

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}