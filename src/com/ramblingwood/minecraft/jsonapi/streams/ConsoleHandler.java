package com.ramblingwood.minecraft.jsonapi.streams;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.ramblingwood.minecraft.jsonapi.JSONServer;

public class ConsoleHandler extends Handler {
	Formatter formatter;
	JSONServer server;
	
	public ConsoleHandler (JSONServer _server) {
		super();
		server = _server;
	}
	
	public void close() throws SecurityException {

	}

	public void flush() {

	}

	public void publish(LogRecord record) {
		if(formatter == null) {
			formatter = new ConsoleLogFormatter();
		}

		server.logConsole(formatter.format(record));
	}
}
