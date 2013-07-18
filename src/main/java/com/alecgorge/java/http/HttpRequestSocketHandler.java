package com.alecgorge.java.http;

import java.net.Socket;

public class HttpRequestSocketHandler {
	Socket socket;
	HttpServer server;
	
	public HttpRequestSocketHandler(Socket _socket, HttpServer server) {
		socket = _socket;
		this.server = server;
	}
	
}
