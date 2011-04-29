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
			try {
				formatter = (Formatter)Class.forName("net.minecraft.server.ConsoleLogFormatter").newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		server.logConsole(formatter.format(record));
	}
}
