package com.alecgorge.minecraft.jsonapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterface;
import com.alecgorge.minecraft.jsonapi.adminium.Adminium3;
import com.alecgorge.minecraft.jsonapi.adminium.PushNotificationDaemon;
import com.alecgorge.minecraft.jsonapi.api.JSONAPICallHandler;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;
import com.alecgorge.minecraft.jsonapi.api.StreamPusher;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.dynamic.APIWrapperMethods;
import com.alecgorge.minecraft.jsonapi.dynamic.API_Method;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.dynamic.JSONAPIMethodProvider;
import com.alecgorge.minecraft.jsonapi.packets.netty.JSONAPINettyInjector;
import com.alecgorge.minecraft.jsonapi.packets.netty.router.RouteMatcher;
import com.alecgorge.minecraft.jsonapi.permissions.GroupManager;
import com.alecgorge.minecraft.jsonapi.streams.PerformanceStreamDataProvider;
import com.alecgorge.minecraft.jsonapi.streams.StreamManager;
import com.alecgorge.minecraft.jsonapi.streams.console.ConsoleHandler;
import com.alecgorge.minecraft.jsonapi.streams.console.ConsoleLogFormatter;
import com.alecgorge.minecraft.jsonapi.streams.console.Log4j2ConsoleHandler;
import com.alecgorge.minecraft.jsonapi.util.OfflinePlayerLoader;
import com.alecgorge.minecraft.jsonapi.util.TickRateCounter;

/**
 * 
 * @author alecgorge
 */
public class JSONAPI extends JavaPlugin implements JSONAPIMethodProvider {
	public PluginLoader pluginLoader;
	// private Server server;
	public JSONServer jsonServer;
	public JSONSocketServer jsonSocketServer;
	public JSONWebSocketServer jsonWebSocketServer;
	public JSONWebSocketServer sslJsonWebSocketServer;
	public JSONAPIMessageListener jsonMessageListener = new JSONAPIMessageListener(this);

	private StreamManager streamManager = new StreamManager();

	public boolean logging = false;
	public String logFile = "false";
	public String salt = "";
	public int port = 20059;
	public boolean allowSendingOldStreamMessages = true;
	private long startupDelay = 2000;
	public List<String> whitelist = new ArrayList<String>();
	public List<String> method_noauth_whitelist = new ArrayList<String>();
	UsersConfig auth;
	public boolean anyoneCanUseCallAdmin = true;
	public String serverName = "default";
	public StreamPusher streamPusher;
	public boolean useGroups = false;
	TickRateCounter tickRateCounter;
	public boolean adminiumEnabled = true;
	
	RouteMatcher router = new RouteMatcher();

	private Logger log = Bukkit.getLogger();
	public Logger outLog = Logger.getLogger("JSONAPI");
	private Handler handler;

	public RTKInterface rtkAPI = null;
	public InetAddress bindAddress;

	// for dynamic access
	public static JSONAPI instance;

	PushNotificationDaemon adminium;
	Adminium3 adminium3;
	
	GroupManager groupManager;
	
	JSONAPINettyInjector injector = null;

//#if jsonapiDebug=="yes"
//$	public static boolean shouldDebug = true;
//#else
	public static boolean shouldDebug = false;
//#endif
	public static void dbug(Object objects) {
		if(JSONAPI.shouldDebug) {
			System.out.println(objects);
		}
	}
	

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
	
	public GroupManager getGroupManager() {
		return groupManager;
	}
	
	public UsersConfig getAuthTable() {
		return auth;
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
		getJSONServer().getCaller().loadString("[" + method + "]", false);
	}

	public void registerMethods(String method) {
		getJSONServer().getCaller().loadString(method, false);
	}

	public synchronized Caller getCaller() {
		return getJSONServer().getCaller();
	}

	@API_Method(namespace = "jsonapi", isProvidedByV2API=false)
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
	
	public TickRateCounter getTickRateCounter() {
		return tickRateCounter;
	}

	private JSONAPIPlayerListener l = new JSONAPIPlayerListener(this);
	YamlConfiguration yamlConfig;
	File yamlFile;
	
	public ClassLoader myClassLoader() {
		return getClassLoader();
	}

	public void onEnable() {
		/*
		try {
			int countI = 0, countB = 0;
			JSONObject o = new JSONObject();
			for(Item i : Item.byId) {
				if(i == null) continue;
				countI++;
				if(i.id < 256) continue;
				
				System.out.println(String.format("name: %25s id: %4s class: %s", i.getName(), i.id, i.getClass()));
				
				String[] names = null;
				for(Field f : i.getClass().getDeclaredFields()) {
					if(f.getType().isAssignableFrom(String[].class)) {
						f.setAccessible(true);
						names = (String[]) f.get(i);
						if(names[0].contains("_")) {
							names = null;
							continue;
						}
						break;
					}
				}
				if(i instanceof ItemCloth) {
					names = ItemDye.a;
				}
				if(names == null) {
					continue;
				}
				
				//LocaleI18n.get(arg0)
				
				for(int j = 0; j < names.length; j++) {
					System.out.println(String.format("\t%d: %s", j, names[j]));
				}
				
				JSONObject obj = new JSONObject();
				obj.put(", value)
				
				o.put(String.valueOf(i.id), obj);				
			}
			for(Block b : Block.byId) {
				if(b == null) continue;
				countB++;
				System.out.println(String.format("name: %25s id: %4s materal: %s class: %s", b.getName(), b.id, b.material.getClass(), b.getClass()));

				String[] names = null;
				for(Field f : b.getClass().getDeclaredFields()) {
					if(f.getType().isAssignableFrom(String[].class)) {
						f.setAccessible(true);
						names = (String[]) f.get(b);
						if(names[0].contains("_")) {
							names = null;
							continue;
						}
						break;
					}
				}
				if(b instanceof BlockCloth) {
					names = ItemDye.a;
				}
				if(names == null) {
					continue;
				}
				
				for(int j = 0; j < names.length; j++) {
					System.out.println(String.format("\t%d: %s", j, names[j]));
				}
			}
			System.out.println("items: " + countI + " blocks: " + countB);
		} catch(Exception e) {
			
		}
		*/
		
		// for minecraft forge, Logger.getLogger("JSONAPI"); doesn't output anything...
		try {
			Class.forName("net.minecraftforge.common.MinecraftForge");
			outLog = log;
		}
		catch (Error e) {}
		catch (Exception e) {}


		boolean rtkInstalled = Bukkit.getPluginManager().getPlugin("RemoteToolkitPlugin") != null;
		
		try {
			auth = new UsersConfig(this);

			if (!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}

			yamlFile = new File(getDataFolder(), "config.yml");

			File methods = new File(getDataFolder(), "methods.json");
			File methodsFolder = new File(getDataFolder(), "methods");
			File rtkConfig = new File(getDataFolder(), "config_rtk.yml");
			File groups = new File(getDataFolder(), "groups.yml");

			if (!methods.exists()) {
				InputStream in = getResource("methods.json");
				OutputStream out = new FileOutputStream(methods);

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}

				in.close();
				out.close();

				log.info("[JSONAPI] methods.json has been copied from the jar");
			}
			if (!methodsFolder.exists()) {
				methodsFolder.mkdirs();
			}
			
			String[] methodsFiles = new String[] { "chat.json", "dynmap.json", "econ.json", "permissions.json", "fs.json", "readme.txt", "remotetoolkit.json", "system.json", "world.json" };

			for (String f : methodsFiles) {
				File outF = new File(methodsFolder, f);
				if (!outF.exists()) {
					outF.createNewFile();

					InputStream in = getResource("methods/" + f);
					OutputStream out = new FileOutputStream(outF);

					byte[] buffer = new byte[1024];
					int len;
					while ((len = in.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}

					in.close();
					out.close();
				}
			}
				
			if (!yamlFile.exists()) {
				yamlFile.createNewFile();

				InputStream in = getResource("config.yml");
				OutputStream out = new FileOutputStream(yamlFile);

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}

				in.close();
				out.close();

				log.info("[JSONAPI] config.yml has been copied from the jar");
			}
			if (rtkInstalled && !rtkConfig.exists()) {
				rtkConfig.createNewFile();

				InputStream in = getResource("config_rtk.yml");
				OutputStream out = new FileOutputStream(rtkConfig);

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}

				in.close();
				out.close();

				log.info("[JSONAPI] config_rtk.yml has been copied from the jar");
			}
			if (!groups.exists()) {
				groups.createNewFile();

				InputStream in = getResource("groups.yml");
				OutputStream out = new FileOutputStream(groups);

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}

				in.close();
				out.close();

				log.info("[JSONAPI] groups.yml has been copied from the jar");
			}

			String reconstituted = "";
			int max_queue_age = 30;
			int max_queue_length = 500;
			if (yamlFile.exists()) {
				yamlConfig = new YamlConfiguration();
				yamlConfig.load(yamlFile); // VERY IMPORTANT
				
				yamlConfig.addDefault("method-whitelist", new String[]{ "getPlayerLimit", "dynmap.getPort"} );
				
				MemoryConfiguration stream_pusher_config = new MemoryConfiguration();
				stream_pusher_config.addDefault("max_queue_age", 30);
				stream_pusher_config.addDefault("max_queue_length", 500);
				
				yamlConfig.addDefault("options.stream_pusher", stream_pusher_config);
				yamlConfig.options().copyDefaults(true);
				yamlConfig.save(yamlFile);
				
				logging = yamlConfig.getBoolean("options.log-to-console", true);
				logFile = yamlConfig.getString("options.log-to-file", "false");

				whitelist = yamlConfig.getStringList("options.ip-whitelist");
				for (String ip : whitelist) {
					reconstituted += ip + ",";
				}
				
				max_queue_age = yamlConfig.getInt("max_queue_age", 30);
				max_queue_length = yamlConfig.getInt("max_queue_length", 500);

				salt = yamlConfig.getString("options.salt", "");
				port = yamlConfig.getInt("options.port", 20059);
				startupDelay = yamlConfig.getInt("options.startup-delay", 2000);
				anyoneCanUseCallAdmin = yamlConfig.getBoolean("options.anyone-can-use-calladmin", false);
				allowSendingOldStreamMessages = yamlConfig.getBoolean("options.send-previous-stream-messages", true);
				serverName = getServer().getServerName();
				adminiumEnabled = yamlConfig.getBoolean("options.adminium-push-enabled", true);
				if(yamlConfig.contains("options.use-new-api")) {
					useGroups = yamlConfig.getBoolean("options.use-new-api", false);
				}
				else {
					yamlConfig.set("options.use-new-api", false);
					yamlConfig.save(yamlFile);
				}

				String host = yamlConfig.getString("options.bind-address", "");
				if (host.equals("")) {
					bindAddress = null;
				} else {
					bindAddress = InetAddress.getByName(host);
				}

				method_noauth_whitelist = yamlConfig.getStringList("method-whitelist");

				File usersFile = new File(getDataFolder(), "users.yml");
				if(usersFile.exists()) {
					auth.init();
				}
				else {
					if(!yamlConfig.contains("logins")) {
						usersFile.createNewFile();

						InputStream in = getResource("users.yml");
						OutputStream out = new FileOutputStream(usersFile);

						byte[] buffer = new byte[1024];
						int len;
						while ((len = in.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}

						in.close();
						out.close();

						log.info("[JSONAPI] users.yml has been copied from the jar");
						auth.init();
					}
					else {
						Set<String> logins = ((ConfigurationSection) yamlConfig.get("logins")).getKeys(false);
						
						List<String> fullgroups = new ArrayList<String>();
						fullgroups.add("full_control");
						for (String k : logins) {
							String password = yamlConfig.getString("logins." + k);
							
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("username", k);
							map.put("password", password);
							map.put("groups", fullgroups);
							auth.users.add(map);
						}
						
						yamlConfig.set("logins", null);
						yamlConfig.save(yamlFile);
						
						auth.save();
					}
				}
			}
			
			if (rtkInstalled) {
				YamlConfiguration yamlRTK = new YamlConfiguration();
	
				try {
					yamlRTK.load(rtkConfig);
					
					Properties rtkProps = new Properties();
					rtkProps.load(new FileInputStream("toolkit/remote.properties"));
					
					int port = Integer.parseInt(rtkProps.getProperty("remote-control-port"));
					String salt = rtkProps.getProperty("auth-salt");
					
					rtkAPI = new RTKInterface(port, "localhost", yamlRTK.getString("RTK.username"), yamlRTK.getString("RTK.password"), salt);
					
				} catch (Exception e) {
					// e.printStackTrace();
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

			if (auth.getUsers().size() == 0) {
				log.severe("[JSONAPI] No valid logins for JSONAPI. Check config.yml");
				return;
			}
			
			log.info("[JSONAPI] Logging to file: " + logFile);
			log.info("[JSONAPI] Logging to console: " + String.valueOf(logging));
			log.info("[JSONAPI] IP Whitelist = " + (reconstituted.equals("") ? "None, all requests are allowed." : reconstituted));

			jsonServer = new JSONServer(auth, this, startupDelay);
						
			// add console stream support
			handler = new ConsoleHandler(jsonServer);
			
			new Log4j2ConsoleHandler(jsonServer);

			log.info("[JSONAPI] Attempting to use port " + port);

			jsonSocketServer = new JSONSocketServer(port + 1, jsonServer);
			jsonWebSocketServer = new JSONWebSocketServer(port + 2, jsonServer);
			jsonWebSocketServer.start();
			
			setupSSLWebsockets();
			
			// new PortMapper(this); // map dem ports

			registerStreamManager("chat", getJSONServer().chat);
			registerStreamManager("egg", getJSONServer().eggStream);
			registerStreamManager("formatted_chat", getJSONServer().formattedChat);
			registerStreamManager("console", getJSONServer().console);
			registerStreamManager("connections", getJSONServer().connections);

			streamPusher = new StreamPusher(streamManager, new File(getDataFolder(), "push_locations.yml"), max_queue_age, max_queue_length);
			groupManager = new GroupManager(this);

			initialiseListeners();

			adminium = new PushNotificationDaemon(new File(getDataFolder(), "adminium.yml"), this);
			adminium3 = new Adminium3(this);
						
			tickRateCounter = new TickRateCounter(this);
			
			injector = new JSONAPINettyInjector(this);
			
			// must load this after the tick counter exists!
			registerStreamManager("performance", getJSONServer().performance);
			PerformanceStreamDataProvider.enqueue(this);

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
			} else if (cmd.getName().equals("jsonapi") && args.length > 0 && args[0].equals("status")) {
				jsonServer.connectionInfo();
				return true;
			}
			/*
			 * else if(!adminium.init && args.length > 1 &&
			 * cmd.getName().equals("calladmin")) {
			 * System.out.println(sender.getName() + ": " +
			 * join(Arrays.asList(args), " ")); return true; }
			 */
		}
		if (args.length >= 1 && cmd.getName().equals("calladmin")) {
			adminium3.calladmin(sender, join(Arrays.asList(args), " "));
			
			// adminium 2.x
			if(adminium.init)
				adminium.calladmin(sender, join(Arrays.asList(args), " "));
			
			return true;
		}

		if (cmd.getName().equals("jsonapi") && (sender.hasPermission("jsonapi.command") || sender instanceof ConsoleCommandSender)) {
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
			} else if(subCommand.equals("reloadgroups")) {
				groupManager.loadFromConfig();
				sender.sendMessage("Groups reloaded!");
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
					List<String> usernames = new ArrayList<String>();
					try {
//					for(JSONAPIUser u : getJSONServer().getLogins().getUsers()) {
//						usernames.add(u.getUsername());
//					}
					}catch(Exception e) {
						e.printStackTrace();
					}
					sender.sendMessage("Usernames: " + join(usernames, ", "));
					return true;
				} else if (args.length == 3 && args[2].equals("password")) {
					UsersConfig logins = getJSONServer().getLogins();

					if (!logins.userExists(args[1])) {
						sender.sendMessage(ChatColor.RED + "No JSONAPI user named " + args[1]);
						return true;
					}

					sender.sendMessage(args[1] + "'s password: " + logins.getUser(args[1]).getPassword());
					return true;
				} else if (args.length == 4 && args[1].equals("add")) {
					try {
						String username = args[2];
						String password = args[3];

						List<String> g = new ArrayList<String>();
						g.add("full_control");
//						getJSONServer().getLogins().getUsers().add(new JSONAPIUser(username, password, g));

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
		} /*else if (cmd.getName().equals("adminium")) {
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
		}*/

		return false;
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
	
	public RouteMatcher getRouter() {
		return router;
	}
	
	void setupSSLWebsockets() {
		// load up the key store
		String STORETYPE = "JKS";
		String KEYSTORE = "ssl.jks";
		String STOREPASSWORD = "jsonapi_store";
		String KEYPASSWORD = "jsonapi_key";

		File kf = new File(getDataFolder(), KEYSTORE );

		if (kf.exists()) {
			try {
				KeyStore ks = KeyStore.getInstance(STORETYPE);
				ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());
	
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, KEYPASSWORD.toCharArray());
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				tmf.init(ks);
	
				SSLContext sslContext = null;
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	
				sslJsonWebSocketServer = new JSONWebSocketServer(port + 3, jsonServer);
				sslJsonWebSocketServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
				sslJsonWebSocketServer.start();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDisable() {
		if (jsonServer != null) {
			injector.close();
			
			try {
				log.info("[JSONAPI] Stopping JSON server");
				jsonServer.stop();

				log.info("[JSONAPI] Stopping JSON socket server");
				jsonSocketServer.stop();
				
				log.info("[JSONAPI] Stopping JSON WebSocket server");
				jsonWebSocketServer.stop();

				log.info("[JSONAPI] Tearing down API methods");
				APIWrapperMethods.getInstance().pluginDisable();

				log.info("[JSONAPI] Cancelling performance monitoring");
				getTickRateCounter().cancel();
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.removeHandler(handler);
			
			
		}
	}

	public static Player loadOfflinePlayer(String exactPlayerName) {
		return OfflinePlayerLoader.load(exactPlayerName);
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

		@EventHandler
		public void onPlayerChat(AsyncPlayerChatEvent event) {
			p.jsonServer.logChat(event);
		}

		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			p.jsonServer.logConnected(event.getPlayer().getName());
		}

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			p.jsonServer.logDisconnected(event.getPlayer().getName());
		}

                @EventHandler
                public void onPlayerThrowEgg (PlayerEggThrowEvent event)
                {
                        p.jsonServer.logEggThrow(event);
                }
	}
}
