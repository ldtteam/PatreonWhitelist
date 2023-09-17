package com.ldtteam.patreonwhitelist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Log {
    private static Logger logger = LogManager.getLogger("patreonwhitelist");

    private Log() {
    }

    public static Logger getLogger() {
        return logger;
    }
}
