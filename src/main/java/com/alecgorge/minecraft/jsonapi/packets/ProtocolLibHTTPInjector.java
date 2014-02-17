package com.alecgorge.minecraft.jsonapi.packets;
//#if mc17OrNewer=="yes"

import java.util.Map;

import net.minecraft.server.v1_7_R1.EnumProtocol;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.accessors.Accessors;

//#endif
public class ProtocolLibHTTPInjector {
	//#if mc17OrNewer=="yes"

	ProtocolManager	lib	= ProtocolLibrary.getProtocolManager();
	PacketType		getPacket;
	JSONAPI			api;

	@SuppressWarnings("unchecked")
	public ProtocolLibHTTPInjector(JSONAPI api) {
		this.api = api;

		try {
			for(Object k : EnumProtocol.HANDSHAKING.a().keySet()) {
				System.out.println("key: " + k + "; value="+EnumProtocol.HANDSHAKING.a().get(k));
			}
			
			EnumProtocol.HANDSHAKING.a().put(0x47, Packet0x47HttpGetPacket.class);
	        Map<Class<?>, EnumProtocol> map = (Map<Class<?>, EnumProtocol>) 
	                Accessors.getFieldAccessor(EnumProtocol.class, Map.class, true).get(EnumProtocol.HANDSHAKING);

	        map.put(Packet0x47HttpGetPacket.class, EnumProtocol.HANDSHAKING);

			for(Object k : EnumProtocol.HANDSHAKING.a().keySet()) {
				System.out.println("key: " + k + "; value="+EnumProtocol.HANDSHAKING.a().get(k));
			}

			getPacket = new PacketType(Protocol.HANDSHAKING, Sender.CLIENT, 0x47, -1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void inject() {

		lib.addPacketListener(new PacketAdapter(api, getPacket) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				JSONAPI.dbug(e.getPacketID());
				JSONAPI.dbug(e.getPacketType());
				JSONAPI.dbug(e.getPacket().getStrings());
			}
			
			@Override
		    public void onPacketSending(PacketEvent event) {
		        System.out.println("Impossible!");
		    }
		});
	}
	
	//#endif
}
