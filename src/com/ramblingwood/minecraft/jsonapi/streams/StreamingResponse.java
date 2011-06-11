package com.ramblingwood.minecraft.jsonapi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simpleForBukkit.JSONObject;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;
import com.ramblingwood.minecraft.jsonapi.JSONServer;

public class StreamingResponse extends InputStream {
	private String stack_name;
	private JSONAPI plugin;
	private String callback;
	private int pos = 0;
	
	public StreamingResponse(JSONAPI _plugin, String istack, String callback) {
		plugin = _plugin;
		stack_name = istack;
		
		pos = getStack().size() > 50 ? getStack().size() - 50 : 0;
	}
	
	public String nextLine () {
		List<? extends JSONAPIStream> stack = getStack();
		while(pos >= stack.size()) {
			try {
				Thread.sleep(250);
				stack = getStack();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		pos++;

		return JSONServer.callback(callback, makeResponseObj(stack.get(pos-1))).concat("\r\n");
	}
	
	private List<? extends JSONAPIStream> getStack () {
		List<? extends JSONAPIStream> stack = new ArrayList<JSONAPIStream>();
		if(stack_name.equals("chat")) stack = plugin.jsonServer.chat;
		else if(stack_name.equals("console")) stack = plugin.jsonServer.console;
		else if(stack_name.equals("connections")) stack = plugin.jsonServer.connections;
		else if(stack_name.equals("all")) stack = plugin.jsonServer.all;
		return stack;
	}
	
	private String makeResponseObj (JSONAPIStream ja) {
		JSONObject o = new JSONObject();
		o.put("result", "success");
		o.put("source", ja.getSourceName());
		o.put("success", ja);
		
		String ret = o.toJSONString();
		return ret;
	}
	
	public int read() throws IOException {
		return -1;
	}
}
