package com.geekbrains.cloud.model;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {

    public abstract CommandType getType();

}