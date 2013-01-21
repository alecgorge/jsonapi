package com.alecgorge.minecraft.jsonapi.packets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;

import net.minecraft.server.v1_4_R1.Connection;
import net.minecraft.server.v1_4_R1.DedicatedServerConnection;
import net.minecraft.server.v1_4_R1.MinecraftServer;
import net.minecraft.server.v1_4_R1.Packet71Weather;
import net.minecraft.server.v1_4_R1.PendingConnection;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class Packet71WeatherProxy extends Packet71Weather {
	boolean isGetRequest = true;
	BufferedInputStream inputStream;
	ByteArrayInputStream payload = null;
	Socket lastSocket = null;

	public Packet71WeatherProxy() {	}

	public void a(DataInputStream inp) {
		try {
			if(lastSocket != null) {
				lastSocket.setSoTimeout(0);
			}
			
			inputStream = new BufferedInputStream(inp);
			inputStream.mark(100000);

			byte[] httpTest = new byte[6];
			inputStream.read(httpTest);

			byte[] getSpaceSlash = new byte[] {
			/* (byte) 0x47 */// this byte doesn't count. it is the "packet id"
					(byte) 0x45, (byte) 0x54, (byte) 0x20, (byte) 0x2F };

			for (int i = 0; i < getSpaceSlash.length; i++) {
				if (httpTest[i] != getSpaceSlash[i]) {
					isGetRequest = false;
				}
			}

			inputStream.reset();
			if (isGetRequest) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int length = 0;

				baos.write("G".getBytes("UTF-8"));
				while ((length = inputStream.read(buffer)) != -1) {
					baos.write(buffer, 0, length);

					// only GET requests are supported. look for end of headers
					byte last = buffer[length - 1];
					byte secondToLast = buffer[length - 2];
					byte thirdToLast = buffer[length - 3];
					byte fourthToLast = buffer[length - 4];

					if (last == (byte) 0x0A && secondToLast == (byte) 0x0D && thirdToLast == (byte) 0x0A && fourthToLast == (byte) 0x0D) {
						break;
					}
				}

				payload = new ByteArrayInputStream(baos.toByteArray());
				baos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void a(DataOutputStream out) {
		if (!isGetRequest) {
			super.a(out);
		}
	}

	public void handle(Connection net) {
		if (!isGetRequest) {
			super.handle(net);
		}

		final PendingConnection loginHandler = (PendingConnection) net;

		try {
			lastSocket = loginHandler.getSocket();
			
			loginHandler.getSocket().shutdownInput();
			loginHandler.getSocket().setSoTimeout(0);
			loginHandler.getSocket().setTcpNoDelay(true);
			
			Field g = PendingConnection.class.getDeclaredField("g");
			g.setAccessible(true);
			
			g.set(loginHandler, 601); // it checks if g++ == 600. 

			JSONAPI.instance.getJSONServer().new HTTPSession(payload, loginHandler.getSocket().getOutputStream(), loginHandler.getSocket().getInetAddress(), new Lambda<Void, Void>() {
				public Void execute(Void x) {					
					// i don't know what this does, but all the cool packets set it when they are done...
					// notably 254
					loginHandler.networkManager.d();
					
					try {
						Field server = PendingConnection.class.getDeclaredField("server");
						server.setAccessible(true);
						
						MinecraftServer conn = (MinecraftServer) server.get(loginHandler);
						
						if(conn.ae() != null && loginHandler.getSocket() != null) {
							((DedicatedServerConnection)conn.ae()).a(loginHandler.getSocket().getInetAddress());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					loginHandler.c = true;
					
					return null;
				}
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public int a() {
		if (!isGetRequest) {
			return super.a();
		}
		return 150;
	}
}
