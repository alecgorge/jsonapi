package com.alecgorge.minecraft.jsonapi.config;

import java.util.ArrayList;
import java.util.List;

public class JSONAPIPermissionNode extends ConfigObject {
	public List<String> methods = new ArrayList<String>();
	public List<String> streams = new ArrayList<String>();
	private String name;
	
	public String getName() {
		return name;
	}
	
	public JSONAPIPermissionNode setName(String name) {
		this.name = name;
		return this;
	}
	
	public JSONAPIPermissionNode(String name) {
		this.name = name;
	}
	
	public List<String> getMethods() {
		return methods;
	}
	
	public List<String> getStreams() {
		return streams;
	}
	
	public boolean canUseMethod(String method) {
		if(method.startsWith("jsonapi.")) {
			return true;
		}

		return methods.contains(method);
	}

	public boolean canUseStream(String stream) {
		return streams.contains(stream);
	}
}
