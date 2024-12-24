package com.goldenrace.wallet.server.properties.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public final class AppLogger implements IAppLogger {

    private final Logger logger;

    public AppLogger(Logger logger) {
        this.logger = logger;
    }

    public static IAppLogger getLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new AppLogger(logger);
    }

    public static IAppLogger getLogger(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        return new AppLogger(logger);
    }

    /**
     * Returns a logger for the calling class or context.
     * <p/>
     * The fully-qualified name of that class is used to get an slf4j logger, which is then wrapped.
     * Typical usage is to use this method to initialize a static member variable, e.g.,
     * {@code private static final Logger LOG = Logger.getLogger();}
     * <p/>
     * As getStackTrace() isn't super cheap, this is not the sort of thing you want (or need)
     * to be doing hundreds of times a second;
     *
     * @return a logger for the current scope
     */
    public static IAppLogger getClassLogger() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement   element    = stacktrace[2];
        String              name       = element.getClassName();

        return new AppLogger(LoggerFactory.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    /*******************************************     LEVEL - TRACE   *****************************************/

    @Override
    public void trace(AppLog.Builder logBuilder) {
        if (isTraceEnabled()) {
            AppLog log = logBuilder.build();
            logger.trace(log.getFormat(), log.getArgs());
        }
    }

    @Override
    public void trace(AppLog.Builder logBuilder, Throwable throwable) {
        if (isTraceEnabled()) {
            AppLog log = logBuilder.build();
            logger.trace(log.getFormat(), log.getArgs());
            logger.trace(throwable.getMessage(), throwable);
        }
    }

    @Override
    public void trace(String message) {
        if (isTraceEnabled()) {
            logger.trace(message);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            logger.trace(format, arg);
        }
    }

    @Override
    public void trace(String format, Object... args) {
        if (isTraceEnabled()) {
            logger.trace(format, args);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            logger.trace(format, arg1, arg2);
        }
    }

    @Override
    public void trace(String message, Throwable throwable) {
        if (isTraceEnabled()) {
            logger.trace(message, throwable);
        }
    }

    @Override
    public void trace(Marker marker, String message) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, message);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, format, arg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, format, arg1, arg2);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object... args) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, format, args);
        }
    }

    @Override
    public void trace(Marker marker, String message, Throwable throwable) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, message);
        }
    }

    /*******************************************     LEVEL - DEBUG   *****************************************/

    @Override
    public void debug(AppLog.Builder logBuilder) {
        if (isDebugEnabled()) {
            AppLog log = logBuilder.build();
            logger.debug(log.getFormat(), log.getArgs());
        }
    }

    @Override
    public void debug(AppLog.Builder logBuilder, Throwable throwable) {
        if (isDebugEnabled()) {
            AppLog log = logBuilder.build();
            logger.debug(log.getFormat(), log.getArgs());
            logger.debug(throwable.getMessage(), throwable);
        }
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            logger.debug(message);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            logger.debug(format, arg);
        }
    }

    @Override
    public void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            logger.debug(format, args);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            logger.debug(format, arg1, arg2);
        }
    }

    @Override
    public void debug(String message, Throwable throwable) {
        if (isDebugEnabled()) {
            logger.debug(message, throwable);
        }
    }

    @Override
    public void debug(Marker marker, String message) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, message);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, format, arg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, format, arg1, arg2);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object... args) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, format, args);
        }
    }

    @Override
    public void debug(Marker marker, String message, Throwable throwable) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, message, throwable);
        }
    }

    /*******************************************     LEVEL - INFO   *****************************************/

    @Override
    public void info(AppLog.Builder logBuilder) {
        if (isInfoEnabled()) {
            AppLog log = logBuilder.build();
            logger.info(log.getFormat(), log.getArgs());
        }
    }

    @Override
    public void info(AppLog.Builder logBuilder, Throwable throwable) {
        if (isInfoEnabled()) {
            AppLog log = logBuilder.build();
            logger.info(log.getFormat(), log.getArgs());
            logger.info(throwable.getMessage(), throwable);
        }
    }

    @Override
    public void info(String message) {
        if (isInfoEnabled()) {
            logger.info(message);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            logger.info(format, arg);
        }
    }

    @Override
    public void info(String format, Object... args) {
        if (isInfoEnabled()) {
            logger.info(format, args);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            logger.info(format, arg1, arg2);
        }
    }

    @Override
    public void info(String message, Throwable throwable) {
        if (isInfoEnabled()) {
            logger.info(message, throwable);
        }
    }

    @Override
    public void info(Marker marker, String message) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, message);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, format, arg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, format, arg1, arg2);
        }
    }

    @Override
    public void info(Marker marker, String format, Object... args) {
        if (isInfoEnabled(marker)) {
            logger.warn(marker, format, args);
        }
    }

    @Override
    public void info(Marker marker, String message, Throwable throwable) {
        if (isInfoEnabled(marker)) {
            logger.warn(marker, message, throwable);
        }
    }

    /*******************************************     LEVEL - WARNING   *****************************************/

    @Override
    public void warn(AppLog.Builder logBuilder) {
        if (isWarnEnabled()) {
            AppLog log = logBuilder.build();
            logger.warn(log.getFormat(), log.getArgs());
        }
    }

    @Override
    public void warn(AppLog.Builder logBuilder, Throwable throwable) {
        if (isWarnEnabled()) {
            AppLog log = logBuilder.build();
            logger.warn(log.getFormat(), log.getArgs());
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    @Override
    public void warn(String message) {
        if (isWarnEnabled()) {
            logger.warn(message);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            logger.warn(format, arg);
        }
    }

    @Override
    public void warn(String format, Object... args) {
        if (isWarnEnabled()) {
            logger.warn(format, args);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            logger.warn(format, arg1, arg2);
        }
    }

    @Override
    public void warn(String message, Throwable throwable) {
        if (isWarnEnabled()) {
            logger.warn(message, throwable);
        }
    }

    @Override
    public void warn(Marker marker, String message) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, message);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, format, arg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, format, arg1, arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... args) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, format, args);
        }
    }

    @Override
    public void warn(Marker marker, String message, Throwable throwable) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, message, throwable);
        }
    }

    /*******************************************     LEVEL - ERROR   *****************************************/

    @Override
    public void error(AppLog.Builder logBuilder) {
        if (isErrorEnabled()) {
            AppLog log = logBuilder.build();
            logger.error(log.getFormat(), log.getArgs());
        }
    }

    @Override
    public void error(AppLog.Builder logBuilder, Throwable throwable) {
        if (isErrorEnabled()) {
            AppLog log = logBuilder.build();
            logger.error(log.getFormat(), log.getArgs());
            logger.error(throwable.getMessage(), throwable);
        }
    }

    @Override
    public void error(String message) {
        if (isErrorEnabled()) {
            logger.error(message);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            logger.error(format, arg);
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            logger.error(format, arg1, arg2);
        }
    }

    @Override
    public void error(String format, Object... args) {
        if (isErrorEnabled()) {
            logger.error(format, args);
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (isErrorEnabled()) {
            logger.error(message, throwable);
        }
    }

    @Override
    public void error(Marker marker, String message) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, message);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, format, arg);
        }
    }

    @Override
    public void error(Marker marker, String message, Object arg1, Object arg2) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, message, arg1, arg2);
        }
    }

    @Override
    public void error(Marker marker, String format, Object... args) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, format, args);
        }
    }

    @Override
    public void error(Marker marker, String message, Throwable throwable) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, message, throwable);
        }
    }

}