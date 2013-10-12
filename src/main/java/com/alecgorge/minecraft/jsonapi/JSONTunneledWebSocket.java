package com.alecgorge.minecraft.jsonapi;

import java.io.InputStream;
import java.io.OutputStream;

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
		send(message + "!!!!!!!!!!!!");
		
//		String[] split = message.split("\\?", 2);
//		JSONServer jsonServer = JSONAPI.instance.jsonServer;
//		
//		NanoHTTPD.Response r = null;
//		if(split.length < 2) {
//			r = jsonServer.new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
//		}
//		else {
//			Properties header = new Properties();
//			NanoHTTPD.decodeParms(split[1], header);
//			Properties p = new Properties();
//			p.put("X-REMOTE-ADDR", socket.getRemoteSocketAddress().toString());
//			r = jsonServer.serve(split[0], "GET", p, header);
//		}
//		
//		if(r.data instanceof StreamingResponse) {
//			final StreamingResponse s = (StreamingResponse)r.data;
//			(new Thread(new Runnable() {
//				@Override
//				public void run() {
//					String line = "";
//					boolean continueSending = true;
//					
//					while((line = s.nextLine()) != null && continueSending) {
//						try {
//							if(socket.isConnected()) {
//								send(line.trim()+"\r\n");
//							}
//							else {
//								continueSending = false;
//							}
//						} catch (NotYetConnectedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (IllegalArgumentException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//			})).start();
//		}
//		else {
//			BufferedReader data;
//			if(r.data != null)
//				data = new BufferedReader(new InputStreamReader(r.data));
//			else
//				data = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(r.bytes)));
//			
//			try {
//				String line = "";
//
//				while((line = data.readLine()) != null) {
//					send(line+"\r\n");
//				}
//			}
//			catch (IOException e) {
//				try {
//					socket.close();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			} catch (NotYetConnectedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}		
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
