
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.simple.JSONObject;


public class HttpStream extends InputStream {
	private String type = "";
	private int next = 0;
	public static ArrayList<String[]> chatStack = new ArrayList<String[]>();
	public static ArrayList<String[]> consoleStack = new ArrayList<String[]>();
	public static ArrayList<String[]> commandStack = new ArrayList<String[]>();
	public static ArrayList<String[]> connectionsStack = new ArrayList<String[]>();
	public ArrayList<String[]> stack = null;
	public String callback = "";
	
	public HttpStream (String s, String callback) {
		type = s;
		this.callback = callback;
		
		if(type.equals("chat"))
			stack = chatStack;
		else if(type.equals("commands"))
			stack = commandStack;
		else if(type.equals("connections"))
			stack = connectionsStack;
		else if(type.equals("console"))
			stack = consoleStack;
		
		next = stack.size();
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
		
		JSONObject r = new JSONObject();
		JSONObject q = new JSONObject();
		
		if(type.equals("chat")) {
			r.put("player", stack.get(next)[0]);
			r.put("message", stack.get(next)[1]);
			//r.put("chat", q);
		}
		else if(type.equals("commands")) {
			r.put("player", stack.get(next)[0]);
			r.put("command", stack.get(next)[1]);
		}
		else if(type.equals("connections")) {
			r.put("action", stack.get(next)[0]);
			r.put("player", stack.get(next)[1]);
			//r.put(", value)
		}
		else if(type.equals("console")) {
			r.put("line", stack.get(next)[0]);
		}
		
		next++;
		q.put("source", type);
		q.put("data", r);
		return JSONServer.callback(callback, q.toJSONString()).concat("\r\n");
	}

	@Override
	public int read() throws IOException {
		return -1;
	}
}
