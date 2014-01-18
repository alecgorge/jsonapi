package com.alecgorge.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;
import java.util.Properties;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class JSONWebSocketServer extends WebSocketServer {
	JSONServer jsonServer;
	
	public JSONWebSocketServer (int port, JSONServer jsonServer) {
		super(new InetSocketAddress(port));
		this.jsonServer = jsonServer;
	}


	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(final WebSocket conn, String message) {
		String[] split = message.split("\\?", 2);
		
		NanoHTTPD.Response r = null;
		if(split.length < 2) {
			r = jsonServer.new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
		}
		else {
			Properties header = new Properties();
			NanoHTTPD.decodeParms(split[1], header);
			Properties p = new Properties();
			p.put("X-REMOTE-ADDR", conn.getRemoteSocketAddress().getAddress().getHostAddress());
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
							if(conn.isOpen()) {
								conn.send(line.trim()+"\r\n");
							}
							else {
								continueSending = false;
							}
						} catch (NotYetConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			})).start();
		}
		else {
			BufferedReader data;
			if(r.data != null)
				data = new BufferedReader(new InputStreamReader(r.data));
			else
				data = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(r.bytes)));
			
			try {
				String line = "";

				while((line = data.readLine()) != null) {
					conn.send(line+"\r\n");
				}
			}
			catch (IOException e) {
				conn.close(0);
			} catch (NotYetConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		// TODO Auto-generated method stub
		
	}
}
