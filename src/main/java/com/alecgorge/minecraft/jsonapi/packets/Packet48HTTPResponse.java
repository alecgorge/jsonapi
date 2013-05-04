package com.alecgorge.minecraft.jsonapi.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.server.v1_5_R3.Connection;
import net.minecraft.server.v1_5_R3.Packet;

public class Packet48HTTPResponse extends Packet {
	InputStream payload = null;
	
	public Packet48HTTPResponse(InputStream s) {
		super();
		payload = s;
	}
	
	@Override
	public int a() {
		return 0;
	}

	@Override
	public void a(DataInputStream arg0) throws IOException {
		return;
	}

	@Override
	public void a(DataOutputStream output) throws IOException {
		byte[] buf = new byte[8196];
		payload.read(); // read off the first H.
		while(payload.read(buf) >= 0) {
			output.write(buf, 0, buf.length);
		}
	}

	@Override
	public void handle(Connection arg0) {
	}

}
