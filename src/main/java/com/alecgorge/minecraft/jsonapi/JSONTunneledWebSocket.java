package com.alecgorge.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.NotYetConnectedException;
import java.util.Properties;

import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;
import com.codebutler.android_websockets.WebSocketServer;

public class JSONTunneledWebSocket extends WebSocketServer {
	public JSONTunneledWebSocket(InputStream input, OutputStream output) {
		super(input, output);
	}

	@Override
	public void onConnect() {
	}

	@Override
	public void onMessage(String message) {
		String[] split = message.split("\\?", 2);
		JSONServer jsonServer = JSONAPI.instance.jsonServer;
		
		NanoHTTPD.Response r = null;
		if(split.length < 2) {
			r = jsonServer.new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
		}
		else {
			Properties header = new Properties();
			NanoHTTPD.decodeParms(split[1], header);
			Properties p = new Properties();
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
							send(line.trim());
						} catch (Exception e) {
							continueSending = false;
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
					send(line);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
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
	public void onMessage(byte[] data) {
	}

	@Override
	public void onDisconnect(int code, String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Exception error) {
		error.printStackTrace();
	}

}
