package com.ramblingwood.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;


/**
*
* @author alecgorge
*/
public class JSONAPI extends JavaPlugin  {
	private PluginLoader pluginLoader;
	private Server server;
	private JSONServer jsonServer;
	
	public boolean logging = false;
	public String logFile = "false";
	public String salt = "";
	public int port = 20059;
	public ArrayList<String> whitelist = new ArrayList<String>();
	
	private Logger log = Logger.getLogger("Minecraft");
	private Logger outLog = Logger.getLogger("JSONAPI");
	
	// for dynamic access
	public static JSONAPI instance;
	
	protected void initalize(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		this.pluginLoader = pluginLoader;
		server = instance;
	}
	
	public JSONAPI () {
		super();
		JSONAPI.instance = this;		
	}
	
	// private JSONApiPlayerListener l = new JSONApiPlayerListener(this);	

	public void onEnable() {
		try {
			Hashtable<String, String> auth = new Hashtable<String, String>();
			
			if(getDataFolder().exists()) {
				getDataFolder().createNewFile();
			}
			// System.out.println(getDataFolder().getAbsolutePath()+"\\JSONAPI.properties");
			PropertiesFile options = new PropertiesFile(new File(getDataFolder().getAbsolutePath()+File.separator+"JSONAPI.properties").getAbsolutePath());
			logging = options.getBoolean("log-to-console", true);
			logFile = options.getString("log-to-file", "false");
			String ipWhitelist = options.getString("ip-whitelist", "false");
			salt = options.getString("salt", "");
			
			String reconstituted = "";
			if(!ipWhitelist.trim().equals("false")) {
				String[] ips = ipWhitelist.split(",");
				for(String ip : ips) {
					reconstituted += ip.trim()+",";
					whitelist.add(ip);
				}
			}
			
			outLog = Logger.getLogger("JSONAPI");
			if(!logging) {
				for(Handler h : outLog.getHandlers()) {
					outLog.removeHandler(h);
				}
			}
			if(logFile != "false") {
				FileHandler fh = new FileHandler(logFile);
				fh.setFormatter(new SimpleFormatter());
				outLog.addHandler(fh);
			}
			
			port = options.getInt("port", 20059);

		    try {
		    	// Open the file that is the first 
		    	// command line parameter
		    	FileInputStream fstream;
		    	File authfile = new File(getDataFolder().getAbsolutePath()+File.separator+"JSONAPIAuthentication.txt");
		    	try {
		    		fstream = new FileInputStream(authfile);
		    	}
		    	catch (FileNotFoundException e) {
		    		authfile.createNewFile();
		    		fstream = new FileInputStream(authfile);
		    	}
		    	// Get the object of DataInputStream
		    	DataInputStream in = new DataInputStream(fstream);
		    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    	String line;
		    	
		    	while ((line = br.readLine()) != null)   {
		    		if(!line.startsWith("#")) {
		    			String[] parts = line.trim().split(":");
		    			if(parts.length == 2) {
		    				auth.put(parts[0], parts[1]);
		    			}
		    		}
		    	}
		    	in.close();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		    if(auth.size() == 0) {
		    	log.severe("[JSONAPI] No valid logins for JSONAPI. Check JSONAPIAuthentication.txt");
		    	return;
		    }
		    
		    log.info("[JSONAPI] Logging to file: "+logFile);
		    log.info("[JSONAPI] Logging to console: "+String.valueOf(logging));
		    log.info("[JSONAPI] IP Whitelist = "+(reconstituted.equals("") ? "None, all requests are allowed." : reconstituted));
		    log.info("[JSONAPI] JSON Server listening on "+port);

		    jsonServer = new JSONServer(auth, this);
			
			initialiseListeners();
		}
		catch( IOException ioe ) {
			log.severe( "[JSONAPI] Couldn't start server!\n");
			ioe.printStackTrace();
			//System.exit( -1 );
		}		
	}
	
	@Override
	public void onDisable(){
		if(jsonServer != null) {
			jsonServer.stop();
		}
	}
	
	private void initialiseListeners(){
		/*PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Type.PLAYER_CHAT, l, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, l, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, l, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_LOGIN, l, Priority.Normal, this);*/
	
		log.info("[JSONAPI] Active and listening for requests.");
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
			e.printStackTrace();
		}
		StringBuffer hexString = new StringBuffer();
		for(int i = 0; i< input.length; i++) {
			String hex = Integer.toHexString(0xFF & input[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	public void disable() {
		jsonServer.stop();
	}
	
	/*public class JSONApiPlayerListener extends PlayerListener {
		JSONAPI p;

		public String join(String[] strings, String separator) {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i < strings.length; i++) {
				if (i != 0) sb.append(separator);
		  		sb.append(strings[i]);
		  	}
		  	return sb.toString();
		}
		
		// This controls the accessability of functions / variables from the main class.
		public JSONApiPlayerListener(JSONAPI plugin) {
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
		public void onPlayerCommandPreprocess(PlayerChatEvent event) {
			HttpStream.log("commands", new String[] {event.getPlayer().getName(), event.getMessage()});
		}
	}*/
}