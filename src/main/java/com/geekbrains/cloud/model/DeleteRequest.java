package com.geekbrains.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor

public class DeleteRequest extends AbstractMessage {

    private String filename;

    @Override
    public CommandType getType() {
        return CommandType.DELETE_REQUEST;
    }
}
