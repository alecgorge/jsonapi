/*
 *              weupnp - Trivial upnp java library
 *
 * Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 *
 */
package org.wetorrent.upnp;

import java.util.logging.Logger;

/**
 * A simple log utility class that contains contants (such as the log name)
 * and logging methods.
 */
public class LogUtils {

    /**
     * Retrieves the logger to use to file entries
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Gets the log name
     * @return the name used by weupnp to create logs
     */
    public static String getLogName() {
        return LOG_NAME;
    }

    /**
     * The name used for logging with java.util.logging.
     * Right now it is <tt>org.wetorrent.weupnp</tt>.
     */
    private static final String LOG_NAME = "org.wetorrent.weupnp";

    /**
     * The logger used by weupnp
     */
    private static Logger logger = Logger.getLogger(LOG_NAME);

}
