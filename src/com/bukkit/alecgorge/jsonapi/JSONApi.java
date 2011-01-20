package com.bukkit.alecgorge.jsonapi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
*
* @author alecgorge
*/
public class JSONApi extends JavaPlugin  {
	
	public JSONApi(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	private JSONApiPlayerListener l = new JSONApiPlayerListener(this);
	protected static final Logger log = Logger.getLogger("Minecraft");
	private String name = "JSONApi";
	public static JSONServer server = null;
	public static JSONWebSocket webSocketServer = null;
	private String version = "rev 4";
	public static boolean logging = false;
	public static String fileLogging = "";
	public static Logger outLog = null;
	public static int port = 0;
	public static String salt = "";
	public static int webSocketPort = 0;
	public static ArrayList<String> whitelist = new ArrayList<String>();
	

	public void onEnable() {
		try {
			Hashtable<String, String> auth = new Hashtable<String, String>();
			
			PropertiesFile options = new PropertiesFile("JSONApi.properties");
			logging = options.getBoolean("logToConsole", true);
			fileLogging = options.getString("logToFile", "false");
			String ipWhitelist = options.getString("ipWhitelist", "false");
			salt = options.getString("salt", "");
			
			String reconstituted = "";
			if(!ipWhitelist.trim().equals("false")) {
				String[] ips = ipWhitelist.split(",");
				for(String ip : ips) {
					reconstituted += ip+",";
					whitelist.add(ip);
				}
			}
			
			outLog = Logger.getLogger("JSONApi");
			for(Handler h : outLog.getHandlers()) {
				outLog.removeHandler(h);
			}
			if(logging) {
				StreamHandler hl = new StreamHandler(System.out, new LogFormat());
				outLog.addHandler(hl);
			}
			if(fileLogging != "false") {
				FileHandler fh = new FileHandler(fileLogging);
				fh.setFormatter(new SimpleFormatter());
				outLog.addHandler(fh);
			}
			
			port = options.getInt("port", 20059);
			webSocketPort = options.getInt("webSocketPort", 20060);

		    try {
		    	// Open the file that is the first 
		    	// command line parameter
		    	FileInputStream fstream;
		    	try {
		    		fstream = new FileInputStream("JSONApiAuthentcation.txt");
		    	}
		    	catch (FileNotFoundException e) {
		    		File f = new File("JSONApiAuthentcation.txt");
		    		f.createNewFile();
		    		fstream = new FileInputStream("JSONApiAuthentcation.txt");
		    	}
		    	// Get the object of DataInputStream
		    	DataInputStream in = new DataInputStream(fstream);
		    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    	String line;
		    	
		    	//Read File Line By Line
		    	while ((line = br.readLine()) != null)   {
		    		// 	Print the content on the console
		    		if(!line.startsWith("#")) {
		    			String[] parts = line.trim().split(":");
		    			if(parts.length == 2) {
		    				auth.put(parts[0], parts[1]);
		    			}
		    		}
		    		//System.out.println (strLine);
		    	}
		    	// Close the input stream
		    	in.close();
		    } catch (Exception e){//Catch exception if any
		    	e.printStackTrace();
		    }
		    if(auth.size() == 0) {
		    	log.severe("No valid logins for JSONApi. Check JSONApiAuthentication.txt");
		    	return;
		    }
		    
		    System.setOut(new PrintStream(new HandleStdOut(System.out), true));
		    log.addHandler(new HandleLogger(new LogFormat()));
		    
		    log.info("[JSONApi] Logging = "+(fileLogging != "false" ? fileLogging : "")+","+(logging ? "console" : ""));
		    log.info("[JSONApi] IP Whitelist = "+reconstituted);
		    log.info("[JSONApi] JSON Server listening on "+port);
		    log.info("[JSONApi] WebSocket Server listening on "+webSocketPort);
		    
		    webSocketServer = new JSONWebSocket(webSocketPort);
		    webSocketServer.start();
			server = new JSONServer(auth, this);
			
			initialiseListeners();
		}
		catch( IOException ioe ) {
			log.severe( "Couldn't start server!\n");
			ioe.printStackTrace();
			//System.exit( -1 );
		}		
	}
	
	@Override
	public void onDisable(){
		
	}
	
	private void initialiseListeners(){
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Type.PLAYER_CHAT, l, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_COMMAND, l, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, l, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_LOGIN, l, Priority.Normal, this);
	
		log.info("JSONApi is active and listening for requests.");
	}
	
	/**
	 * From a password, a number of iterations and a salt,
	 * returns the corresponding digest
	 * @param iterationNb int The number of iterations of the algorithm
	 * @param password String The password to encrypt
	 * @param salt byte[] The salt
	 * @return byte[] The digested password
	 * @throws NoSuchAlgorithmException If the algorithm doesn't exist
	 */
	public static String SHA256(String password) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		byte[] input = null;
		try {
			input = digest.digest(password.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer hexString = new StringBuffer();
		for(int i = 0; i< input.length; i++) {
			String hex = Integer.toHexString(0xFF & input[i]);
			if (hex.length() == 1) {
			    // could use a for loop, but we're only dealing with a single byte
			    hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	public void disable() {
		if(server != null) {
			server.stop();
		}
		if(webSocketServer != null) {
			try {
				webSocketServer.stop();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public class JSONApiPlayerListener extends PlayerListener {
		JSONApi p;

	    public String join(String[] strings, String separator) {
	        StringBuffer sb = new StringBuffer();
	        for (int i=0; i < strings.length; i++) {
	            if (i != 0) sb.append(separator);
	      	    sb.append(strings[i]);
	      	}
	      	return sb.toString();
	    }
	    
	    // This controls the accessability of functions / variables from the main class.
		public JSONApiPlayerListener(JSONApi plugin) {
			p = plugin;
		}
		
		@Override
		public void onPlayerChat(PlayerChatEvent event) {
			HttpStream.log("chat", new String[]{event.getPlayer().getName(),event.getMessage()});			
		}
		
		@Override
		public void onPlayerJoin(PlayerEvent event) {
			HttpStream.log("connections", new String[] {"connect", event.getPlayer().getName()});
		}

		@Override
		public void onPlayerQuit(PlayerEvent event) {
			HttpStream.log("connections", new String[] {"disconnect", event.getPlayer().getName()});
		}
		
		@Override
		public void onPlayerCommand(PlayerChatEvent event) {
			HttpStream.log("commands", new String[] {event.getPlayer().getName(), event.getMessage()});
		}
	}
}