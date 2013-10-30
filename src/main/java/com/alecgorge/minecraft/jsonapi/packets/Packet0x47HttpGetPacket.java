package com.alecgorge.minecraft.jsonapi.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.Socket;
import java.util.Properties;

import net.minecraft.server.v1_6_R3.Connection;
import net.minecraft.server.v1_6_R3.Packet71Weather;
import net.minecraft.server.v1_6_R3.PendingConnection;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class Packet0x47HttpGetPacket extends Packet71Weather {
	ByteArrayInputStream payload = null;

	boolean isHTTP = true;
	boolean isStream = false;
	boolean isWebsocket = false;
	boolean isKeepAlive = false;
	
	boolean isGET = true;

	String streamLine = null;
	RawPacket rawPacket;
	
	boolean shouldDebug = false;
	void dbug(Object objects) {
		if(shouldDebug) {
			System.out.println(objects);
		}
	}
	
	public void a(DataInput inp) {
		try {
			// System.out.println("attempting to read packet");
			StringBuilder builder = new StringBuilder();

			final byte next = inp.readByte();
			dbug("next: " + (char)next);
			if (next == 'E') {
				rawPacket = new RawPacket(inp);
				rawPacket.resetTimeout();
								
				JSONServer jsonServer = JSONAPI.instance.jsonServer;
				ByteArrayInputStream prefix = new ByteArrayInputStream(new byte[] { 'G', 'E' });
				jsonServer.new HTTPSession(new SequenceInputStream(prefix, rawPacket.getInputStream()), rawPacket.getOutputStream(), rawPacket.getAddr(), true);
			} else if (next == 'S') {
				isHTTP = false;
				isStream = true;

				if (inp.readByte() == 'A') {
					dbug("Keep Alive!");
					isKeepAlive = true;
					inp.readLine();
					return;
				}
				dbug("Stream!");

				streamLine = inp.readLine();
				
				dbug(streamLine);
				return;
			} else {
				isGET = false;
				ByteArrayInputStream prefix = new ByteArrayInputStream(new byte[] {'G', next});
				super.a(new DataInputStream(new SequenceInputStream(prefix, (DataInputStream)inp)));
				return;
			}

			payload = new ByteArrayInputStream(builder.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void a(DataOutput out) {
		if(!isGET) {
			super.a(out);
		}
	}
	
	public void handle(Connection net) {
		if(!isGET) {
			super.handle(net);
			return;
		}
		final PendingConnection loginHandler = (PendingConnection) net;

		try {
			loginHandler.getSocket().setTcpNoDelay(true);

			RawPacket.resetTimeout(loginHandler);

			if (isKeepAlive) {
				return;
			}

			if (isStream) {
				final Socket clientSocket = loginHandler.getSocket();

				(new Thread(new Runnable() {
					@Override
					public void run() {
						JSONServer jsonServer = JSONAPI.instance.getJSONServer();
						String[] split = streamLine.split("\\?", 2);

						NanoHTTPD.Response r = null;
						if (split.length < 2) {
							r = jsonServer.new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
						} else {
							Properties header = new Properties();
							NanoHTTPD.decodeParms(split[1], header);
							Properties p = new Properties();
							p.put("X-REMOTE-ADDR", clientSocket.getInetAddress().getHostAddress());
							r = jsonServer.serve(split[0], "GET", p, header);
						}

						if (r.data instanceof StreamingResponse) {
							final StreamingResponse s = (StreamingResponse) r.data;
							String line = "";
							boolean continueSending = true;

							while ((line = s.nextLine()) != null && continueSending) {
								RawPacket.sendPacket(new PacketStringResponse(line + "\r\n", false), loginHandler, isHTTP);
								RawPacket.resetTimeout(loginHandler);
								continueSending = !clientSocket.isClosed();
							}
							
							RawPacket.sendPacket(new PacketStringResponse("\r\n", true), loginHandler, isHTTP);
							
							try {
								((StreamingResponse)r.data).close();
								Thread.currentThread().interrupt();
								Thread.currentThread().join();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
							
						} else {
							InputStream res = r.data;
							if(res == null) {
								res = new ByteArrayInputStream(r.bytes);
							}
							RawPacket.sendPacket(new PacketStringResponse(res, false), loginHandler, isHTTP);
						}
					}
				})).start();
				
				return;
			}

			ByteArrayOutputStream oup = new ByteArrayOutputStream();
			JSONAPI.instance.getJSONServer().new HTTPSession(payload, oup, loginHandler.getSocket().getInetAddress(), new Lambda<Void, OutputStream>() {
				public Void execute(OutputStream x) {
					try {
						ByteArrayOutputStream o = (ByteArrayOutputStream) x;
						ByteArrayInputStream inp = new ByteArrayInputStream(o.toByteArray());
						PacketStringResponse packet = new PacketStringResponse(inp, true);

						RawPacket.sendPacket(packet, loginHandler, isHTTP);
					} catch (Exception e) {
						e.printStackTrace();
					}

					return null;
				}
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public int a() {
		return 0;
	}
}
