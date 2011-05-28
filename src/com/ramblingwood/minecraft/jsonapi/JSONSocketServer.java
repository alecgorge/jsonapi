package com.ramblingwood.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import com.ramblingwood.minecraft.jsonapi.streams.StreamingResponse;

public class JSONSocketServer implements Runnable{
	protected int			serverPort		= 20060;
	protected ServerSocket	serverSocket	= null;
	protected JSONServer	jsonServer		= null;
	protected boolean		isStopped		= true;
	protected Thread		runningThread	= null;

	public JSONSocketServer(int port, JSONServer jsonServer){
		this.serverPort = port;
		this.jsonServer = jsonServer;
		start();
	}

	public void run(){
		synchronized(this){
			this.runningThread = Thread.currentThread();
			isStopped = false;
		}
		
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
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

	public synchronized void start(){
		(new Thread(this)).start();
	}
	
	public class WorkerRunnable implements Runnable{

		protected Socket clientSocket = null;
		protected String serverText	 = null;

		public WorkerRunnable(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {
				String line2 = "";
				final BufferedReader input	= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				final DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

				while((line2 = input.readLine()) != null) {
					// format method=xyz&args=[]&username=user&password=pass
					String[] split = line2.split("\\?", 2);
					
					NanoHTTPD.Response r = null;
					if(split.length < 2) {
						r = jsonServer.new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
					}
					else {
						Properties header = new Properties();
						NanoHTTPD.decodeParms(split[1], header);
						Properties p = new Properties();
						p.put("X-REMOTE-ADDR", clientSocket.getInetAddress().getHostAddress());
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
										if(clientSocket.isConnected() && !clientSocket.isClosed()) {
											output.writeBytes(line.trim()+"\r\n");
										}
										else {
											continueSending = false;
										}
									} catch (IOException e) {
										//e.printStackTrace();
										continueSending = false;
										try {
											clientSocket.close();
											output.close();
											input.close();
										} catch (IOException e1) {
											e1.printStackTrace();
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
								output.writeUTF(line+"\r\n");
							}
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				}
				output.close();
				input.close();
			} catch (IOException e) {
				try {
					clientSocket.close();
				} catch (IOException e1) {
				}
				
				//e.printStackTrace();
			}
		}
	}
}