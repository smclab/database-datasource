package com.smc.connector.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    public static final Logger logger =
            LoggerFactory.getLogger("database_logger");

    public static <T> T cast(Object o, Class<T> clazz) {
        return clazz.cast(o);
    }
}
