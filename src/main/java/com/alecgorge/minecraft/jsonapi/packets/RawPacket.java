package com.alecgorge.minecraft.jsonapi.packets;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_6_R2.DedicatedServerConnection;
import net.minecraft.server.v1_6_R2.DedicatedServerConnectionThread;
import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.PendingConnection;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;

// use nasty reflection to capture the raw input and output streams while doing the initial read
// from the packet stream
public class RawPacket {
	Field throttleHashMapField = null;
	Field throttleDedicatedServerConnectionThreadField = null;
	Field dedicatedServerConnectThreadPendingConnectionList = null;
	Field networkManagerDataInputStream = null;
	Field networkManagerDataOutputStream = null;
	Field networkManagerMagicDisconnectFlag = null;
	
	DedicatedServerConnectionThread thread = null;
	
	protected DataInputStream inputStream;
	protected DataOutputStream outputStream;
	protected InetAddress addr;
	protected PendingConnection pendingConnection;
	
	public RawPacket(DataInput inp) {
		resetThrottleAndFindIOStreams(inp);
	}

	public DataInputStream getInputStream() {
		return inputStream;
	}

	public DataOutputStream getOutputStream() {
		return outputStream;
	}

	public InetAddress getAddr() {
		return addr;
	}

	public PendingConnection getPendingConnection() {
		return pendingConnection;
	}
	
	public Socket getSocket() {
		return getPendingConnection().networkManager.getSocket();
	}

	@SuppressWarnings("unchecked")
	protected void resetThrottleAndFindIOStreams(DataInput inp) {
		try {
			if (throttleHashMapField == null) {
				throttleDedicatedServerConnectionThreadField = DedicatedServerConnection.class.getDeclaredField("b");
				throttleDedicatedServerConnectionThreadField.setAccessible(true);
				
				throttleHashMapField = DedicatedServerConnectionThread.class.getDeclaredField("b");
				throttleHashMapField.setAccessible(true);
				
				dedicatedServerConnectThreadPendingConnectionList = DedicatedServerConnectionThread.class.getDeclaredField("a");
				dedicatedServerConnectThreadPendingConnectionList.setAccessible(true);
				
				networkManagerDataInputStream = NetworkManager.class.getDeclaredField("input");
				networkManagerDataInputStream.setAccessible(true);
				
				networkManagerDataOutputStream = NetworkManager.class.getDeclaredField("output");
				networkManagerDataOutputStream.setAccessible(true);
				
				networkManagerMagicDisconnectFlag = NetworkManager.class.getDeclaredField("n");
				networkManagerMagicDisconnectFlag.setAccessible(true);
			}
			
			MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
			DedicatedServerConnection conn = (DedicatedServerConnection)server.ag();
			thread = (DedicatedServerConnectionThread)throttleDedicatedServerConnectionThreadField.get(conn);
			
			HashMap<Object, Object> o = (HashMap<Object,Object>)throttleHashMapField.get(thread);
			
			synchronized (o) {
				o.clear();
			}
			
			if(inputStream == null || outputStream == null) {
				List<PendingConnection> pendingConnections = (List<PendingConnection>) dedicatedServerConnectThreadPendingConnectionList.get(thread);
				for(PendingConnection pcon : pendingConnections) {
					DataInputStream stream = (DataInputStream) networkManagerDataInputStream.get(pcon.networkManager);
					
					if(stream == inp) {
						// FOUND A MATCH.
						// ALL YOUR RAW IO STREAMS ARE BELONG TO US
						outputStream = (DataOutputStream) networkManagerDataOutputStream.get(pcon.networkManager);
						inputStream = stream;
						addr = pcon.networkManager.socket.getInetAddress();
						pendingConnection = pcon;
						
						// don't allow public void NetworkManager.a(String s, Object[] aobject) to close the socket
						networkManagerMagicDisconnectFlag.set(pcon.networkManager, false);
						
						break;
					}
				}
			}
			
			getPendingConnection().networkManager.socket.setSoTimeout(0);
			getPendingConnection().networkManager.socket.setTcpNoDelay(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// this prevents the stupid "took too long to log in" message
	public void resetTimeout() {
		RawPacket.resetTimeout(getPendingConnection());
	}

	private static Field weirdIterator = null;

	// this prevents the stupid "took too long to log in" message
	public static void resetTimeout(PendingConnection loginHandler) {
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

			weirdIterator.set(loginHandler, Integer.MIN_VALUE); // it checks if g++ == 600,
																// spigot checks for 6000
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void sendPacket(Packet packet, PendingConnection loginHandler, boolean isFinalPacket) {
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
				if (isFinalPacket) {
					networkManager.getClass().getDeclaredMethod("d").invoke(networkManager);
				}
			} catch (Exception e) {
				networkManager.getClass().getDeclaredMethod("func_74429_a", Packet.class).invoke(networkManager, packet);
				if (isFinalPacket) {
					networkManager.getClass().getDeclaredMethod("func_74423_d").invoke(networkManager);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
