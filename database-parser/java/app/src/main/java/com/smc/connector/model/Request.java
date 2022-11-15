package com.smc.connector.model;

import java.util.Map;

public abstract class Request {
    public Request(Map model) {}

    public abstract void extractRecent();
}
