package com.ramblingwood.minecraft.jsonapi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simpleForBukkit.JSONObject;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;
import com.ramblingwood.minecraft.jsonapi.JSONServer;
import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStream;
import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class StreamingResponse extends InputStream {
	private List<JSONAPIStream> stacks = new ArrayList<JSONAPIStream>();
	private List<Integer> positions = new ArrayList<Integer>();
	private List<String> sourceLists = new ArrayList<String>();
	private JSONAPI plugin;
	private String callback;
	
	public StreamingResponse(JSONAPI _plugin, List<String> sourceLists, String callback, boolean showOlder) {
		plugin = _plugin;
		
		this.sourceLists = sourceLists;

		for(String s : sourceLists) {
			if(plugin.getStreamManager().streamExists(s)) {
				stacks.add(plugin.getStreamManager().getStream(s));
			}
			else {
				plugin.outLog.warning("The requested stream: '"+s+"' does not exist.");
			}
		}
		
		for(JSONAPIStream s : stacks) {
			positions.add(showOlder ? (s.getStack().size() > 50 ? s.getStack().size() - 50 : 0) : s.getStack().size());
		}
	}
	
	public String nextLine () {
		while(true) {
			for(int i = 0; i < stacks.size(); i++) {
				List<JSONAPIStreamMessage> stack = stacks.get(i).getStack();
				
				synchronized(stack) {
					Integer pos = positions.get(i);
				
					if(pos >= stack.size()) {
						continue;
					}
					else {
						String res = JSONServer.callback(callback, makeResponseObj(stack.get(pos), i)).concat("\r\n");
					
						positions.set(i, pos+1);
					
						return res;
					}
				}
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String makeResponseObj (JSONAPIStreamMessage ja, Integer pos) {
		JSONObject o = new JSONObject();
		o.put("result", "success");
		o.put("source", sourceLists.get(pos));
		o.put("success", ja);
		
		String ret = o.toJSONString();
		return ret;
	}
	
	public int read() throws IOException {
		return -1;
	}
}
