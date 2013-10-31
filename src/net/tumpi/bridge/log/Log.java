package net.tumpi.bridge.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jorge Madrid (66785403)
 */
public enum Log {

    $;
    private final String defaultLogger = "default";
    private final String infoLogger = "info";
    private final String errorLogger = "error";

    public void info(String msg) {
        getInfoLogger().info(msg);
    }

    public void info(String msg, Throwable ex) {
        getInfoLogger().info(msg, ex);
    }

    public void debug(String str) {
        getDefaultLogger().debug(str);
    }

    public void debug(String msg, Throwable ex) {
        getDefaultLogger().debug(msg, ex);
    }

    public void warn(String str) {
        getDefaultLogger().warn(str);
    }

    public void warn(String msg, Throwable ex) {
        getDefaultLogger().warn(msg, ex);
    }

    public void error(String msg) {
        getErrorLogger().error(msg);
    }

    public void error(String msg, Throwable ex) {
        getErrorLogger().error(msg, ex);
    }
    
    public void fatal(){
        getErrorLogger().fatal("fatal");
    }

    public void trace(String msg) {
        getDefaultLogger().trace(msg);
    }

    public void trace(String msg, Throwable ex) {
        getDefaultLogger().trace(msg, ex);
    }

    private Logger getDefaultLogger() {
        return LogManager.getLogger(defaultLogger);
    }

    private Logger getErrorLogger() {
        return LogManager.getLogger(errorLogger);
    }

    private Logger getInfoLogger() {
        return LogManager.getLogger(infoLogger);
    }
}