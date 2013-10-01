package com.alecgorge.minecraft.jsonapi.packets;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import net.minecraft.server.v1_6_R2.Packet;

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
				System.out.println(String.format("[JSONAPI] Injected packet: 0x%X", packetID));
			} catch (Exception e) {
				e.printStackTrace();
				// just smoosh the error, we are probably reloading
			}
		}
	}
	
	static Method findRegistrationMethod() {
		Class<Packet> p = Packet.class;
		Method m = null;
		Class<?> validTypes[] = new Class<?>[] { Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Class.class };

		try {
			m = p.getDeclaredMethod("a", validTypes);
			m.setAccessible(true);
			
			return m;
		} catch (Exception e) {
			try {
				for(Method mm : p.getDeclaredMethods()) {
					boolean valid = mm.getParameterTypes().length == 4;
					
					if(!valid) continue;
					
					for(int i = 0; i < mm.getParameterTypes().length; i++) {
						if(!validTypes[i].equals(mm.getParameterTypes()[i])) {
							valid = false;
							break;
						}
					}
					
					if(valid) {
						System.out.println("[JSONAPI] Found packet registration method through heuristics: " + mm.getName());
						mm.setAccessible(true);
						return mm;
					}
				}
			}
			catch(Exception sube) {
				sube.printStackTrace();
			}
		}
		
		return m;
	}
	
	public static void register(int packetID, Class<? extends Packet> packetClass) {
		register(packetID, false, true, packetClass);
	}
}
