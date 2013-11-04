package com.alecgorge.minecraft.jsonapi.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.Connection;
//$import net.minecraft.server./*$mcversion$*/.Packet;
//#else
import net.minecraft.server.v1_6_R3.Connection;
import net.minecraft.server.v1_6_R3.Packet;
//#endif

public class PacketStringResponse extends Packet {
	InputStream payload = null;
	byte[] stringPayload = null;
	boolean finalPacket;
	
	public PacketStringResponse(InputStream s, boolean fin) {
		super();
		try {
			setPacketID(s.read());
		} catch (IOException e) {
			e.printStackTrace();
		}
		finalPacket = fin;
		payload = s;
	}
	
	public PacketStringResponse(String s, boolean fin) {
		try {
			stringPayload = s.getBytes("UTF-8");
			setPacketID(stringPayload[0]);
			finalPacket = false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void setPacketID(int id) {
		try {
			Field f = getClass().getSuperclass().getDeclaredField("packetID");
			f.setAccessible(true);
			f.set(this, id);
		}
		catch(Throwable t) {
			for(Field f : getClass().getSuperclass().getDeclaredFields()) {
				if(f.getType().equals(Integer.TYPE)) {
					f.setAccessible(true);
					try {
						f.set(this, id);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
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
		if(stringPayload != null) {
			// skip the first byte, that is the packet ID
			output.write(stringPayload, 1, stringPayload.length - 1);
			
			return;
		}
		
		byte[] buf = new byte[8196];
		int len = -1;
		while((len = payload.read(buf)) >= 0) {
			output.write(buf, 0, len);
		}
		
		output.write("\r\n".getBytes(Charset.forName("UTF-8")));
		
		if(finalPacket) {
//			output.flush();
//			output.close();
		}
	}

	@Override
	public void handle(Connection net) {
		
	}

}
