package com.alecgorge.minecraft.jsonapi.packets;

public class PacketRegistrarLauncher {
	public PacketRegistrarLauncher() {
		PacketRegistrar.register(0x47, Packet0x47HttpGetPacket.class);
		PacketRegistrar.register(0x51, PacketStringResponse.class);
	}
}
