package com.alecgorge.minecraft.jsonapi.config;

import java.util.ArrayList;
import java.util.List;

public class JSONAPIGroupConfigObject extends ConfigObject {
	public String name;
	public List<String> streams = new ArrayList<String>();
	public List<String> permissions = new ArrayList<String>();
	public List<String> methods = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	
	public List<String> getStreams() {
		return streams != null ? streams : new ArrayList<String>();
	}
	
	private List<JSONAPIPermissionNode> _list = null;
	public List<JSONAPIPermissionNode> getPermissions() {
		if(_list == null) {
			_list = new ArrayList<JSONAPIPermissionNode>();
			for(String name : permissions) {
				_list.add(PermissionNodesConfig.fromName(name));
			}
		}
		return _list;
	}
	
	public List<String> getMethods() {
		return methods != null ? methods : new ArrayList<String>();
	}
	
	public boolean canUseStream(String streamName) {
		if(getStreams().contains("ALLOW_ALL")) {
			return true;
		}
		
		for(JSONAPIPermissionNode node : getPermissions()) {
			if(node.canUseStream(streamName)) {
				return true;
			}
		}
		
		return getStreams().contains(streamName);
	}
	
	public boolean canUseMethod(String methodName) {
		if(methodName.startsWith("jsonapi.")) {
			return true;
		}

		if(getMethods().contains("ALLOW_ALL")) {
			return true;
		}
		
		for(JSONAPIPermissionNode node : getPermissions()) {
			if(node.canUseMethod(methodName)) {
				return true;
			}
		}
		
		return getMethods().contains("ALLOW_ALL") || getMethods().contains(methodName);
	}
	
	public boolean hasPermission(String permission) {
		for(JSONAPIPermissionNode node : getPermissions()) {
			if(node.getName().equals(permission)) {
				return true;
			}
		}
		
		return false;
	}
}
