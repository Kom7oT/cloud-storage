package com.geekbrains.cloud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Data
public class FilesList extends AbstractMessage {

    private List<String> list;

    public String getCurrentDir() {
        return currentDir;
    }

    String currentDir;

    public FilesList(Path dir) throws IOException {
        list = Files.list(dir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        currentDir = String.valueOf(dir);
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }
}