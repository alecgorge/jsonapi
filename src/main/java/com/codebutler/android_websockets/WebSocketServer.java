package com.codebutler.android_websockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class WebSocketServer {
	InputStream 						inputStream;
	OutputStream 						outputStream;
	HybiParser.HappyDataInputStream 	websocketInputStream;
	
	public HybiParser 					parser;
	final Object 						sendLock = new Object();
	
	public WebSocketServer(InputStream input, OutputStream output) {
		inputStream = input;
		outputStream = output;
		
		websocketInputStream = new HybiParser.HappyDataInputStream(inputStream);
		parser = new HybiParser(this);		
	}
	
	public void start() {
		try {
			parser.start(websocketInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public WebSocketServer getListener() {
		return this;
	}
	
    public void send(String data) {
        sendFrame(parser.frame(data));
    }

    public void send(byte[] data) {
        sendFrame(parser.frame(data));
    }
    
	public void sendFrame(final byte[] frame) {
		try {
			synchronized (sendLock) {
				outputStream.write(frame);
				outputStream.flush();
			}
		} catch (IOException e) {
			getListener().onError(e);
		}
	}
	
	public abstract void onConnect();
	public abstract void onMessage(String message);
	public abstract void onMessage(byte[] data);
	public abstract void onDisconnect(int code, String reason);
	public abstract void onError(Exception error);
}
