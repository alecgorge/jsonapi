package com.alecgorge.minecraft.jsonapi.config;

import java.util.ArrayList;
import java.util.List;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

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
			if(permissions != null) {
				for(String name : permissions) {
					_list.add(PermissionNodesConfig.fromName(name));
				}
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
		JSONAPI.dbug("Testing permissions for " + methodName);
		
		if(getMethods().contains("ALLOW_ALL")) {
			JSONAPI.dbug("ALLOW_ALL found in methods");
			return true;
		}
		
		for(JSONAPIPermissionNode node : getPermissions()) {
			if(node.canUseMethod(methodName)) {
				JSONAPI.dbug(node.getName() + " allows usage of " + methodName);
				return true;
			}
		}
		
		if(getMethods().contains(methodName)) {
			JSONAPI.dbug("the user's method list explicitly contains " + methodName);
			return true;
		}
		else {
			JSONAPI.dbug("the user cannot use " + methodName);
			return false;
		}
	}
	
	public boolean hasPermission(String permission) {
		for(JSONAPIPermissionNode node : getPermissions()) {
			if(node.getName().equals(permission)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String toString() {
		return "name: " + name + "; streams: " + streams + "; permissions: " + permissions + "; methods: " + methods;
	}
}
