package com.biit.liferay.auto.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiferayAutoconfiguratorLogger {
    private static final Logger logger = LoggerFactory.getLogger(LiferayAutoconfiguratorLogger.class);

    private LiferayAutoconfiguratorLogger() {
    }

    private static void debug(String message) {
        logger.debug(message);
    }

    public static void debug(String className, String message) {
        debug(className + ": " + message);
    }

    public static void entering(String className, String method) {
        debug(className, "ENTRY (" + method + ")");
    }

    private static void error(String message) {
        logger.error(message);
    }

    public static void error(String className, String message) {
        error(className + ": " + message);
    }

    public static void errorMessage(String className, Throwable throwable) {
        String error = getStackTrace(throwable);
        error(className, error);
    }

    public static void exiting(String className, String method) {
        debug(className, "RETURN (" + method + ")");
    }

    private static void fatal(String message) {
        logger.error(message);
    }

    public static void fatal(String className, String message) {
        fatal(className + ": " + message);
    }

    private static String getStackTrace(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
    }

    private static void info(String message) {
        logger.info(message);
    }

    public static void info(String className, String message) {
        info(className + ": " + message);
    }

    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    private static void warning(String message) {
        logger.warn(message);
    }

    public static void warning(String className, String message) {
        warning(className + ": " + message);
    }
}
