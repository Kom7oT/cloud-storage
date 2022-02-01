package com.geekbrains.cloud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Data
public class FilesList extends AbstractMessage {

    private List<String> list;
    private String root;

    public FilesList(String root, Path dir) throws IOException {
       Path currentDir =  Paths.get(root).resolve(dir);
        list = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }
}