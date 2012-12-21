package com.alecgorge.minecraft.jsonapi.packets;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Properties;

import net.minecraft.server.v1_4_5.NetHandler;
import net.minecraft.server.v1_4_5.NetLoginHandler;
import net.minecraft.server.v1_4_5.Packet;
import net.minecraft.server.v1_4_5.Packet254GetInfo;

import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class Packet251JSONAPI extends Packet254GetInfo {
	public String tag;
	public int length;
	public String data;

	static JSONAPI api;

	public Packet251JSONAPI() {}

	public static void register(JSONAPI _api) {
		try {
			api = _api;

			Class<Packet> p = Packet.class;
			Method m = p.getDeclaredMethod("a", new Class<?>[] { Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Class.class });
			
			m.setAccessible(true);
			
			// register a new Packet--this one
			m.invoke(null, new Object[] { 251, true, true, Packet251JSONAPI.class });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	public void a(DataInputStream paramDataInputStream) {
		try {
			this.length = paramDataInputStream.readShort();

			if ((this.length > 0) && (this.length < Short.MAX_VALUE)) {
				byte[] b = new byte[this.length];
				paramDataInputStream.readFully(b);

				this.data = new String(b);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Abstract. Writes the raw packet data to the data stream.
	 */
	public void a(DataOutputStream paramDataOutputStream) {}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void handle(NetHandler paramNetHandler) {
		System.out.println("handlin': " + data);
		try {
			NetLoginHandler h = (NetLoginHandler) paramNetHandler;
			final Socket s = h.networkManager.getSocket();
			s.setSoTimeout(0); // INFINITE TIME
			
			final JSONServer jsonServer = api.getJSONServer();

			final DataOutputStream output = new DataOutputStream(s.getOutputStream());
			
			Thread t = new Thread(new Runnable() {
				public void run() {
					String[] split = data.split("\\?", 2);
					
					NanoHTTPD.Response r = null;
					if(split.length < 2) {
						r = jsonServer.new Response( NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
					}
					else {
						Properties header = new Properties();
						NanoHTTPD.decodeParms(split[1], header);
						Properties p = new Properties();
						p.put("X-REMOTE-ADDR", s.getInetAddress().getHostAddress());
						r = jsonServer.serve(split[0], "GET", p, header);
					}

					if (r.data instanceof StreamingResponse) {
						final StreamingResponse stream = (StreamingResponse) r.data;
						String line = "";
						boolean continueSending = true;

						while ((line = stream.nextLine()) != null && continueSending) {
							try {
								if (s.isConnected() && !s.isClosed()) {
									output.write((line.trim() + "\r\n").getBytes("UTF-8"));
								} else {
									continueSending = false;
								}
							} catch (IOException e) {
								// e.printStackTrace();
								continueSending = false;
								try {
									s.close();
									output.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					} else {
						BufferedReader data = new BufferedReader(new InputStreamReader(r.data));

						try {
							String line = "";

							while ((line = data.readLine()) != null) {
								output.write((line + "\r\n").getBytes("UTF-8"));
							}

							data.close();
							output.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			});
			t.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Abstract. Return the size of the packet (not counting the header).
	 */
	public int a() {
		return 2 + 2 + this.length;
	}
}
