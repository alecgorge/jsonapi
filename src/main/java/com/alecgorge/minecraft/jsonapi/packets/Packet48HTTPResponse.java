package com.alecgorge.minecraft.jsonapi.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.server.v1_6_R1.Connection;
import net.minecraft.server.v1_6_R1.Packet;

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
	public void a(DataInput arg0) throws IOException {
		return;
	}

	@Override
	public void a(DataOutput output) throws IOException {
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
