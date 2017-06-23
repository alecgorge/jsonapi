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

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.java.http.MutableHttpRequest;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.api.APIMethodName;
import com.alecgorge.minecraft.jsonapi.api.JSONAPICallHandler;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamListener;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionMessage;
import com.alecgorge.minecraft.jsonapi.util.FixedSizeArrayList;

public class PushNotificationDaemon implements JSONAPIStreamListener, JSONAPICallHandler {
	public class AdminiumPushNotification {
		Date dateSent;
		String notification;
		
		public AdminiumPushNotification(Date d, String message) {
			dateSent = d;
			notification = message;
		}
		
		public Date getDateSent() {
			return dateSent;
		}

		public void setDateSent(Date dateSent) {
			this.dateSent = dateSent;
		}

		public String getMessage() {
			return notification;
		}

		public void setMessage(String notification) {
			this.notification = notification;
		}
		
		public String getPushNotification() {
			JSONAPI api = JSONAPI.instance;
			
			String messager = getMessage();
			if (api.serverName != null && !api.serverName.isEmpty()) {
				messager = (api.serverName.equals("default") ? api.getServer().getServerName() : api.serverName) + ": " + messager;
			}

			return messager.length() > 210 ? messager.substring(0, 208) + "\u2026" : messager;
		}		
	}
	
	YamlConfiguration deviceConfig = new YamlConfiguration();
	File configFile;

	List<String> devices = new ArrayList<String>();
	Map<String, Boolean> settings = new HashMap<String, Boolean>();

	Map<String, Map<String, Boolean>> deviceOverrides = new HashMap<String, Map<String, Boolean>>();

	// log /calladmin & severes
	List<String> calladmins = Collections.synchronizedList(new FixedSizeArrayList<String>(50));
	List<String> severeLogs = Collections.synchronizedList(new FixedSizeArrayList<String>(50));
	List<AdminiumPushNotification> notifications = Collections.synchronizedList(new FixedSizeArrayList<AdminiumPushNotification>(150));

	private final String APNS_PUSH_ENDPOINT = "http://push.adminiumapp.com/push-message";

	private JSONAPI api;
	public boolean init = false;

	public final boolean doTrace = false;

	private Logger mcLog = Logger.getLogger("Minecraft");

	private void trace(Object... args) {
		if (doTrace) {
			String[] na = new String[args.length];
			for (int i = 0; i < args.length; i++)
				na[i] = args[i] == null ? "NULL_VALUE" : args[i].toString();

			mcLog.info("'" + api.join(Arrays.asList(na), "' '") + "'");
		}
	}

	private List<String> pushTypes = new ArrayList<String>();
	private List<String> pushTypeDescriptions;

	public PushNotificationDaemon(File configFile, JSONAPI api) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.configFile = configFile;
		this.api = api;

		api.registerAPICallHandler(this);
		if (configFile.exists()) {
			initalize();
		}
	}

	public boolean calladmin(CommandSender from, String message) {
		if (api.anyoneCanUseCallAdmin || from.hasPermission("jsonapi.calladmin")) {
			String push = "Admin request from " + from.getName() + ": " + message;

			if (!(settings.get("admin_call") != null && settings.get("admin_call"))) {
				from.sendMessage("The admin has disabled /calladmin.");

				return true;
			}

			calladmins.add(0, push);
			pushNotification(push, "admin_call");
			from.sendMessage("A message was sent to the admin(s).");
		} else if (!from.hasPermission("jsonapi.calladmin")) {
			from.sendMessage("You don't have the jsonapi.calladmin permission to call for an admin.");
		}

		return true;
	}

	public class ConsoleHandler extends Handler {
		PushNotificationDaemon p;
		long lastNotification;

		public ConsoleHandler(PushNotificationDaemon d) {
			p = d;
			lastNotification = 0;
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
			if (settings != null && arg0 != null && arg0.getLevel().equals(Level.SEVERE)) {
				String message = "SEVERE message logged in the console: " + arg0.getMessage();
				severeLogs.add(0, message);

				if (settings.get("severe_log") != null && settings.get("severe_log")) {
					long time = (new Date()).getTime();
					if (time - lastNotification > 60 * 1000) {
						lastNotification = time;

						p.pushNotification(message, "severe_log");
					}
				}
			}
		}
	}

	public void addDeviceIfNotExist(String device) throws IOException {
		if (!devices.contains(device)) {
			registerDevice(device);
		}
	}

	private void registerDevice(final String device) {
		trace("Attempting to register", device);

		if (device.length() != 64 || devices.contains(device)) {
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

	private void deregisterDevice(final String device) {
		trace("Attempting to deregister", device);

		if (device.length() != 64 || !devices.contains(device)) {
			return;
		}

		devices.remove(device);
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
		trace(message);

		if (message instanceof ConnectionMessage) {
			ConnectionMessage c = (ConnectionMessage) message;
			if (settings.get("player_joined") != null && settings.get("player_joined") && c.TrueIsConnectedFalseIsDisconnected) {
				pushNotification(c.player + " joined!", "player_joined");
			} else if (settings.get("player_quit") != null && settings.get("player_quit") && !c.TrueIsConnectedFalseIsDisconnected) {
				pushNotification(c.player + " quit!", "player_quit");
			}
		}
	}

	public void pushNotification(String messager, final String type) {
		if (devices.size() < 1) {
			return;
		}

		final AdminiumPushNotification not = new AdminiumPushNotification(new Date(), messager);
		notifications.add(not);

		new Thread(new Runnable() {

			@Override
			public void run() {
				String msg  = not.getPushNotification();
				trace("pushing", msg);
				MutableHttpRequest r = null;
				try {
					r = new MutableHttpRequest(new URL(APNS_PUSH_ENDPOINT));
					for (String d : devices) {
						if (deviceOverrides.containsKey(d) && deviceOverrides.get(d).containsKey(type) && deviceOverrides.get(d).get(type) || !deviceOverrides.containsKey(d))
							r.addPostValue("devices[]", d);
					}
					r.addPostValue("message", msg);

					trace("Sending Post Args:", devices, msg, r.getPostKeys(), r.getPostValues());

					trace("Complete", r.post().getReponse());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (r != null)
						r.close();
				}
			}
		}).start();
	}

	@Override
	public boolean willHandle(APIMethodName methodName) {
		return (methodName.getNamespace().equals("adminium") && (methodName.getMethodName().equals("registerDevice") || methodName.getMethodName().equals("listPushTypes") || methodName.getMethodName().equals("setPushTypeEnabled") || methodName.getMethodName().equals("triggerSevere") || methodName.getMethodName().equals("getCallAdmins") || methodName.getMethodName().equals("getSeveres") || methodName.getMethodName().equals("deregisterDevice")));
	}

	private void initalize() {
		if (!this.init) {
			mcLog.addHandler(new ConsoleHandler(this));

			boolean initialSetup = !configFile.exists();

			try {
				configFile.createNewFile();
				deviceConfig.load(configFile);

				if (initialSetup) {
					deviceConfig.set("devices", null);
					deviceConfig.set("device-map", null);
					deviceConfig.set("settings", "");
					deviceConfig.set("settings.player_joined", false);
					deviceConfig.set("settings.player_quit", false);
					deviceConfig.set("settings.admin_call", true);
					deviceConfig.set("settings.severe_log", false);

					deviceConfig.save(configFile);
					deviceConfig.load(configFile);
				}

				if (!deviceConfig.contains("group_assignments")) {
					deviceConfig.set("group_assignments", null);
					deviceConfig.save(configFile);
				}

				devices = deviceConfig.getStringList("devices");
				if (devices == null)
					devices = new ArrayList<String>();

				trace("Current Devices", devices);

				Map<String, Object> tempDefaults = ((ConfigurationSection) deviceConfig.get("settings")).getValues(false);
				for (String key : tempDefaults.keySet()) {
					settings.put(key, Boolean.valueOf(tempDefaults.get(key).toString()));
				}

				// load device specific overrides
				Object m = deviceConfig.get("device-map");

				if (m != null) {
					Map<String, Object> tempOverrides = ((ConfigurationSection) m).getValues(false);
					for (String device : tempOverrides.keySet()) {
						Map<String, Boolean> map = new HashMap<String, Boolean>();

						Map<String, Object> tempBools = ((ConfigurationSection) deviceConfig.get("device-map." + device)).getValues(false);

						for (String key : tempBools.keySet()) {
							deviceOverrides.put(key, new HashMap<String, Boolean>());

							map.put(key, Boolean.valueOf(tempDefaults.get(key).toString()));
						}
						deviceOverrides.put(device, map);
					}
				}

				if (settings != null && settings.get("player_joined") || settings.get("player_quit")) {
					api.getStreamManager().getStream("connections").registerListener(this, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			pushTypes.addAll(settings.keySet());
			Collections.sort(pushTypes);
			pushTypeDescriptions = Arrays.asList(new String[] { "On /calladmin", "On player join", "On player quit", "On SEVERE logs" });

			this.init = true;
		}
	}

	public void saveConfig() throws IOException {
		/*deviceConfig.set("group_assignments", groupAssignments);
		deviceConfig.set("group_permissions", groupPerms);*/

		deviceConfig.save(configFile);
	}

	@Override
	public Object handle(APIMethodName methodName, Object[] args) {
		if (methodName.getNamespace().equals("adminium")) {
			initalize();
		}

		if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("registerDevice") && args.length == 1) {
			String deviceToken = args[0].toString();

			registerDevice(deviceToken);
		} else if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("deregisterDevice") && args.length == 1) {
			String deviceToken = args[0].toString();

			deregisterDevice(deviceToken);
		} else if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("listPushTypes")) {
			JSONObject o = new JSONObject();

			int i = 0;
			for (String k : pushTypes) {
				JSONObject oo = new JSONObject();
				oo.put("enabled", settings.get(k));
				oo.put("description", pushTypeDescriptions.get(i));
				o.put(k, oo);
				i++;
			}

			return o;
		} else if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("setPushTypeEnabled")) {
			settings.put(args[0].toString(), Boolean.valueOf(args[1].toString()));
			deviceConfig.set("settings." + args[0].toString(), settings.get(args[0].toString()));
			try {
				deviceConfig.save(configFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		} else if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("triggerSevere")) {
			for (int i = 0; i < 10; i++)
				mcLog.severe("This is a severe log: " + i);

			return true;
		} else if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("getCallAdmins")) {
			return calladmins;
		} else if (methodName.getNamespace().equals("adminium") && methodName.getMethodName().equals("getSeveres")) {
			return severeLogs;
		}

		return null;
	}
}
