package com.alecgorge.java.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

public class HttpServer {
	protected int port;
	protected boolean ssl;
	protected InetAddress bindAddress;
	
	Thread readThread;
	ServerSocket serverSocket;
	
	public HttpServer(int port) {
		this(port, false, null);
	}
	
	public HttpServer(int port, boolean ssl) {
		this(port, ssl, null);
	}
	
	public HttpServer(int port, boolean ssl, InetAddress bindAddress) {
		this.port = port;
		this.ssl = ssl;
		this.bindAddress = bindAddress;
	}
	
	public void start() throws IOException {
		if (ssl) {
			ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
			serverSocket = ssocketFactory.createServerSocket(port);
		} else {
			if (bindAddress != null) {
				serverSocket = new ServerSocket(port, /* default value */-1, bindAddress);
			} else {
				serverSocket = new ServerSocket(port);
			}
		}
		
		final HttpServer server = this;
		readThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						final Socket socket = serverSocket.accept();
						(new Thread(new Runnable() {
							@Override
							public void run() {
								new HttpRequestSocketHandler(socket, server);
							}							
						})).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		readThread.setDaemon(true);
		readThread.start();
	}
	
	public void stop() throws InterruptedException {
		readThread.join();
	}
	
	public HttpResponse serve(HttpRoute route) {
		return null;
	}
}
