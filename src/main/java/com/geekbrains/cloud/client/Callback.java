package com.geekbrains.cloud.client;

import com.geekbrains.cloud.model.AbstractMessage;

import java.io.IOException;

public interface Callback {

    void onMessageReceived(AbstractMessage message) throws IOException;

}