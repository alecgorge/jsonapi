package com.bukkit.alecgorge.jsonapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import net.tootallnate.websocket.WebSocket;

import org.json.simple.JSONObject;

public class JSONSocketServer implements Runnable{

	protected int		  	serverPort		= 20061;
	protected ServerSocket	serverSocket	= null;
	protected boolean		isStopped		= false;
	protected Thread		runningThread	= null;

	public JSONSocketServer(int port){
		this.serverPort = port;
	}

	public void run(){
		synchronized(this){
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(!isStopped()){
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();
				clientSocket.setTcpNoDelay(true);
			} catch (IOException e) {
				if(isStopped()) {
					return;
				}
				e.printStackTrace();
			}
			new Thread(
				new WorkerRunnable(clientSocket)
			).start();
		}
	}


	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class WorkerRunnable implements Runnable{

		protected Socket clientSocket = null;
		protected String serverText   = null;

		public WorkerRunnable(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {
				InputStream input  = clientSocket.getInputStream();
				OutputStream output = clientSocket.getOutputStream();
				
				final char[] buffer = new char[0x10000];
				StringBuilder out = new StringBuilder();
				Reader in = new InputStreamReader(input, "UTF-8");
				int read;
				do {
				  read = in.read(buffer, 0, buffer.length);
				  if (read>0) {
				    out.append(buffer, 0, read);
				  }
				} while (read>=0);
				
				// format method=xyz&args=[]&username=user&password=pass
				String[] split = out.toString().split("\\?", 2);
				
				NanoHTTPD.Response r = null;
				if(split.length < 2) {
					JSONObject ro = new JSONObject();
					ro.put("result", "error");
					ro.put("error", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....");
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
							output.write(next.getBytes());
							next = ((HttpStream)data).getNext();
						}
					}
					else if ( data != null) {
						byte[] buff = new byte[4096];
						read = 1;
						String buffer2 = "";
						while (true) {
							read = data.read( buff, read-1, 4096 );
							buffer2 += new String(buff, WebSocket.UTF8_CHARSET);
							
							if (data.available() <= 0)
								break;
						}
						output.write(buffer2.trim().toString().getBytes());
						//conn.send(buffer.trim());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}