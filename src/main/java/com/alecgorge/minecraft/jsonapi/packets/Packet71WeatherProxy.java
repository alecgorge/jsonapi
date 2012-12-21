package com.alecgorge.minecraft.jsonapi.packets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;

import net.minecraft.server.v1_4_5.NetHandler;
import net.minecraft.server.v1_4_5.NetLoginHandler;
import net.minecraft.server.v1_4_5.Packet71Weather;

public class Packet71WeatherProxy extends Packet71Weather {
	boolean isGetRequest = true;
	BufferedInputStream inputStream;
	ByteArrayInputStream payload = null;
	
	public Packet71WeatherProxy() {}

	public void a(DataInputStream inp) {
		try {
			inputStream = new BufferedInputStream(inp);
			inputStream.mark(100000);
			
			byte[] httpTest = new byte[6];
			inputStream.read(httpTest);
			
			byte[] getSpaceSlash = new byte[] {
				/* (byte) 0x47 */ // this byte doesn't count. it is the "packet id"
				(byte) 0x45, (byte) 0x54, (byte) 0x20, (byte) 0x2F
			};
			
			for(int i = 0; i < getSpaceSlash.length; i++) {
				if(httpTest[i] != getSpaceSlash[i]) {
					isGetRequest = false;
				}
			}
			
			inputStream.reset();
			if(isGetRequest) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    byte[] buffer = new byte[1024];
			    int length = 0;
			    
			    baos.write("G".getBytes("UTF-8"));
			    while ((length = inputStream.read(buffer)) != -1) {
			    	baos.write(buffer, 0, length);
			        
			        // only GET requests are supported. look for end of headers 
			        byte last = buffer[length - 1];
			        byte secondToLast = buffer[length - 2];
			        byte thirdToLast = buffer[length - 3];
			        byte fourthToLast = buffer[length - 4];
			        
			        if(last == (byte) 0x0A
			        && secondToLast == (byte) 0x0D
			        && thirdToLast == (byte) 0x0A
			        && fourthToLast == (byte) 0x0D
			        ) {
			        	break;
			        }
			    }
			    
			    payload = new ByteArrayInputStream(baos.toByteArray());
			    baos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void a(DataOutputStream out) {
		if(!isGetRequest) {
			super.a(out);
		}
	}

	public void handle(NetHandler net) {
		if (!isGetRequest) {
			super.handle(net);
		}

		final NetLoginHandler loginHandler = (NetLoginHandler) net;
		
		try {
			loginHandler.getSocket().shutdownInput();
			loginHandler.getSocket().setSoTimeout(0);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		(new Thread(new Runnable() {
			public void run() {
				try {
					loginHandler.getSocket().setTcpNoDelay(true);
					
					JSONAPI.instance.getJSONServer().new HTTPSession(payload, loginHandler.getSocket().getOutputStream(), loginHandler.getSocket().getInetAddress());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		})).start();
	}

	public int a() {
		if(!isGetRequest) {
			return super.a();
		}
		return 17;
	}
}
