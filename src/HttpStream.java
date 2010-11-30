
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONObject;


public class HttpStream extends InputStream {
	private String type = "";
	private int next = 0;
	public static ArrayList<String[]> chatStack = new ArrayList<String[]>();
	public static ArrayList<String[]> consoleStack = new ArrayList<String[]>();
	public static ArrayList<String[]> commandStack = new ArrayList<String[]>();
	public static ArrayList<String[]> connectionsStack = new ArrayList<String[]>();
	public ArrayList<String[]> stack = null;
	public HashMap<String, Integer> stackCount = new HashMap<String, Integer>();
	public String callback = "";
	
	public HttpStream (String s, String callback) throws Exception {
		type = s;
		this.callback = callback;
		
		stack = getStack(type);
		
		if(stack != null)
			next = stack.size();
		else {
			if(type.equals("all")) {
				stackCount.put("chat", getStack("chat").size());
				stackCount.put("console", getStack("console").size());
				stackCount.put("commands", getStack("commands").size());
				stackCount.put("connections", getStack("connections").size());
			}
			else {
				throw new Exception();
			}
		}
	}
	
	public ArrayList<String[]> getStack (String type) {
		if(type.equals("chat"))
			return chatStack;
		else if(type.equals("commands"))
			return commandStack;
		else if(type.equals("connections"))
			return connectionsStack;
		else if(type.equals("console"))
			return consoleStack;

		return null;
	}
	
	public String getNext () {
		String thistype = null;
		if(stack == null) {
			while(true) {
				try {
					Thread.sleep(500);
					
					Set<String> c = stackCount.keySet();
					
					Iterator<String> itr = c.iterator();
					
					while(itr.hasNext()) {
						String key = itr.next();
						int x = stackCount.get(key).intValue();
						
						if(x < getStack(key).size()) {
							thistype = key;
							stackCount.put(key, x++);
							next = x;
							break;
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		else {
			while(next >= stack.size()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			thistype = type;
		}
		
		JSONObject r = new JSONObject();
		JSONObject q = new JSONObject();
		
		if(thistype.equals("chat")) {
			r.put("player", chatStack.get(next)[0]);
			r.put("message", chatStack.get(next)[1]);
			//r.put("chat", q);
		}
		else if(thistype.equals("commands")) {
			r.put("player", commandStack.get(next)[0]);
			r.put("command", commandStack.get(next)[1]);
		}
		else if(thistype.equals("connections")) {
			r.put("action", connectionsStack.get(next)[0]);
			r.put("player", connectionsStack.get(next)[1]);
			//r.put(", value)
		}
		else if(thistype.equals("console")) {
			r.put("line", consoleStack.get(next)[0]);
		}
		
		next++;
		q.put("source", thistype);
		q.put("data", r);
		return JSONServer.callback(callback, q.toJSONString()).concat("\r\n");
	}

	@Override
	public int read() throws IOException {
		throw new IOException("not implemented");
	}
}
