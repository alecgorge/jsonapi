package com.alecgorge.minecraft.jsonapi.packets;

public class PacketRegistrarLauncher {
	public PacketRegistrarLauncher() {
		PacketRegistrar.register(0x47, Packet0x47HttpGetPacket.class);
		PacketRegistrar.register(0x4F, Packet0x4FHttpOptionsPacket.class);
		PacketRegistrar.register(0x50, Packet0x50HttpPostPacket.class);
		PacketRegistrar.register(0x51, PacketStringResponse.class);
		PacketRegistrar.register(0x3C, Packet0x3CFlashPolicy.class);
	}
}
