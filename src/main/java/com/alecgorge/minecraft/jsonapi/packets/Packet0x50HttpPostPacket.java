package com.alecgorge.minecraft.jsonapi.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.Connection;
//$import net.minecraft.server./*$mcversion$*/.Packet;
//#else
import net.minecraft.server.v1_6_R3.Connection;
import net.minecraft.server.v1_6_R3.Packet;
//#endif

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;

public class Packet0x50HttpPostPacket extends Packet {
	RawPacket rawPacket;
	
//#if mc16OrNewer=="yes"
	@Override
	public void a(DataInput inp) {
//#else
//$ @Override
//$	public void a(DataInputStream inp) {
//#endif
		try {
			rawPacket = new RawPacket(inp);
			rawPacket.resetTimeout();
							
			JSONServer jsonServer = JSONAPI.instance.jsonServer;
			ByteArrayInputStream prefix = new ByteArrayInputStream(new byte[] { 'P' });
			jsonServer.new HTTPSession(new SequenceInputStream(prefix, rawPacket.getInputStream()), rawPacket.getOutputStream(), rawPacket.getAddr(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void handle(Connection arg0) {
		if(rawPacket != null) {
		}
	}

//#if mc16OrNewer=="yes"
	@Override
	public void a(DataOutput out) {
//#else
//$ @Override
//$	public void a(DataOutputStream out) {
//#endif
	}

	@Override
	public int a() {
		return 0;
	}
}
