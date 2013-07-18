package com.alecgorge.minecraft.jsonapi.permissions;

import java.util.List;

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
	}
	
	public String getName() {
		return name;
	}
	
	private JSONAPIGroupConfigObject getConfigObject() {
		return obj;
	}
	
	public List<String> getStreams() {
		return getConfigObject().getStreams();
	}
	
	public List<JSONAPIPermissionNode> getPermissions() {
		return getConfigObject().getPermissions();
	}
	
	public List<String> getMethods() {
		return getConfigObject().getMethods();
	}
	
	public boolean canUseStream(String streamName) {
		return getConfigObject().canUseStream(streamName);
	}
	
	public boolean canUseMethod(String methodName) {
		return getConfigObject().canUseMethod(methodName);
	}
	
	public boolean hasPermission(String permission) {
		return getConfigObject().hasPermission(permission);
	}
}
