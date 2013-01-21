package com.alecgorge.minecraft.jsonapi.packets;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import net.minecraft.server.v1_4_R1.Packet;

public class PacketRegistrar {
	static Method registrationMethod = null;
	
	public static void register(int packetID, boolean isClientPacket, boolean isServerPacket, Class<? extends Packet> packetClass) {
		if(registrationMethod == null) {
			registrationMethod = findRegistrationMethod();			
		}
		
		// is it still null?
		if(registrationMethod == null) {
			Logger.getLogger("Minecraft").warning("Couldn't find the packet registration method!");
		}
		else {
			try {
				if(Packet.l.get(packetID) != null) {
					Packet.l.d(packetID); // remove the current packet registration if it exists
				}

				registrationMethod.invoke(null, new Object[] { packetID, isClientPacket, isServerPacket, packetClass });				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	static Method findRegistrationMethod() {
		try {
			Class<Packet> p = Packet.class;
			Method m = p.getDeclaredMethod("a", new Class<?>[] { Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Class.class });
			
			m.setAccessible(true);
			
			return m;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void register(int packetID, Class<? extends Packet> packetClass) {
		register(packetID, false, true, packetClass);
	}
}
