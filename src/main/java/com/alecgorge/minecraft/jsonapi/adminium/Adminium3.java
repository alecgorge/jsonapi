package com.alecgorge.minecraft.jsonapi.adminium;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.alecgorge.java.http.MutableHttpRequest;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamListener;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionMessage;
import com.alecgorge.minecraft.jsonapi.util.FixedSizeArrayList;

public class Adminium3 implements JSONAPIStreamListener {
	public static List<String>	pushTypes				= Arrays.asList(new String[] { "calladmin","player_join","player_quit","severe","taboo" });
	public static List<String>	pushTypeDescriptions	= Arrays.asList(new String[] { "On /calladmin","On player join","On player quit","On SEVERE logs","Chat notifications" });

	public Adminium3(JSONAPI jsonapi) {
		api = jsonapi;
		config = Adminium3Config.config();

		api.getServer().getPluginManager().registerEvents(new AdminiumChatListener(this), api);
		mcLog.addHandler(new ConsoleHandler(this));

		api.registerMethods(new Adminium3Methods(this));
		api.getStreamManager().getStream("connections").registerListener(this, false);
	}

	List<AdminiumPushNotification>	notifications		= Collections.synchronizedList(new FixedSizeArrayList<AdminiumPushNotification>(200));

	final String					APNS_PUSH_ENDPOINT	= "http://push.adminiumapp.com/push-message";

	JSONAPI							api;
	Adminium3Config					config;

	public final boolean			doTrace				= false;

	Logger							mcLog				= Logger.getLogger("Minecraft");

	public void pushNotification(String message, String push_type) {
		List<String> devices = new ArrayList<String>();

		for (String device : config.devices.keySet()) {
			Boolean should_recieve = config.devices.get(device).get(push_type);

			if (should_recieve != null && should_recieve) {
				devices.add(device);
			}
		}

		AdminiumPushNotification not = new AdminiumPushNotification(new Date(), message);
		sendNotification(devices, not);
	}
	
	public boolean shouldSendNotifications() {
		return JSONAPI.instance.adminiumEnabled && config.devices.keySet().size() > 0;
	}

	@Override
	public void onMessage(JSONAPIStreamMessage message, JSONAPIStream sender) {
		if (message instanceof ConnectionMessage) {
			ConnectionMessage c = (ConnectionMessage) message;
			if (c.TrueIsConnectedFalseIsDisconnected) {
				pushNotification(c.player + " joined!", "player_join");
			}
			else {
				pushNotification(c.player + " quit!", "player_quit");
			}
		}
	}

	public boolean calladmin(CommandSender from, String message) {
		if (api.anyoneCanUseCallAdmin || from.hasPermission("jsonapi.calladmin")) {
			String push = "Admin request from " + from.getName() + ": " + message;

			pushNotification(push, "calladmin");
			from.sendMessage("A message was sent to the admin(s).");
		}
		else if (!from.hasPermission("jsonapi.calladmin")) {
			from.sendMessage("You don't have the jsonapi.calladmin permission to call for an admin.");
		}

		return true;
	}

	protected void sendNotification(final List<String> devices, final AdminiumPushNotification not) {
		notifications.add(not);
		
		if(!shouldSendNotifications()) {
			return;
		}

		api.getServer().getScheduler().runTaskAsynchronously(api, new Runnable() {
			@Override
			public void run() {
				String msg = not.getPushNotification();
				MutableHttpRequest r = null;
				try {
					r = new MutableHttpRequest(new URL(APNS_PUSH_ENDPOINT));
					for (String d : devices) {
						r.addPostValue("devices[]", d);
					}
					r.addPostValue("message", msg);

					r.post();

					JSONAPI.dbug("Sending to " + APNS_PUSH_ENDPOINT + ": " + r.getPostKeys() + " -- " + r.getPostValues());

					// System.out.println(String.format("Sending %s to %d (%s) devices.",
					// msg, devices.size(), devices.get(0)));
				}
				catch (Exception e) {
					mcLog.warning("The Adminium push notification server seems to be down...push notifications will not go through at this time.");
				}
				finally {
					if (r != null)
						r.close();
				}
			}
		});
	}

	public class AdminiumChatListener implements Listener {
		Adminium3		adminium;
		Adminium3Config	config	= Adminium3Config.config();

		public AdminiumChatListener(Adminium3 ad) {
			adminium = ad;
		}

		@EventHandler
		public void onPlayerChat(AsyncPlayerChatEvent event) {
			String player = event.getPlayer().getName();
			String message = event.getMessage();
			String lowerMessage = message.toLowerCase();

			for (String device : config.devices.keySet()) {
				if (config.taboo.containsKey(device)) {
					for (String taboo : config.taboo.get(device)) {
						if (lowerMessage.indexOf(taboo.toLowerCase()) > -1) {
							String pmessage = String.format("%s mentioned %s: %s", player, taboo, message);
							AdminiumPushNotification not = new AdminiumPushNotification(new Date(), pmessage);

							List<String> devices = new ArrayList<String>();
							devices.add(device);
							adminium.sendNotification(devices, not);
						}
					}
				}
			}
		}
	}

	public class ConsoleHandler extends Handler {
		Adminium3	adminium;
		long		lastNotification;

		public ConsoleHandler(Adminium3 d) {
			adminium = d;
			lastNotification = 0;
		}

		@Override
		public void close() throws SecurityException {
		}

		@Override
		public void flush() {
		}

		@Override
		public void publish(LogRecord arg0) {
			if (arg0 != null && arg0.getLevel().equals(Level.SEVERE)) {
				String message = "SEVERE message: " + arg0.getMessage();

				long time = (new Date()).getTime();
				if (time - lastNotification > 60 * 1000) {
					lastNotification = time;

					adminium.pushNotification(message, "severe_log");
				}
			}
		}
	}

	public class AdminiumPushNotification {
		Date	dateSent;
		String	notification;

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
}
