package com.goldenrace.wallet.server.properties.logging;

import org.slf4j.Logger;

public interface IAppLogger extends Logger {

    /**
     * @param logBuilder
     */
    void trace(AppLog.Builder logBuilder);

    /**
     * @param logBuilder
     * @param throwable
     */
    void trace(AppLog.Builder logBuilder, Throwable throwable);

    /**
     * @param logBuilder
     */
    void debug(AppLog.Builder logBuilder);

    /**
     * @param logBuilder
     * @param throwable
     */
    void debug(AppLog.Builder logBuilder, Throwable throwable);

    /**
     * @param logBuilder
     */
    void info(AppLog.Builder logBuilder);

    /**
     * @param logBuilder
     * @param throwable
     */
    void info(AppLog.Builder logBuilder, Throwable throwable);

    /**
     * @param logBuilder
     */
    void warn(AppLog.Builder logBuilder);

    /**
     * @param logBuilder
     * @param throwable
     */
    void warn(AppLog.Builder logBuilder, Throwable throwable);

    /**
     * @param logBuilder
     */
    void error(AppLog.Builder logBuilder);

    /**
     * @param logBuilder
     * @param throwable
     */
    void error(AppLog.Builder logBuilder, Throwable throwable);

}
