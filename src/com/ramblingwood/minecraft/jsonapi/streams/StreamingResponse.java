package com.ramblingwood.minecraft.jsonapi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.ramblingwood.minecraft.jsonapi.JSONServer;

public class StreamingResponse extends InputStream {
	private ArrayList<? extends JSONAPIStream> stack;
	private String callback;
	private int pos = 0;
	
	public StreamingResponse(ArrayList<? extends JSONAPIStream> arr, String callback) {
		stack = arr;
	}
	
	public String nextLine () {
		while(pos >= stack.size()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return JSONServer.callback(callback, stack.get(pos).toJSONString()).concat("\r\n");		
	}
	
	public int read() throws IOException {
		return -1;
	}
}
