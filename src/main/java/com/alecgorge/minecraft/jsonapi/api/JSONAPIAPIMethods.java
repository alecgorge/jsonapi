package com.alecgorge.minecraft.jsonapi.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.config.GroupsConfig;
import com.alecgorge.minecraft.jsonapi.config.JSONAPIPermissionNode;
import com.alecgorge.minecraft.jsonapi.config.PermissionNodesConfig;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIGroup;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;

public class JSONAPIAPIMethods {
	Server server;
	UsersConfig authTable;
	public JSONAPIAPIMethods(Server server) {
		this.server = server;
		authTable = JSONAPI.instance.getAuthTable();
	}
	
	public List<JSONAPIUser> listUsers() {
		try {
			return new ArrayList<JSONAPIUser>(authTable.getJSONAPIUsers().values());
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<JSONAPIUser>();
		}
	
	}
	public List<JSONAPIGroup> listGroups() {
		try {
			return GroupsConfig.config().getJSONAPIGroups();
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<JSONAPIGroup>();
		}
	}
	
	public JSONAPIUser getUser(String username) {
		return authTable.getUser(username);
	}
	
	public JSONAPIGroup getGroup(String groupName) {
		return new JSONAPIGroup(groupName);
	}
	
	public List<String> listMethods() {
		try {
			List<String> l = JSONAPI.instance.getJSONServer().getCaller().getAllMethods();
			l.add(0, "ALLOW_ALL");
			return l;
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	public List<String> listStreams() {
		try {
			List<String> l = JSONAPI.instance.getStreamSources();
			l.add(0, "ALLOW_ALL");
			return l;
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	public List<String> listPermissions() {
		List<String> r = new ArrayList<String>(PermissionNodesConfig.config().getPermissions().keySet());
		Collections.sort(r);
		return r;
	}
	
	public List<String> listUsernames() {
		try {
			List<String> r = new ArrayList<String>();
			for(JSONAPIUser u : listUsers()) {
				r.add(u.getUsername());
			}
			return r;
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	
	}
	public List<String> listGroupNames() {
		try {
			List<String> r = new ArrayList<String>();
			for(JSONAPIGroup u : listGroups()) {
				r.add(u.getName());
			}
			return r;
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	public boolean changePassword(String username, String password) {
		try {
			Map<String, Object> m = authTable.getRawUser(username);
			m.put("password", password);
			authTable.generateCache(true);
			authTable.save();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean addUser(String username, String password, List<String> groups) {
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("username", username);
			map.put("password", password);
			map.put("groups", groups);
			authTable.users.add(map);
			authTable.save();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean removeUser(String username) {
		try {
			for(int i = 0; i < authTable.users.size(); i++) {
				if(authTable.users.get(i).get("username").equals(username)) {
					authTable.users.remove(i);
				}
			}
			authTable.generateCache(true);
			authTable.save();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;		
	}
	
	@SuppressWarnings("unchecked")
	public boolean addGroupToUser(String username, String group) {
		try {
			Map<String, Object> m = authTable.getRawUser(username);
			((List<String>)m.get("groups")).add(group);
			authTable.generateCache(true);
			authTable.save();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean removeGroupFromUser(String username, String group) {
		try {
			Map<String, Object> m = authTable.getRawUser(username);
			((List<String>)m.get("groups")).remove(group);
			authTable.generateCache(true);
			authTable.save();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean addGroup(String name, List<String> methods, List<String> streams, List<String> nodes) {
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("streams", streams);
			map.put("methods", methods);
			map.put("permissions", nodes);
			GroupsConfig.config().groups.add(map);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean setGroup(String name, List<String> methods, List<String> streams, List<String> nodes) {
		try {
			Map<String, Object> map = GroupsConfig.config().getRawGroup(name);
			map.put("streams", streams);
			map.put("methods", methods);
			map.put("permissions", nodes);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean removeGroup(String name) {
		try {
			for(int i = 0; i < GroupsConfig.config().groups.size(); i++) {
				if(GroupsConfig.config().groups.get(i).get("name").equals(name)) {
					GroupsConfig.config().groups.remove(i);
				}
			}
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addPermissionToGroup(String name, String node) {
		try {
			Map<String, Object> m = GroupsConfig.config().getRawGroup(name);
			((List<String>)m.get("permissions")).add(node);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean removePermissionFromGroup(String name, String node) {
		try {
			Map<String, Object> m = GroupsConfig.config().getRawGroup(name);
			((List<String>)m.get("permissions")).remove(node);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addMethodToGroup(String name, String method) {
		try {
			Map<String, Object> m = GroupsConfig.config().getRawGroup(name);
			((List<String>)m.get("methods")).add(method);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean removeMethodFromGroup(String name, String method) {
		try {
			Map<String, Object> m = GroupsConfig.config().getRawGroup(name);
			((List<String>)m.get("methods")).remove(method);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addStreamToGroup(String name, String stream) {
		try {
			Map<String, Object> m = GroupsConfig.config().getRawGroup(name);
			((List<String>)m.get("methods")).add(stream);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean removeStreamFromGroup(String name, String stream) {
		try {
			Map<String, Object> m = GroupsConfig.config().getRawGroup(name);
			((List<String>)m.get("streams")).remove(stream);
			GroupsConfig.config().generateCache();
			GroupsConfig.config().save();
			authTable.generateCache(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public List<String> listPermissions(String username) {
		List<String> perms = new ArrayList<String>();
		JSONAPIUser u = JSONAPI.instance.getAuthTable().getUser(username);
		if(u.canUseMethod("ALLOW_ALL") && u.canUseStream("ALLOW_ALL")) {
			perms.addAll(PermissionNodesConfig.config().getPermissions().keySet());
			return perms;
		}
		for(JSONAPIGroup g : u.getGroups()) {
			for(JSONAPIPermissionNode node : g.getPermissions()) {
				perms.add(node.getName());
			}
		}
		return perms;
	}
	
	public List<String> listMethods(String username) {
		List<String> perms = new ArrayList<String>();
		try {
			for(JSONAPIGroup g : JSONAPI.instance.getAuthTable().getUser(username).getGroups()) {
				perms.addAll(g.getMethods());
				for(JSONAPIPermissionNode node : g.getPermissions()) {
					perms.addAll(node.methods);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return perms;
	}
	
	public List<String> listStreams(String username) {
		List<String> perms = new ArrayList<String>();
		try {
			for(JSONAPIGroup g : JSONAPI.instance.getAuthTable().getUser(username).getGroups()) {
				perms.addAll(g.getStreams());
				for(JSONAPIPermissionNode node : g.getPermissions()) {
					perms.addAll(node.streams);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return perms;
	}

}
