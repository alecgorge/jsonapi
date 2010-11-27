import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketDraft;
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
				if(params.length > 1) {
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
		
		NanoHTTPD.Response r = JSONApi.server.serve("/api/call", "GET", new Properties(), getQueryMap(message));
		
		InputStream data = r.data;
		String sbuff = "";
		
		try {
			if(data.getClass().getName().toString().endsWith("HttpStream")) {
				String next = ((HttpStream)data).getNext();
				System.out.println("next".concat(next));
				while(next != null) {
					conn.send(next);
					next = ((HttpStream)data).getNext();
				}
			}
			else if ( data != null) {
				byte[] buff = new byte[2048];
				while (true) {
					int read = data.read( buff, 0, 2048 );
					System.out.println("BUFF: ".concat(new String(buff).trim()));
					sbuff.concat("BUFF".concat(new String(buff).trim()));	
					System.out.println(sbuff);

					if (read <= 0)
						break;
				}
				System.out.println("EXAMPLE: ..".concat(sbuff).concat(".."));
				conn.send(sbuff);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
