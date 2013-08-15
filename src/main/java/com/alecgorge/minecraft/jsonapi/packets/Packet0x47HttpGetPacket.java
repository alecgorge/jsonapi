package com.alecgorge.minecraft.jsonapi.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Properties;

import net.minecraft.server.v1_6_R2.Connection;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.Packet71Weather;
import net.minecraft.server.v1_6_R2.PendingConnection;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class Packet0x47HttpGetPacket extends Packet71Weather {
	ByteArrayInputStream payload = null;

	boolean isHTTP = true;
	boolean isStream = false;
	boolean isKeepAlive = false;
	
	boolean isGET = true;

	String streamLine = null;
	
	boolean shouldDebug = false;
	void dbug(Object objects) {
		if(shouldDebug) {
			System.out.println(objects);
		}
	}

	public void a(DataInput inp) {
		try {
			// System.out.println("attempting to read packet");
			String thisLine = "";
			StringBuilder builder = new StringBuilder();

			final byte next = inp.readByte();
			dbug("next: " + (char)next);
			if (next == 'E') {
				builder.append('G').append(next);
				while ((thisLine = inp.readLine()) != null) {
					dbug("got: "+thisLine);
					builder.append(thisLine).append("\r\n");

					if (thisLine.isEmpty()) {
						break;
					}
				}

				builder.append("\r\n");
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
				final DataInput wrappedInput = inp;
				super.a(new DataInputStream(new InputStream() {
					boolean hasReadFirst = false;
					
					@Override
					public int read() throws IOException {
						return hasReadFirst ? wrappedInput.readInt() : next;
					}
				}));
				return;
				// System.err.println("Uh oh. Protocol error! Encountered: " + next);
			}

			payload = new ByteArrayInputStream(builder.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Field weirdIterator = null;

	// this prevents the stupid "took too long to log in" message
	private void resetTimeout(PendingConnection loginHandler) {
		try {
			if (weirdIterator == null) {
				for (Field g : PendingConnection.class.getDeclaredFields()) {
					if (g.getType().getName().equals("int")) {
						weirdIterator = g;
						break;
					}
				}
				weirdIterator.setAccessible(true);
			}

			weirdIterator.set(loginHandler, 0); // it checks if g++ == 600,
												// spigot checks for 6000
		} catch (Throwable e) {
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

			resetTimeout(loginHandler);

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
								sendPacket(new PacketStringResponse(line + "\r\n", false), loginHandler);
								resetTimeout(loginHandler);
								continueSending = !clientSocket.isClosed();
							}
							
							sendPacket(new PacketStringResponse("\r\n", true), loginHandler);
							
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
							sendPacket(new PacketStringResponse(res, false), loginHandler);
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

						sendPacket(packet, loginHandler);
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

	private void sendPacket(Packet packet, PendingConnection loginHandler) {
		try {
			Field networkManagerField = null;
			for (Field f : loginHandler.getClass().getDeclaredFields()) {
				if (f.getType().isAssignableFrom(NetworkManager.class)) {
					networkManagerField = f;
					networkManagerField.setAccessible(true);
					break;
				}
			}

			Object networkManager = networkManagerField.get(loginHandler);
			try {
				networkManager.getClass().getDeclaredMethod("queue", Packet.class).invoke(networkManager, packet);
				if (isHTTP) {
					networkManager.getClass().getDeclaredMethod("d").invoke(networkManager);
				}
			} catch (Exception e) {
				networkManager.getClass().getDeclaredMethod("func_74429_a", Packet.class).invoke(networkManager, packet);
				if (isHTTP) {
					networkManager.getClass().getDeclaredMethod("func_74423_d").invoke(networkManager);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public int a() {
		return 0;
	}
}
