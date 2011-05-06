package com.ramblingwood.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;

import com.ramblingwood.minecraft.jsonapi.streams.StreamingResponse;

public class JSONWebSocketServer extends WebSocketServer {
	JSONServer jsonServer;
	
	public JSONWebSocketServer (int port, JSONServer jsonServer) {
		super(port);
		this.jsonServer = jsonServer;
	}
	
	@Override
	public void onClientClose(WebSocket conn) {
		
	}

	@Override
	public void onClientMessage(final WebSocket conn, String message) {
		// format method=xyz&args=[]&username=user&password=pass
		String[] split = message.split("\\?", 2);
		
		NanoHTTPD.Response r = null;
		if(split.length < 2) {
			r = jsonServer.new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
		}
		else {
			Properties header = new Properties();
			NanoHTTPD.decodeParms(split[1], header);
			Properties p = new Properties();
			p.put("X-REMOTE-ADDR", conn.socketChannel().socket().getInetAddress().getHostAddress());
			r = jsonServer.serve(split[0], "GET", p, header);
		}
		
		if(r.data instanceof StreamingResponse) {
			final StreamingResponse s = (StreamingResponse)r.data;
			(new Thread(new Runnable() {
				@Override
				public void run() {
					String line = "";
					boolean continueSending = true;
					
					while((line = s.nextLine()) != null && continueSending) {
						try {
							if(conn.socketChannel().isOpen() && conn.socketChannel().isConnected()) {
								conn.send(line.trim()+"\r\n");
							}
							else {
								continueSending = false;
							}
						} catch (IOException e) {
							continueSending = false;
							try {
								conn.close();
							} catch (IOException e1) {
								
							}
						}
					}
				}
			})).start();
		}
		else {
			BufferedReader data = new BufferedReader(new InputStreamReader(r.data));
			
			try {
				String line = "";

				while((line = data.readLine()) != null) {
					conn.send(line+"\r\n");
				}
			}
			catch (IOException e) {
				try {
					conn.close();
				} catch (IOException e1) {
					
				}
			}
		}
	}

	@Override
	public void onClientOpen(WebSocket conn) {
		
	}
}
