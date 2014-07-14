package com.alecgorge.minecraft.jsonapi.adminium;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.adminium.Adminium3.AdminiumPushNotification;
import com.alecgorge.minecraft.jsonapi.dynamic.API_Method;
import com.alecgorge.minecraft.jsonapi.dynamic.JSONAPIMethodProvider;

public class Adminium3Methods implements JSONAPIMethodProvider {
	Adminium3Config config;
	Adminium3 adminium;
	public Adminium3Methods(Adminium3 ad) {
		adminium = ad;
		config = Adminium3Config.config();
	}
	
	@API_Method(namespace="",name="adminium.devices.register", isProvidedByV2API=false)
	public boolean registerDevice(String pushID) {
		if(pushID.length() != 64) return false;
		
		if(!config.devices.containsKey(pushID)) {
			config.devices.put(pushID, config.notificationDefaults);
			try {
				config.save();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	@API_Method(namespace="",name="adminium.devices.remove", isProvidedByV2API=false)
	public boolean removeDevice(String pushID) {
		if(config.devices.containsKey(pushID)) {
			config.devices.remove(pushID);
			try {
				config.save();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	@API_Method(namespace="",name="adminium.devices.device.taboo.all", isProvidedByV2API=false)
	public List<String> tabooList(String pushID) {
		try {
			List<String> taboo = config.taboo.get(pushID);
			if(taboo == null) {
				return new ArrayList<String>();
			}
			return taboo;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	@API_Method(namespace="",name="adminium.devices.device.taboo.add", isProvidedByV2API=false)
	public boolean tabooAdd(String pushID, String phrase) {
		if(!config.taboo.containsKey(pushID)) {
			config.taboo.put(pushID, new ArrayList<String>());
		}
		
		if(!config.taboo.get(pushID).contains(phrase)) {
			config.taboo.get(pushID).add(phrase);
			try {
				config.save();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	@API_Method(namespace="",name="adminium.devices.device.taboo.remove", isProvidedByV2API=false)
	public boolean tabooRemove(String pushID, String phrase) {
		if(config.taboo.containsKey(pushID) && config.taboo.get(pushID).contains(phrase)) {
			config.taboo.get(pushID).remove(phrase);
			try {
				config.save();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	@API_Method(namespace="",name="adminium.devices.device.push_notifications", isProvidedByV2API=false)
	public JSONObject pushNotifications(String pushID) {
		if(config.devices.containsKey(pushID)) {
			Map<String, Boolean> dev = config.devices.get(pushID);
			
			JSONObject o = new JSONObject();
			for(String key : dev.keySet()) {
				JSONObject oo = new JSONObject();
				oo.put("enabled", dev.get(key));
				oo.put("description", Adminium3.pushTypeDescriptions.get(Adminium3.pushTypes.indexOf(key)));
				o.put(key, oo);
			}
			return o;
		}
		return new JSONObject();
	}
	
	@API_Method(namespace="",name="adminium.devices.device.set_push_notification", isProvidedByV2API=false)
	public JSONObject setPushNotifications(String pushID, String pushType, Boolean on) {
		if(config.devices.containsKey(pushID)) {
			Map<String, Boolean> dev = config.devices.get(pushID);
			dev.put(pushType, on);
			try {
				config.save();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return new JSONObject();
	}
	
	@API_Method(namespace="",name="adminium.notifications.all", isProvidedByV2API=false)
	public List<AdminiumPushNotification> getAllPushNotifications() {
		return adminium.notifications;
	}
}
