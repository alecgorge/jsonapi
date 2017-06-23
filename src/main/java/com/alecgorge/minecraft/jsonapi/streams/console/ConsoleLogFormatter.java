package com.alecgorge.minecraft.jsonapi.streams.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

final public class ConsoleLogFormatter extends Formatter {

    private SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ConsoleLogFormatter() {}

    public String format(LogRecord logrecord) {
        StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append(this.a.format(Long.valueOf(logrecord.getMillis())));
        stringbuilder.append(" [");
        stringbuilder.append(logrecord.getLevel().getLocalizedName().toUpperCase());
        stringbuilder.append("] ");

        stringbuilder.append(logrecord.getMessage());
        stringbuilder.append('\n');
        Throwable throwable = logrecord.getThrown();

        if (throwable != null) {
            StringWriter stringwriter = new StringWriter();

            throwable.printStackTrace(new PrintWriter(stringwriter));
            stringbuilder.append(stringwriter);
        }

        return stringbuilder.toString();
    }
}
