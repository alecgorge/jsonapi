package com.alecgorge.minecraft.jsonapi.streams.console;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.alecgorge.minecraft.jsonapi.JSONServer;

@Plugin(name = "ConsoleStub", category = "Core", elementType = "appender", printObject = true)

public class ConsoleStubAppender
extends AbstractAppender
{
	JSONServer server;
	
	public ConsoleStubAppender(JSONServer server) {
		super("JSONAPI", null, PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg%n", null, null, null, null), false);
		
		this.server = server;
	}
	
	@Override
	public boolean isStarted() {
		return true;
	}

	@Override
	public void append(LogEvent e) {
		server.logConsole(getLayout().toSerializable(e).toString());
	}
}
