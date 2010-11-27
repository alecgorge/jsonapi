import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Used for formatting.
 *
 * @author sk89q
 */
public class LogFormat extends Formatter {
	public String format(LogRecord record) {
		StringBuilder text = new StringBuilder();
		
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(record.getMillis());
		text.append(String.format("%s-%s-%s %02d:%02d:%02d ",
									c.get(Calendar.YEAR),
									c.get(Calendar.MONTH)+1,
									c.get(Calendar.DATE),
									c.get(Calendar.HOUR_OF_DAY),
									c.get(Calendar.MINUTE),
									c.get(Calendar.SECOND)
									));
		
		Level level = record.getLevel();

		if (level == Level.FINEST) {
			text.append("[FINEST] ");
		} else if (level == Level.FINER) {
			text.append("[FINER] ");
		} else if (level == Level.FINE) {
			text.append("[FINE] ");
		} else if (level == Level.INFO) {
			text.append("[INFO] ");
		} else if (level == Level.WARNING) {
			text.append("[WARNING] ");
		} else if (level == Level.SEVERE) {
			text.append("[SEVERE] ");
		}

		text.append(record.getMessage());

		Throwable t = record.getThrown();
		if (t != null) {
			StringWriter writer = new StringWriter();
			t.printStackTrace(new PrintWriter(writer));
			text.append(writer.toString());
		}
		
		return text.toString();
	}
}