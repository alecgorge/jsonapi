package com.alecgorge.minecraft.jsonapi.packets;
//#if mc17OrNewer=="yes"

import java.io.IOException;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.Packet;
//$import net.minecraft.server./*$mcversion$*/.PacketDataSerializer;
//$import net.minecraft.server./*$mcversion$*/.PacketListener;
//#else
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketDataSerializer;
import net.minecraft.server.v1_7_R1.PacketListener;
//#endif

//#endif
public class Packet0x47HttpGetPacket
//#if mc17OrNewer=="yes"
extends Packet
//#endif
{
	//#if mc17OrNewer=="yes"
	@Override
	public void a(PacketDataSerializer ser) throws IOException {
		System.out.println("Sending/receiving packet. a");
	}

	@Override
	public void b(PacketDataSerializer ser) throws IOException {
		 System.out.println("Sending/receiving packet. b");
	}

	@Override
	public void handle(PacketListener listener) {
		System.out.println("Handling custom packet.");
	}
	//#endif
}
