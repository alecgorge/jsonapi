package com.alecgorge.minecraft.jsonapi.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.Connection;
//$import net.minecraft.server./*$mcversion$*/.Packet60Explosion;
//#else
import net.minecraft.server.v1_6_R3.Connection;
import net.minecraft.server.v1_6_R3.Packet60Explosion;
//#endif

public class Packet0x3CFlashPolicy extends Packet60Explosion {
	final static String flashSocketPolicy = "<?xml version=\"1.0\"?>\n" + 
											"<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">\n" + 
											"<cross-domain-policy>\n" + 
											"<allow-access-from domain=\"*\" to-ports=\"*\"/>\n" + 
											"</cross-domain-policy>";
	
	final static byte[] flashSocketPolicyBytes = flashSocketPolicy.getBytes(Charset.forName("UTF-8"));
	
	RawPacket rawPacket;
	
//#if mc16OrNewer=="yes"
	@Override
	public void a(DataInput inp) {
//#else
//$ @Override
//$	public void a(DataInputStream inp) {
//#endif
		try {
			final byte next = inp.readByte();
			if (next == 'p') {
				// it is very likely that this is going to be <policy-file-request/>
				rawPacket = new RawPacket(inp);
				rawPacket.resetTimeout();
				
				rawPacket.getOutputStream().write(flashSocketPolicyBytes);
				rawPacket.getOutputStream().flush();
				
				rawPacket.getSocket().close();
			} else {
				ByteArrayInputStream prefix = new ByteArrayInputStream(new byte[] { '<', next });
				super.a(new DataInputStream(new SequenceInputStream(prefix, (DataInputStream) inp)));
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void handle(Connection arg0) {
		if(rawPacket != null) {
			return;
		}
		
		super.handle(arg0);
	}
}
