import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Properties;

import org.json.simple.JSONObject;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;

public class JSONWebSocket extends WebSocketServer {
	public JSONWebSocket (int port) {
		super(port);
	}
	
	public JSONWebSocket() {
		this(20060);
	}

	@Override
	public void onClientOpen(WebSocket conn) {
		if(JSONApi.whitelist.size() > 0) {
			boolean valid = false;
			for(int i = 0; i < JSONApi.whitelist.size(); i++) {
				if(conn.socketChannel().socket().getInetAddress().getHostAddress() == JSONApi.whitelist.get(i)) {
					valid = true;
				}
			}
			if(!valid) {
				try {
					conn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onClientClose(WebSocket conn) {
    }

    
	public static Properties getQueryMap(String query)	{
	    String[] params = query.split("&");
	    
	    Properties p = new Properties();
	    for (String param : params) {
	        String name = param.split("=")[0];
	        String value = "";
			try {
				if(param.indexOf("=") > -1) {
					value = URLDecoder.decode(param.split("=")[1], "UTF-8");
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p.put(name, value);
	    }
	    return p;
	}

	@Override
	public void onClientMessage(WebSocket conn, String message) {
		// format method=xyz&args=[]&username=user&password=pass
		String[] split = message.split("\\?", 2);
		
		NanoHTTPD.Response r = null;
		if(split.length < 2) {
			JSONObject ro = new JSONObject();
			ro.put("result", "error");
			ro.put("error", "Incorrect. WebSocket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....");
			r = (JSONApi.server).new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, ro.toJSONString());
		}
		else {
			Properties p = new Properties();
			JSONServer.decodeParms(split[1], p);
			r = JSONApi.server.serve(split[0], "GET", new Properties(), p);
		}
		
		InputStream data = r.data;
		
		try {
			
			if(data.getClass().getName().toString().endsWith("HttpStream")) {
				String next = ((HttpStream)data).getNext();
				//System.out.println("next".concat(next));
				while(next != null) {
					conn.send(next);
					next = ((HttpStream)data).getNext();
				}
			}
			else if ( data != null) {
				byte[] buff = new byte[4096];
				int read = 1;
				String buffer = "";
				while (true) {
					read = data.read( buff, read-1, 4096 );
					buffer += new String(buff, WebSocket.UTF8_CHARSET);
					
					if (data.available() <= 0)
						break;
				}
				conn.send(buffer.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
