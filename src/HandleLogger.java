import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class HandleLogger extends Handler {
    public HandleLogger(Formatter formatter) {
        setFormatter(formatter);
    }

    /**
     * Publish the record.
     * 
     * @param record
     */
    public void publish(LogRecord record) {
    	HttpStream.log("console", new String[] {getFormatter().format(record)});
    }

    /**
     * Flush the stream.
     */
    public void flush() {

    }

    /**
     * Close the handler.
     */
    public void close() {
    }
}
