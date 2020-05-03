package com.alecgorge.minecraft.jsonapi.permissions;

import java.util.ArrayList;
import java.util.List;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.config.ConfigObject;
import com.alecgorge.minecraft.jsonapi.config.GroupsConfig;
import com.alecgorge.minecraft.jsonapi.config.JSONAPIGroupConfigObject;
import com.alecgorge.minecraft.jsonapi.config.JSONAPIPermissionNode;

public class JSONAPIGroup extends ConfigObject {
	public String name;
	private JSONAPIGroupConfigObject obj;
	
	public JSONAPIGroup(String groupName) {
		name = groupName;
		obj = GroupsConfig.config().getGroup(groupName);
		
		if(obj == null) {
			JSONAPI.instance.getLogger().warning(name + " is not a valid name for a JSONAPI group.");
			JSONAPI.instance.getLogger().warning("Valid group are " + GroupsConfig.config().getGroups());
		}
	}
	
	public String getName() {
		return name;
	}
	
	private JSONAPIGroupConfigObject getConfigObject() {
		return obj;
	}
	
	public List<String> getStreams() {
		if(getConfigObject() == null) return new ArrayList<String>();
		return getConfigObject().getStreams();
	}
	
	public List<JSONAPIPermissionNode> getPermissions() {
		if(getConfigObject() == null) return new ArrayList<JSONAPIPermissionNode>();
		return getConfigObject().getPermissions();
	}
	
	public List<String> getMethods() {
		if(getConfigObject() == null) return new ArrayList<String>();
		return getConfigObject().getMethods();
	}
	
	public boolean canUseStream(String streamName) {
		if(getConfigObject() == null) return false;
		return getConfigObject().canUseStream(streamName);
	}
	
	public boolean canUseMethod(String methodName) {
		if(getConfigObject() == null) return false;
		return getConfigObject().canUseMethod(methodName);
	}
	
	public boolean hasPermission(String permission) {
		if(getConfigObject() == null) return false;
		return getConfigObject().hasPermission(permission);
	}
}
