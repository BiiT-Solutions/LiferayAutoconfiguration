package com.biit.liferay.auto.log;

/*-
 * #%L
 * Liferay Basic Configuration Creation (Core)
 * %%
 * Copyright (C) 2017 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class LiferayAutoconfiguratorLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiferayAutoconfiguratorLogger.class);

    private LiferayAutoconfiguratorLogger() {
    }

    private static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void debug(String className, String message) {
        debug(className + ": " + message);
    }

    public static void entering(String className, String method) {
        debug(className, "ENTRY (" + method + ")");
    }

    private static void error(String message) {
        LOGGER.error(message);
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
        LOGGER.error(message);
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
        LOGGER.info(message);
    }

    public static void info(String className, String message) {
        info(className + ": " + message);
    }

    public static boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    private static void warning(String message) {
        LOGGER.warn(message);
    }

    public static void warning(String className, String message) {
        warning(className + ": " + message);
    }
}
