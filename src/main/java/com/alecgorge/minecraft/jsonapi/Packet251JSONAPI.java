package com.alecgorge.minecraft.jsonapi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.xml.bind.DatatypeConverter;

import net.minecraft.server.v1_4_5.NetHandler;
import net.minecraft.server.v1_4_5.Packet;
import net.minecraft.server.v1_4_5.Packet250CustomPayload;

public class Packet251JSONAPI extends Packet {
	public String tag;
	public int length;
	public byte[] data;

	static JSONAPI api;

	public Packet251JSONAPI() {
	}

	public Packet251JSONAPI(String paramString, byte[] paramArrayOfByte) {
		this.tag = paramString;
		this.data = paramArrayOfByte;

		if (paramArrayOfByte != null) {
			this.length = paramArrayOfByte.length;

			if (this.length > 32767)
				throw new IllegalArgumentException("Payload may not be larger than 32k");
		}
	}

	public static void register(JSONAPI _api) {
		try {
			api = _api;

			Class<Packet> p = Packet.class;
			Method m = p.getDeclaredMethod("a", new Class<?>[] { Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Class.class });
			
			m.setAccessible(true);
			
			// register a new Packet--this one
			m.invoke(null, new Object[] { 251, true, true, Packet251JSONAPI.class });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	public void a(DataInputStream paramDataInputStream) {
		try {
			// this.tag = a(paramDataInputStream, 20);
			this.length = paramDataInputStream.readShort();

			System.out.println(this.length);

			if ((this.length > 0) && (this.length < Short.MAX_VALUE)) {
				this.data = new byte[this.length];
				paramDataInputStream.readFully(this.data);

				System.out.println(DatatypeConverter.printHexBinary(this.data));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Abstract. Writes the raw packet data to the data stream.
	 */
	public void a(DataOutputStream paramDataOutputStream) {
		try {
			a(this.tag, paramDataOutputStream);
			paramDataOutputStream.writeShort((short) this.length);
			if (this.data != null)
				paramDataOutputStream.write(this.data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void handle(NetHandler paramNetHandler) {
		// this doesn't seem to do anything?
		// the body of a(Packet250CustomPayload) is empty
		paramNetHandler.a((Packet250CustomPayload) null);
	}

	/**
	 * Abstract. Return the size of the packet (not counting the header).
	 */
	public int a() {
		return 2 + this.tag.length() * 2 + 2 + this.length;
	}
}
