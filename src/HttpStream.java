
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.simple.JSONObject;


public class HttpStream extends InputStream {
	private String type = "";
	private int next = 0;
	public static ArrayList<String[]> chatStack = new ArrayList<String[]>();
	public static ArrayList<String[]> consoleStack = new ArrayList<String[]>();
	public static ArrayList<String[]> commandStack = new ArrayList<String[]>();
	public static ArrayList<String[]> connectionsStack = new ArrayList<String[]>();
	public static ArrayList<String[]> allStack = new ArrayList<String[]>();
	public ArrayList<String[]> stack = null;
	public HashMap<String, Integer> stackCount = new HashMap<String, Integer>();
	public String callback = "";
	
	public HttpStream (String s, String callback) throws Exception {
		type = s;
		this.callback = callback;
		
		stack = getStack(type);
		next = stack.size();
	}
	
	public static void log (String type, String[] event) {
		String[] newArgs = new String[event.length+1];
		newArgs[0] = String.valueOf(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).getTime());
		
		String[] newArgs2 = new String[newArgs.length+1];
		newArgs2[0]= type;
		
		System.arraycopy(event, 0, newArgs, 1, event.length);
		System.arraycopy(newArgs, 0, newArgs2, 1, newArgs.length);

		getStack(type).add(newArgs);
		getStack("all").add(newArgs2);
	}
	
	public static ArrayList<String[]> getStack (String type) {
		if(type.equals("chat"))
			return chatStack;
		else if(type.equals("commands"))
			return commandStack;
		else if(type.equals("connections"))
			return connectionsStack;
		else if(type.equals("console"))
			return consoleStack;
		else if(type.equals("all"))
			return allStack;

		return null;
	}
	
	public String getNext () {
		while(next >= stack.size()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JSONObject q = format(type);
		
		return JSONServer.callback(callback, q.toJSONString()).concat("\r\n");
	}
	
	private JSONObject formatOne (String type, String[] theseArgs) {
		JSONObject r = new JSONObject();
		if(type.equals("chat")) {
			r.put("time", theseArgs[0]);
			r.put("player", theseArgs[1]);
			r.put("message", theseArgs[2]);
			//r.put("chat", q);
		}
		else if(type.equals("commands")) {
			r.put("time", theseArgs[0]);
			r.put("player", theseArgs[1]);
			r.put("command", theseArgs[2]);
		}
		else if(type.equals("connections")) {
			r.put("time", theseArgs[0]);
			r.put("action", theseArgs[1]);
			r.put("player", theseArgs[2]);
			//r.put(", value)
		}
		else if(type.equals("console")) {
			r.put("time", theseArgs[0]);
			r.put("line", theseArgs[1]);
		}
		else if(type.equals("all")) {
			//JSONObject z = new JSONObject();
			String[] nextStack = allStack.get(next);
			
			r.put("source", nextStack[0]);
			
			String[] nextArgs = new String[nextStack.length-1];
			System.arraycopy(nextStack, 1, nextArgs, 0, nextStack.length-1);
			
			r.put("data", formatOne(nextStack[0], nextArgs));
			// r.put("all", z);
		}
		return r;
	}
	
	private JSONObject format (String type) {
		JSONObject q = new JSONObject();
		JSONObject r = formatOne(type, getStack(type).get(next));
		
		next++;

		q.put("source", type);
		q.put("data", r);
		
		return q;
	}

	@Override
	public int read() throws IOException {
		throw new IOException("not implemented");
	}
}
