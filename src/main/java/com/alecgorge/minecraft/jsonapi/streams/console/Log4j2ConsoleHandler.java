package com.alecgorge.minecraft.jsonapi.streams.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.alecgorge.minecraft.jsonapi.JSONServer;

public class Log4j2ConsoleHandler {
	public Log4j2ConsoleHandler(JSONServer server) {
		Logger l = (Logger)LogManager.getRootLogger();
		l.addAppender(new ConsoleStubAppender(server));
	}
}
