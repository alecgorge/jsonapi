package com.alecgorge.minecraft.jsonapi.adminium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;

import com.alecgorge.java.http.HttpRequest;
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
	
	List<String> devices = new ArrayList<String>();
	Map<String, Boolean> settings = new HashMap<String, Boolean>();
	
	private final String APNS_PUSH_ENDPOINT = "http://alecgorge.com:25132/push";
	
	private JSONAPI api;
	public boolean init = false;
	
	public boolean doTrace = false;
	
	private Logger mcLog = Logger.getLogger("Minecraft");
	
	private void trace(Object ... args) {		
		if(doTrace) {
			String[] na = new String[args.length];
			for(int i = 0; i < args.length; i++)
				na[i] = args[i] == null ? "NULL_VALUE" : args[i].toString();
			
			mcLog.info("'" + api.join(Arrays.asList(na), "' '") + "'");
		}
	}
	
	private List<String> pushTypes = new ArrayList<String>();
	private List<String> pushTypeDescriptions;
	
	public PushNotificationDaemon(File configFile, JSONAPI api) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.configFile = configFile;
		this.api = api;
		
		mcLog.addHandler(new ConsoleHandler(this));
		api.registerAPICallHandler(this);
		if(configFile.exists()) {
			initalize();
		}
	}
	
	public class ConsoleHandler extends Handler {
		PushNotificationDaemon p;
		long lastNotification;
		
		public ConsoleHandler (PushNotificationDaemon d) {
			p = d;
			lastNotification = new Date().getTime();
		}
		

		@Override
		public void close() throws SecurityException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void publish(LogRecord arg0) {
			if(arg0 != null && arg0.getLevel().equals(Level.SEVERE)) {
				if((new Date()).getTime() - lastNotification > (60*15)) {
					p.pushNotification("SEVERE message logged in the console: "+arg0.getMessage().substring(0, Math.min(200, arg0.getMessage().length()-1)));
				}
			}
		}
	}
	
	public void addDeviceIfNotExist(String device) throws IOException {
		if(!devices.contains(device)) {
			registerDevice(device);
		}
	}
	
	private void registerDevice(final String device) {
		trace("Attempting to register", device);
		
		if(device.length() != 64 || devices.contains(device)) {
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
		if(devices.size() < 1) {
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				trace("pushing", message);
				HttpRequest r = null;
				try {
					r = new HttpRequest(new URL(APNS_PUSH_ENDPOINT));
			        for(String d : devices) {
			        	r.addPostValue("devices[]", d);
			        }
			        r.addPostValue("message", message);
			        
			        trace("Sending Post Args:", devices, message, r.getPostKeys(), r.getPostValues());
			
			        trace("Complete", r.post().getReponse());
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if(r!=null) r.close();
				}
			}
		}).start();		
	}

	@Override
	public boolean willHandle(APIMethodName methodName) {
		return (methodName.getNamespace().equals("adminium") && (methodName.getMethodName().equals("registerDevice") || methodName.getMethodName().equals("listPushTypes") || methodName.getMethodName().equals("setPushTypeEnabled")));
	}
	
	private void initalize() {
		if(!this.init) {
			boolean initialSetup = !configFile.exists();
			
			try {
				configFile.createNewFile();
				deviceConfig.load(configFile);
				
				if(initialSetup) {
					deviceConfig.set("devices", null);
					deviceConfig.set("settings", "");
					deviceConfig.set("settings.player_joined", true);
					deviceConfig.set("settings.player_quit", true);
					deviceConfig.set("settings.admin_call", true);
					deviceConfig.set("settings.severe_log", true);
					
					deviceConfig.save(configFile);
					deviceConfig.load(configFile);
				}
				
				devices = deviceConfig.getList("devices", new ArrayList<String>());
				if(devices == null) devices = new ArrayList<String>();
				
				trace("Current Devices", devices);
				
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
			
			pushTypes.addAll(settings.keySet());
			Collections.sort(pushTypes);
			pushTypeDescriptions = Arrays.asList(new String[] {
				"On /calladmin",
				"On player join",
				"On player quit",
				"On SEVERE logs"
			});
			
			this.init = true;
		}		
	}

	@Override
	public Object handle(APIMethodName methodName, Object[] args) {
		if(methodName.getNamespace().equals("adminium")) {
			initalize();
		}
		
		
		if(methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("registerDevice") && args.length == 1) {			
			String deviceToken = args[0].toString();
			
			registerDevice(deviceToken);
		}
		else if(methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("listPushTypes")) {
			JSONObject o = new JSONObject();
			
			int i = 0;
			for(String k : pushTypes) {
				JSONObject oo = new JSONObject();
				oo.put("enabled", settings.get(k));
				oo.put("description", pushTypeDescriptions.get(i));
				o.put(k, oo);
				i++;
			}
			
			return o;
		}
		else if(methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("setPushTypeEnabled")) {
			settings.put(args[0].toString(), Boolean.valueOf(args[1].toString()));
			deviceConfig.set("settings."+args[0].toString(), settings.get(args[0].toString()));
			try {
				deviceConfig.save(configFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		}
		
		return null;
	}
}
