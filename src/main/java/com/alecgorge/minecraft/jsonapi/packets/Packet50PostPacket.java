package com.alecgorge.minecraft.jsonapi.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import net.minecraft.server.v1_5_R2.Connection;
import net.minecraft.server.v1_5_R2.NetworkManager;
import net.minecraft.server.v1_5_R2.Packet;
import net.minecraft.server.v1_5_R2.PendingConnection;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class Packet50PostPacket extends Packet {
	ByteArrayInputStream payload = null;

	@SuppressWarnings("deprecation")
	public void a(DataInputStream inp) {
		try {
			String thisLine = "";
			StringBuilder builder = new StringBuilder("P");
			while((thisLine = inp.readLine()) != null) {
				builder.append(thisLine).append("\r\n");
				
				if(thisLine.isEmpty()) {
					break;
				}				
			}
			
			builder.append("\r\n");
			
			payload = new ByteArrayInputStream(builder.toString().getBytes());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static Field weirdIterator = null;

	public void a(DataOutputStream out) {
	}

	public void handle(Connection net) {
		final PendingConnection loginHandler = (PendingConnection) net;		

		try {
			loginHandler.getSocket().setTcpNoDelay(true);
			
			if(weirdIterator == null) {
				for(Field g : PendingConnection.class.getDeclaredFields()) {
					if(g.getType().getName().equals("int")) {
						weirdIterator = g;
						break;
					}
				}
				weirdIterator.setAccessible(true);
			}
			
			weirdIterator.set(loginHandler, 10000); // it checks if g++ == 600, spigot checks for 6000
			
			ByteArrayOutputStream oup = new ByteArrayOutputStream();
			JSONAPI.instance.getJSONServer().new HTTPSession(payload, oup, loginHandler.getSocket().getInetAddress(), new Lambda<Void, OutputStream>() {
				public Void execute(OutputStream x) {
					try {
						ByteArrayOutputStream o = (ByteArrayOutputStream)x;
						
						Field networkManagerField = null;
						for(Field f : loginHandler.getClass().getDeclaredFields()) {
							if(f.getType().isAssignableFrom(NetworkManager.class)) {
								networkManagerField = f;
								networkManagerField.setAccessible(true);
								break;
							}
						}
						
						Object networkManager = networkManagerField.get(loginHandler);
						ByteArrayInputStream inp = new ByteArrayInputStream(o.toByteArray());
						Packet48HTTPResponse packet = new Packet48HTTPResponse(inp);
						try {
							networkManager.getClass().getDeclaredMethod("queue", Packet.class).invoke(networkManager, packet);
							networkManager.getClass().getDeclaredMethod("d").invoke(networkManager);
						}
						catch(Exception e) {
							networkManager.getClass().getDeclaredMethod("func_74429_a", Packet.class).invoke(networkManager, packet);
							networkManager.getClass().getDeclaredMethod("func_74423_d").invoke(networkManager);
						}
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
