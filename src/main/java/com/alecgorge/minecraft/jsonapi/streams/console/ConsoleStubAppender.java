package com.alecgorge.minecraft.jsonapi.streams.console;

import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.plugins.*;
import com.alecgorge.minecraft.jsonapi.*;
import org.apache.logging.log4j.core.layout.*;
import org.apache.logging.log4j.core.*;

@Plugin(name = "ConsoleStub", category = "Core", elementType = "appender", printObject = true)
public class ConsoleStubAppender extends AbstractAppender
{
	JSONServer server;

	public ConsoleStubAppender(final JSONServer server) {
		super("JSONAPI", (Filter)null, (Layout)PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg%n").build(), false);
		this.server = server;
	}

	public boolean isStarted() {
		return true;
	}

	public void append(final LogEvent e) {
		this.server.logConsole(this.getLayout().toSerializable(e).toString());
	}
}