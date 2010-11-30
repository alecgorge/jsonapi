

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class JSONServer extends NanoHTTPD {
	Hashtable<String, Object> methods = new Hashtable<String, Object>();
	Hashtable<String, String> logins = new Hashtable<String, String>(); 

	public JSONServer(Hashtable<String, String> logins) throws IOException {
		super(JSONApi.port);
		
		this.logins = logins;

		methods.put("server", new XMLRPCServerAPI());
		methods.put("minecraft", new XMLRPCMinecraftAPI());
		methods.put("player", new XMLRPCPlayerAPI());
	}
	
	public Object callMethod(String cat, String method, Object[] params) {
		for(Method m : methods.get(cat).getClass().getMethods()) {
			if(m.getName().equals(method)) {
				try {
					Class<?>[] argTypes = m.getParameterTypes();
					int key = -1;
					for(Class<?> arg : argTypes) {
						key++;
						
						if(arg.getName().equals("int")) {
							params[key] = new Integer(params[key].toString());
						}
					}		
					return m.invoke(methods.get(cat), params);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public boolean methodExists (String cat, String method) {
		for(Method m : methods.get(cat).getClass().getMethods()) {
			if(m.getName().equals(method)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean testLogin (String u, String p) {
		try {
			return logins.get(u).equals(p);
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static String callback (String callback, String json) {
		if(callback == null) return json;
		return callback.concat("(").concat(json).concat(")");
	}
	
	public Response serve( String uri, String method, Properties header, Properties parms )	{
		String callback = parms.getProperty("callback");
		
		if(uri.equals("/api/subscribe")) {
			String source = parms.getProperty("source");
			String username = parms.getProperty("username");
			String password = parms.getProperty("password");
			
			if(!testLogin(username, password)) {
				JSONObject r = new JSONObject();                                             
				r.put("result", "error");                                                    
				r.put("error", "Invalid username/password.");                                
				return new NanoHTTPD.Response(HTTP_FORBIDDEN, MIME_JSON, callback(callback, r.toJSONString()));  
			}                                                                                
			                                                                                 
			if(JSONApi.logging) {
				JSONApi.log.info("[JSONApi] source="+ source);
			}
			
			try {
				HttpStream out = new HttpStream(source, callback);                                     
			                                                                                 
				return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, out);                
			} catch (Exception e) {                                                                     
				JSONObject r = new JSONObject();                                                 
				r.put("result", "error");                                                        
				r.put("error", "That source doesn't exist!");                                    
				return new NanoHTTPD.Response( HTTP_NOTFOUND, MIME_JSON, callback(callback, r.toJSONString())); 
			}
		}
		
		if(!uri.equals("/api/call")) {
			return new NanoHTTPD.Response( HTTP_NOTFOUND, MIME_PLAINTEXT, "Invalid API call.\r\n");
		}
		//System.out.println()
		
		JSONParser parse = new JSONParser();
		
		Object args = parms.getProperty("args","[]");
		String calledMethodHold = (String)parms.getProperty("method");
		
		String[] calledMethod = null;
		if(calledMethodHold != null) {
			calledMethod = calledMethodHold.split("\\.");
		}
		else {
			JSONObject r = new JSONObject();
			r.put("result", "error");
			r.put("error", "Method doesn't exist!");
			return new NanoHTTPD.Response( HTTP_NOTFOUND, MIME_JSON, callback(callback, r.toJSONString()));
		}
		
		String username = parms.getProperty("username");
		String password = parms.getProperty("password");
		
		if(!testLogin(username, password)) {
			JSONObject r = new JSONObject();
			r.put("result", "error");
			r.put("error", "Invalid username/password.");
			return new NanoHTTPD.Response(HTTP_FORBIDDEN, MIME_JSON, callback(callback, r.toJSONString()));
		}
		
		
		if(JSONApi.logging) {
			JSONApi.log.info("[JSONApi] method="+ parms.getProperty("method").concat("?args=").concat((String) args));
		}
		
		if(args == null || calledMethod == null || calledMethod.length < 2) {
			JSONObject r = new JSONObject();
			r.put("result", "error");
			r.put("error", "You need to pass a method and an array of arguments.");
			return new NanoHTTPD.Response( HTTP_NOTFOUND, MIME_JSON, callback(callback, r.toJSONString()));
		}
		else {
			try {
				args = parse.parse((String) args);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(args.getClass().getCanonicalName().endsWith("JSONArray")) {
				//for(Object x : (ArrayList)args) {
					Object result = callMethod(calledMethod[0], calledMethod[1], (Object[]) ((ArrayList) args).toArray(new Object[((ArrayList) args).size()]));
					if(result == null) {
						JSONObject r = new JSONObject();
						r.put("result", "error");
						r.put("error", "You need to pass a valid method and an array arguments.");
						return new NanoHTTPD.Response( HTTP_NOTFOUND, MIME_JSON, callback(callback, r.toJSONString()));
					}
					JSONObject r = new JSONObject();
					r.put("result", "success");
					r.put("source", calledMethodHold);
					r.put("success", result);

					return new NanoHTTPD.Response( HTTP_OK, MIME_JSON, callback(callback, r.toJSONString()));
				//}
			}
			JSONObject r = new JSONObject();
			r.put("result", "error");
			r.put("error", "You need to pass a method and an array of arguments.");
			return new NanoHTTPD.Response( HTTP_NOTFOUND, MIME_JSON, callback(callback, r.toJSONString()));
		}
	}
}
