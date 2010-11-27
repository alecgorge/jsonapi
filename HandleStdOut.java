import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HandleStdOut extends ByteArrayOutputStream {
	private PrintStream printStream = null;
	private StringBuffer buffer = new StringBuffer();
	
	public HandleStdOut (PrintStream old) {
		printStream = old;
	}
	
	/*
	 *  Override this method to intercept the output text. Each line of text
	 *  output will actually involve invoking this method twice:
	 *
	 *  a) for the actual text message
	 *  b) for the newLine string
	 *
	 *  The message will be treated differently depending on whether the line
	 *  will be appended or inserted into the Document
	 */
	public void flush() {
		String message = toString();

		if (message.length() == 0) return;

		if (message.endsWith("\r") || message.endsWith("\n")) {
			buffer.append(message);
		}
		else {
			buffer.append(message);
			if (printStream != null) {
				printStream.println(message);
			}
			
			HttpStream.consoleStack.add(new String[] {message});
			//printStream.println("\r\nOUTPUT:::"+message);

			buffer.setLength(0);
		}

		reset();
	}
}
