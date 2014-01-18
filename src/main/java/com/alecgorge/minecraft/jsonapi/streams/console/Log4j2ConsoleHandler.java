package com.alecgorge.minecraft.jsonapi.streams.console;

//#if mc17OrNewer=="yes"
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
//#endif

import com.alecgorge.minecraft.jsonapi.JSONServer;

public class Log4j2ConsoleHandler {
	public Log4j2ConsoleHandler(JSONServer server) {
		// #if mc17OrNewer=="yes"
		Logger l = (Logger) LogManager.getRootLogger();
		l.addAppender(new ConsoleStubAppender(server));
		// #endif
	}
}
