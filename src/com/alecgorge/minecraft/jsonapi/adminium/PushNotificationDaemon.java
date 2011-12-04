package com.alecgorge.minecraft.jsonapi.adminium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.api.APIMethodName;
import com.alecgorge.minecraft.jsonapi.api.JSONAPICallHandler;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamListener;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionMessage;

public class PushNotificationDaemon implements JSONAPIStreamListener, JSONAPICallHandler {
	YamlConfiguration deviceConfig = new YamlConfiguration();
	File configFile;
	
	List<String> devices;
	Map<String, Boolean> settings = new HashMap<String, Boolean>();
	
	private final String APNS_PUSH_ENDPOINT = "http://adminium.nodejitsu.com/push";
	
	private JSONAPI api;
	public boolean init = false;
	
	public PushNotificationDaemon(File configFile, JSONAPI api) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.configFile = configFile;
		this.api = api;
		
		api.registerAPICallHandler(this);
	}
	
	public void addDeviceIfNotExist(String device) throws IOException {
		if(!devices.contains(device)) {
			registerDevice(device);
		}
	}
	
	private void registerDevice(final String device) {
		if(device.length() != 64) {
			return;
		}
		
    	devices.add(device);
    	deviceConfig.set("devices", devices);
    	try {
			deviceConfig.save(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(JSONAPIStreamMessage message, JSONAPIStream sender) {
		if(message instanceof ConnectionMessage) {
			ConnectionMessage c = (ConnectionMessage) message;
			if(settings.get("player_joined") && c.TrueIsConnectedFalseIsDisconnected) {
				pushNotification(c.player + " joined!");
			}
			else if(settings.get("player_quit") && !c.TrueIsConnectedFalseIsDisconnected) {
				pushNotification(c.player + " quit!");
			}
		}
	}
	
	public void pushNotification(final String message) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				try {
					HttpPost p = new HttpPost(APNS_PUSH_ENDPOINT);
					
			        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			        
			        for(String d : devices) {
			        	nvps.add(new BasicNameValuePair("devices[]", d));
			        }
			        nvps.add(new BasicNameValuePair("message", message));
			
			        p.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
			        httpclient.execute(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					httpclient.getConnectionManager().shutdown();
				}
			}
		})).start();
	}

	@Override
	public boolean willHandle(APIMethodName methodName) {
		return (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("registerDevice"));
	}

	@Override
	public Object handle(APIMethodName methodName, Object[] args) {
		if(methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("registerDevice")) {
			if(!this.init) {
				boolean initialSetup = !configFile.exists();
				
				try {
					configFile.createNewFile();
					deviceConfig.load(configFile);
					
					if(initialSetup) {
						HashMap<String, Boolean> defaults = new HashMap<String, Boolean>();
						defaults.put("player_joined", true);
						defaults.put("player_quit", true);
						defaults.put("admin_call", true);
						defaults.put("severe_log", true);
						
						deviceConfig.set("devices", null);
						deviceConfig.set("settings", defaults);
						
						deviceConfig.save(configFile);
					}
					
					devices = deviceConfig.getList("devices", new ArrayList<String>());
					
					Map<String, Object> tempDefaults = ((ConfigurationSection)deviceConfig.get("settings")).getValues(false);
					for(String key : tempDefaults.keySet()) {
						settings.put(key, Boolean.valueOf(tempDefaults.get(key).toString()));
					}
					
					if(settings.get("player_joined") || settings.get("player_quit")) {
						api.getStreamManager().getStream("connections").registerListener(this, false);
					}				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			String deviceToken = args[0].toString();
			
			registerDevice(deviceToken);
		}
		return null;
	}
}
