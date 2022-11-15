package com.smc.connector.util;

import com.smc.connector.model.Request;

public class AsyncTask extends Thread {

    public AsyncTask(Request request) {
        _request = request;
    }

    @Override
    public void run() {
        _request.extractRecent();
    }

    private final Request _request;
}
