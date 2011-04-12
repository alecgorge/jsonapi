package com.ramblingwood.minecraft.jsonapi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.JSONAware;

import com.ramblingwood.minecraft.jsonapi.JSONServer;

public class StreamingResponse extends InputStream {
	private ArrayList<? extends JSONAPIStream> stack;
	private String callback;
	private String type;
	private int pos = 0;
	
	public StreamingResponse(String type, ArrayList<? extends JSONAPIStream> arr, String callback) {
		// System.out.println(type);
		// System.out.println(arr);
		
		stack = arr;
		this.type = type;
	}
	
	public String nextLine () {
		while(pos >= stack.size()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		pos++;

		return JSONServer.callback(callback, makeResponseObj(stack.get(pos-1))).concat("\r\n");		
	}
	
	private String makeResponseObj (JSONAware ja) {
		JSONObject o = new JSONObject();
		o.put("result", "success");
		o.put("source", type);
		o.put("success", ja);
		
		String ret = o.toJSONString();
		return ret;
	}
	
	public int read() throws IOException {
		return -1;
	}
}
