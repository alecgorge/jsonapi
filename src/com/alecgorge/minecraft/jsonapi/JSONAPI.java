package com.alecgorge.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterface;
import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterfaceException;
import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKListener;
import com.alecgorge.minecraft.jsonapi.adminium.PushNotificationDaemon;
import com.alecgorge.minecraft.jsonapi.api.JSONAPICallHandler;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;
import com.alecgorge.minecraft.jsonapi.api.StreamPusher;
import com.alecgorge.minecraft.jsonapi.dynamic.APIWrapperMethods;
import com.alecgorge.minecraft.jsonapi.dynamic.API_Method;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.dynamic.JSONAPIMethodProvider;
import com.alecgorge.minecraft.jsonapi.streams.ConsoleHandler;
import com.alecgorge.minecraft.jsonapi.streams.ConsoleLogFormatter;
import com.alecgorge.minecraft.jsonapi.streams.StreamManager;
import com.alecgorge.minecraft.jsonapi.util.PropertiesFile;
import org.bukkit.event.EventPriority;

/**
 * 
 * @author alecgorge
 */
public class JSONAPI extends JavaPlugin implements RTKListener, JSONAPIMethodProvider {
	public PluginLoader pluginLoader;
	// private Server server;
	public JSONServer jsonServer;
	public JSONSocketServer jsonSocketServer;
	public JSONWebSocketServer jsonWebSocketServer;
	private StreamManager streamManager = new StreamManager();

	public boolean logging = false;
	public String logFile = "false";
	public String salt = "";
	public int port = 20059;
	private long startupDelay = 2000;
	public List<String> whitelist = new ArrayList<String>();
	public List<String> method_noauth_whitelist = new ArrayList<String>();
	public boolean anyoneCanUseCallAdmin = true;
	public String serverName = "default";
	public StreamPusher streamPusher;

	private Logger log = Logger.getLogger("Minecraft");
	public Logger outLog = Logger.getLogger("JSONAPI");
	private Handler handler;

	public RTKInterface rtkAPI = null;
	public InetAddress bindAddress;

	// for dynamic access
	public static JSONAPI instance;

	PushNotificationDaemon adminium;

	protected void initalize(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		this.pluginLoader = pluginLoader;
		// server = instance;
	}

	public JSONAPI() {
		super();
		JSONAPI.instance = this;
	}

	public JSONServer getJSONServer() {
		return jsonServer;
	}

	public synchronized StreamManager getStreamManager() {
		return this.streamManager;
	}

	public void registerStreamManager(String streamName, JSONAPIStream stream) {
		getStreamManager().registerStream(streamName, stream);
	}

	public void deregisterStream(String streamName) {
		getStreamManager().deregisterStream(streamName);
	}

	public void registerMethod(String method) {
		getJSONServer().getCaller().loadString("[" + method + "]");
	}

	public void registerMethods(String method) {
		getJSONServer().getCaller().loadString(method);
	}

	public synchronized Caller getCaller() {
		return getJSONServer().getCaller();
	}

	@API_Method(namespace = "jsonapi")
	public List<String> getStreamSources() {
		return new ArrayList<String>(getStreamManager().getStreams().keySet());
	}

	public void registerAPICallHandler(JSONAPICallHandler handler) {
		getCaller().registerAPICallHandler(handler);
	}

	public void registerMethods(JSONAPIMethodProvider obj) {
		getCaller().registerMethods(obj);
	}

	public void deregisterAPICallHandler(JSONAPICallHandler handler) {
		getCaller().deregisterAPICallHandler(handler);
	}

	private JSONAPIPlayerListener l = new JSONAPIPlayerListener(this);
	YamlConfiguration yamlConfig;
	File yamlFile;

	public void onEnable() {
		try {
			HashMap<String, String> auth = new HashMap<String, String>();

			if (!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}

			yamlFile = new File(getDataFolder(), "config.yml");
			outLog = Logger.getLogger("JSONAPI");

			File mainConfig = new File(getDataFolder(), "JSONAPI.properties");
			File authfile = new File(getDataFolder(), "JSONAPIAuthentication.txt");
			File authfile2 = new File(getDataFolder(), "JSONAPIMethodNoAuthWhitelist.txt");
			File methods = new File(getDataFolder(), "methods.json");

			if (!methods.exists()) {
				log.severe("[JSONAPI] plugins/JSONAPI/methods.json is missing!");
				log.severe("[JSONAPI] JSONAPI not loaded!");
				return;
			}
			if (!yamlFile.exists() && !mainConfig.exists()) {
				log.severe("[JSONAPI] config.yml and JSONAPI.properties are both missing. You need at least one!");
				log.severe("[JSONAPI] JSONAPI not loaded!");
				return;
			}

			PropertiesFile options = null;
			String ipWhitelist = "";
			String reconstituted = "";
			if (mainConfig.exists()) {
				options = new PropertiesFile(mainConfig.getAbsolutePath());
				logging = options.getBoolean("log-to-console", true);
				logFile = options.getString("log-to-file", "false");
				ipWhitelist = options.getString("ip-whitelist", "false");
				salt = options.getString("salt", "");
				reconstituted = "";
			}

			if (mainConfig.exists() && !yamlFile.exists()) {
				// auto-migrate to yaml from properties and plain text files
				yamlFile.createNewFile();
				yamlConfig = new YamlConfiguration();

				if (!ipWhitelist.trim().equals("false")) {
					String[] ips = ipWhitelist.split(",");
					StringBuffer t = new StringBuffer();
					for (String ip : ips) {
						t.append(ip.trim() + ",");
						whitelist.add(ip);
					}
					reconstituted = t.toString();
				}

				port = options.getInt("port", 20059);

				try {
					FileInputStream fstream;
					try {
						fstream = new FileInputStream(authfile);
					} catch (FileNotFoundException e) {
						authfile.createNewFile();
						fstream = new FileInputStream(authfile);
					}

					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String line;

					while ((line = br.readLine()) != null) {
						if (!line.startsWith("#")) {
							String[] parts = line.trim().split(":");
							if (parts.length == 2) {
								auth.put(parts[0], parts[1]);
							}
						}
					}

					br.close();
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					FileInputStream fstream;
					try {
						fstream = new FileInputStream(authfile2);
					} catch (FileNotFoundException e) {
						authfile2.createNewFile();
						fstream = new FileInputStream(authfile2);
					}

					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String line;

					while ((line = br.readLine()) != null) {
						if (!line.trim().startsWith("#")) {
							method_noauth_whitelist.add(line.trim());
						}
					}

					br.close();
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				yamlConfig.set("options.log-to-console", logging);
				yamlConfig.set("options.log-to-file", logFile);
				yamlConfig.set("options.ip-whitelist", whitelist);
				yamlConfig.set("options.salt", salt);
				yamlConfig.set("options.port", port);
				yamlConfig.set("options.anyone-can-use-calladmin", false);

				yamlConfig.set("method-whitelist", method_noauth_whitelist);

				yamlConfig.set("logins", auth);

				yamlConfig.save(yamlFile);

				mainConfig.delete();
				authfile.delete();
				authfile2.delete();
			} else if (yamlFile.exists()) {
				yamlConfig = new YamlConfiguration();
				yamlConfig.load(yamlFile); // VERY IMPORTANT

				logging = yamlConfig.getBoolean("options.log-to-console", true);
				logFile = yamlConfig.getString("options.log-to-file", "false");

				whitelist = yamlConfig.getStringList("options.ip-whitelist");
				for (String ip : whitelist) {
					reconstituted += ip + ",";
				}

				salt = yamlConfig.getString("options.salt", "");
				port = yamlConfig.getInt("options.port", 20059);
				startupDelay = yamlConfig.getInt("options.startup-delay", 2000);
				anyoneCanUseCallAdmin = yamlConfig.getBoolean("options.anyone-can-use-calladmin", false);
				serverName = yamlConfig.getString("options.server-name", "default");

				String host = yamlConfig.getString("options.bind-address", "");
				if (host.equals("")) {
					bindAddress = null;
				} else {
					bindAddress = InetAddress.getByName(host);
				}

				method_noauth_whitelist = yamlConfig.getStringList("method-whitelist");

				Set<String> logins = ((ConfigurationSection) yamlConfig.get("logins")).getKeys(false);
				for (String k : logins) {
					auth.put(k, yamlConfig.getString("logins." + k));
				}
			}

			YamlConfiguration yamlRTK = new YamlConfiguration();

			try {
				yamlRTK.load(new File(getDataFolder(), "config_rtk.yml"));
				rtkAPI = RTKInterface.createRTKInterface(yamlRTK.getInt("RTK.port", 25561), "localhost", yamlRTK.getString("RTK.username", "user"), yamlRTK.getString("RTK.password", "pass"));
			} catch (RTKInterfaceException e) {

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (rtkAPI != null) {
					rtkAPI.registerRTKListener(this);
				}
			}

			if (!logging) {
				outLog.setUseParentHandlers(false);
				for (Handler h : outLog.getHandlers()) {
					outLog.removeHandler(h);
				}
			}
			if (!logFile.equals("false") && !logFile.isEmpty()) {
				FileHandler fh = new FileHandler(logFile, true);
				fh.setFormatter(new ConsoleLogFormatter());
				outLog.addHandler(fh);
			}

			if (auth.size() == 0) {
				log.severe("[JSONAPI] No valid logins for JSONAPI. Check config.yml");
				return;
			}

			log.info("[JSONAPI] Logging to file: " + logFile);
			log.info("[JSONAPI] Logging to console: " + String.valueOf(logging));
			log.info("[JSONAPI] IP Whitelist = " + (reconstituted.equals("") ? "None, all requests are allowed." : reconstituted));

			jsonServer = new JSONServer(auth, this, startupDelay);

			// add console stream support
			handler = new ConsoleHandler(jsonServer);
			log.addHandler(handler);

			if (logging) {
				outLog.addHandler(handler);
			}

			log.info("[JSONAPI] Attempting to use port " + port);

			jsonSocketServer = new JSONSocketServer(port + 1, jsonServer);
			jsonWebSocketServer = new JSONWebSocketServer(port + 2, jsonServer);
			jsonWebSocketServer.start();

			registerStreamManager("chat", getJSONServer().chat);
			registerStreamManager("console", getJSONServer().console);
			registerStreamManager("connections", getJSONServer().connections);

			streamPusher = new StreamPusher(streamManager, new File(getDataFolder(), "push_locations.yml"));

			initialiseListeners();

			adminium = new PushNotificationDaemon(new File(getDataFolder(), "adminium.yml"), this);

			registerMethods(this);
		} catch (Exception ioe) {
			log.severe("[JSONAPI] Couldn't start server!\n");
			ioe.printStackTrace();
			// System.exit( -1 );
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			if (cmd.getName().equals("reloadjsonapi")) {
				if (sender instanceof ConsoleCommandSender) {
					log.info("Reloading JSONAPI");
					onDisable();
					onEnable();
				}
				return true;
			} else if (cmd.getName().equals("jsonapi-list")) {
				listMethods(sender);
				return true;
			}
			/*
			 * else if(!adminium.init && args.length > 1 &&
			 * cmd.getName().equals("calladmin")) {
			 * System.out.println(sender.getName() + ": " +
			 * join(Arrays.asList(args), " ")); return true; }
			 */
		}
		if (adminium.init && args.length >= 1 && cmd.getName().equals("calladmin")) {
			return adminium.calladmin(sender, join(Arrays.asList(args), " "));
		}

		if (cmd.getName().equals("jsonapi")) {
			if (args.length == 0) {
				sender.sendMessage("If you don't know how to use this, you should probably use /help jsonapi");
				return true;
			}

			String subCommand = args[0];
			if (subCommand.equals("list")) {
				if (args.length == 1) {
					listMethods(sender);
				} else {
					listMethods(sender, args[1]);
				}
				return true;
			} else if (subCommand.equals("reload")) {
				log.info("Reloading JSONAPI");
				onDisable();
				onEnable();
				return true;
			} else if (subCommand.equals("subscribe")) {
				if (args.length != 3) {
					sender.sendMessage(ChatColor.RED + "Incorrect number of args. Should be /jsonapi subscribe [stream-name] [url-to-POST-to]");
					return true;
				}

				try {
					streamPusher.subscribe(args[1], args[2], true);
					sender.sendMessage(ChatColor.GREEN + "Subscription setup.");
				} catch (MalformedURLException e) {
					sender.sendMessage(ChatColor.RED + "Invalid URL: " + e.getMessage());
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
				}
				return true;
			} else if (subCommand.equals("users")) {
				if (args.length == 1 || args[1].equals("list")) {
					sender.sendMessage("Usernames: " + join(new ArrayList<String>(getJSONServer().getLogins().keySet()), ", "));
					return true;
				} else if (args.length == 3 && args[2].equals("password")) {
					Map<String, String> logins = getJSONServer().getLogins();

					if (!logins.containsKey(args[1])) {
						sender.sendMessage(ChatColor.RED + "No JSONAPI user named " + args[1]);
						return true;
					}

					sender.sendMessage(args[1] + "'s password: " + logins.get(args[1]));
					return true;
				} else if (args.length == 4 && args[1].equals("add")) {
					try {
						String username = args[2];
						String password = args[3];

						getJSONServer().getLogins().put(username, password);

						yamlConfig.set("logins", getJSONServer().getLogins());
						yamlConfig.save(yamlFile);

						sender.sendMessage(ChatColor.GREEN + "Created a new user with the username '" + username + "' and password '" + password + "'");
					} catch (IOException e) {
						sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
						e.printStackTrace();
					}
					return true;
				}
			}
		} else if (cmd.getName().equals("adminium")) {
			if (!adminium.init) {
				sender.sendMessage(ChatColor.RED + "You need Adminium for that.");
				return true;
			}

			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "/adminium [user (username)|create-user (username) (group name)|set-group (username) (group name)|list-groups]");
				return true;
			}

			String sub = args[0];
			if (args.length == 2 && sub.equals("user")) {
				String username = args[1];

				if (!adminium.groupAssignments.containsKey(username)) {
					sender.sendMessage(ChatColor.GREEN + username + " has access to everything and is not in a group.");
					return true;
				}

				sender.sendMessage(ChatColor.GREEN + username + " is in the group " + adminium.groupAssignments.get(username));
				return true;
			} else if (args.length == 3 && (sub.equals("create-user") || sub.equals("set-group"))) {
				try {
					String username = args[1];
					String groupName = args[2];

					if (!adminium.groupPerms.containsKey(groupName)) {
						sender.sendMessage(ChatColor.RED + groupName + " is a non-existant group!");
						return true;
					}

					String pass = "";
					if (!getJSONServer().getLogins().containsKey(username)) {
						pass = genPassword();
						getJSONServer().getLogins().put(username, pass);

						yamlConfig.set("logins", getJSONServer().getLogins());
						yamlConfig.save(yamlFile);
					} else {
						pass = getJSONServer().getLogins().get(username);
					}

					adminium.groupAssignments.put(username, groupName);
					adminium.saveConfig();

					sender.sendMessage(ChatColor.GREEN + "This user has the following information");
					sender.sendMessage(ChatColor.GREEN + "Username: " + username);
					sender.sendMessage(ChatColor.GREEN + "Password: " + pass);
					sender.sendMessage(ChatColor.GREEN + "Group name: " + groupName);
					sender.sendMessage(ChatColor.GREEN + "Salt: " + salt);
				} catch (IOException e) {
					sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
					e.printStackTrace();
				}
				return true;
			} else if (args.length == 1 && sub.equals("list-groups")) {
				sender.sendMessage(ChatColor.GREEN + join(new ArrayList<String>(adminium.groupPerms.keySet()), ", "));
				return true;
			}

			return true;
		}

		return false;
	}

	private String genPassword() {
		Random random = new Random();

		StringBuilder b = new StringBuilder((char) (random.nextInt('z' - 'A' + 1) + 'A'));
		for (int i = 0; i < 12; i++) {
			b.append((char) (random.nextInt('z' - 'A' + 1) + 'A'));
		}

		return b.toString();
	}

	private void listMethods(CommandSender sender) {
		listMethods(sender, "-1");
	}

	private void listMethods(CommandSender sender, String testKey) {
		for (String key : jsonServer.getCaller().methods.keySet()) {
			if (!testKey.equals("-1") && !testKey.equals(key)) {
				continue;
			}

			StringBuilder sb = new StringBuilder((key.trim().equals("") ? "Default Namespace" : key.trim()) + ": ");
			for (String m : jsonServer.getCaller().methods.get(key).keySet()) {
				sb.append(jsonServer.getCaller().methods.get(key).get(m).getName()).append(", ");
			}
			sender.sendMessage(sb.substring(0, sb.length() - 2).toString() + "\n");
		}
	}

	public String join(List<String> list, String conjunction) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

	@Override
	public void onDisable() {
		if (jsonServer != null) {
			try {
				jsonServer.stop();
				jsonSocketServer.stop();
				jsonWebSocketServer.stop();
				APIWrapperMethods.getInstance().disconnectAllFauxPlayers();
				if (rtkAPI != null)
					rtkAPI.deregisterRTKListener(this);
			} catch (Exception e) {
				// e.printStackTrace();
			}
			log.removeHandler(handler);
		}
	}

	public static Player loadOfflinePlayer(String exactPlayerName) {
		// big thanks to
		// https://github.com/lishd/OpenInv/blob/master/src/lishid/openinv/commands/OpenInvPluginCommand.java#L106
		// Offline inv here...
		try {
			// See if the player has data files

			// Find the player folder
			File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");

			// Find player name
			for (File playerfile : playerfolder.listFiles()) {
				String filename = playerfile.getName();
				String playername = filename.substring(0, filename.length() - 4);

				if (playername.trim().equalsIgnoreCase(exactPlayerName)) {
					// Create an entity to load the player data
					MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
					EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername, new ItemInWorldManager(server.getWorldServer(0)));
					Player target = (entity == null) ? null : (Player) entity.getBukkitEntity();
					if (target != null) {
						target.loadData();

						return target;
					}
				}
			}
		} catch (Exception e) {

		}

		return null;
	}

	private void initialiseListeners() {
		getServer().getPluginManager().registerEvents(l, this);
	}

	/**
	 * From a password, a number of iterations and a salt, returns the
	 * corresponding digest
	 * 
	 * @param iterationNb
	 *            int The number of iterations of the algorithm
	 * @param password
	 *            String The password to encrypt
	 * @param salt
	 *            byte[] The salt
	 * @return byte[] The digested password
	 * @throws NoSuchAlgorithmException
	 *             If the algorithm doesn't exist
	 */
	public static String SHA256(String password) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		byte[] input = null;
		try {
			input = digest.digest(password.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < input.length; i++) {
				String hex = Integer.toHexString(0xFF & input[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "UnsupportedEncodingException";
	}

	public void disable() {
		jsonServer.stop();
	}

	public static class JSONAPIPlayerListener implements Listener {
		JSONAPI p;

		// This controls the accessibility of functions / variables from the
		// main class.
		public JSONAPIPlayerListener(JSONAPI plugin) {
			p = plugin;
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
		public void onPlayerChat(PlayerChatEvent event) {
			p.jsonServer.logChat(event.getPlayer().getName(), event.getMessage());
		}

		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			APIWrapperMethods.getInstance().manager = ((CraftPlayer) event.getPlayer()).getHandle().netServerHandler.networkManager;
			p.jsonServer.logConnected(event.getPlayer().getName());
		}

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			p.jsonServer.logDisconnected(event.getPlayer().getName());
		}
	}

	@Override
	public void onRTKStringReceived(String message) {

	}
}