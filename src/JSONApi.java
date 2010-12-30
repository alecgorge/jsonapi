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


/**
*
* @author alecgorge
*/
public class JSONApi extends Plugin  {
	private Listener l = new Listener(this);
	protected static final Logger log = Logger.getLogger("Minecraft");
	private String name = "JSONApi";
	public static JSONServer server = null;
	public static JSONWebSocket webSocketServer = null;
	private String version = "rev 3";
	public static boolean logging = false;
	public static String fileLogging = "";
	public static Logger outLog = null;
	public static int port = 0;
	public static int webSocketPort = 0;
	public static ArrayList<String> whitelist = new ArrayList<String>();
	

	public void enable() {
		try {
			Hashtable<String, String> auth = new Hashtable<String, String>();
			
			PropertiesFile options = new PropertiesFile("JSONApi.properties");
			logging = options.getBoolean("logToConsole", true);
			fileLogging = options.getString("logToFile", "false");
			String ipWhitelist = options.getString("ipWhitelist", "false");
			
			if(ipWhitelist != "false") {
				String[] ips = ipWhitelist.split(",");
				for(String ip : ips) {
					whitelist.add(ip);
				}
			}
			
			outLog = Logger.getLogger("JSONApi");
			if(logging) {
				Handler[] h = log.getHandlers();
				for(int i = 0; i < h.length; i++) {
					outLog.addHandler(h[i]);
				}
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
		    
		    webSocketServer = new JSONWebSocket(webSocketPort);
		    webSocketServer.start();
			server = new JSONServer(auth);
		}
		catch( IOException ioe ) {
			log.severe( "Couldn't start server!\n");
			ioe.printStackTrace();
			//System.exit( -1 );
		}		
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
		return new String(input);
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

	public void initialize() {
		log.info("JSONApi is active and listening for requests.");
		// Uncomment as needed.
		//etc.getLoader().addListener( PluginLoader.Hook.ARM_SWING, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.BLOCK_CREATED, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.BLOCK_DESTROYED, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.CHAT, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.COMMAND, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.COMPLEX_BLOCK_CHANGE, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.COMPLEX_BLOCK_SEND, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.DISCONNECT, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.INVENTORY_CHANGE, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.IPBAN, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.KICK, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.LOGIN, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.LOGINCHECK, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.NUM_HOOKS, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.PLAYER_MOVE, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.SERVERCOMMAND, l, this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.TELEPORT, l, this, PluginListener.Priority.MEDIUM);
	}
	
	public class Listener extends PluginListener {
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
		public Listener(JSONApi plugin) {
			p = plugin;
		}
		
		public boolean onChat(Player player, String message) {
			HttpStream.log("chat", new String[]{player.getName(),message});
			
			return false;
		}
		
		public void onDisconnect (Player player) {
			HttpStream.log("connections", new String[] {"disconnect", player.getName()});
		}
		
		public void onLogin (Player player) {
			HttpStream.log("connections", new String[] {"connect", player.getName()});
		}
		
		public boolean onCommand (Player player, String[] split) {
			HttpStream.log("commands", new String[] {player.getName(), join(split, " ")});
			
			return false;
		}
	}
}