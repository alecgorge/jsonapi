package com.bukkit.alecgorge.jsonapi;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;

/**
 * Shamelessly stolen from sk89...
 * @author sk89q
 */
public class XMLRPCServerAPI {
    private static final int NO_SUCH_PLUGIN = 100;
    private static final int NO_SUCH_BAN = 102;
    private static final int NO_SUCH_KIT = 105;
	private JSONApi etc;
    
    public XMLRPCServerAPI(JSONApi plugin) {
		this.etc = plugin;
	}

	/**
     * Converts a location to a List.
     * 
     * @param loc
     * @return
     */
    public static List<Double> locationToList(Location loc) {
        List<Double> l = new ArrayList<Double>(5);
        l.add(loc.getX());
        l.add(loc.getY());
        l.add(loc.getZ());
        l.add((double)loc.getPitch());
        l.add((double)loc.getYaw());
        return l;
    }
    
    public static String getFileContents (String file) {
    	FileInputStream fstream = null;
    	String out = "";
    	String thisLine;
    	try {
			fstream = new FileInputStream(new File(System.getProperty("user.dir")+file));
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fstream));
			try {
				while ((thisLine = myInput.readLine()) != null) {  
					out += thisLine+"\n";
				}
			} catch (IOException e) {
				 
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			 
			e.printStackTrace();
		}
		return out;
    }
    
    public static boolean setFileContents (String file, String contents) {
    	FileWriter fstream;
		try {
			File tfile = new File(System.getProperty("user.dir")+file);
			tfile.delete();
			
			fstream = new FileWriter(tfile);
	    	BufferedWriter out = new BufferedWriter(fstream);
	    	out.write(contents);
	    	out.close();
		} catch (IOException e) {
			 
			e.printStackTrace();
			return false;
		}
		return true;
    }

    /**
     * Converts a location to a map.
     * 
     * @param loc
     * @return
     */
    public static Map<String,Object> locationToMap(Location loc) {
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("x", loc.getX());
        kv.put("y", loc.getY());
        kv.put("z", loc.getZ());
        kv.put("pitch", loc.getPitch());
        kv.put("yaw", loc.getYaw());
        return kv;
    }

    /**
     * Convert arrays of integers stored as strings to List<Integer>.
     * 
     * @param raw
     * @return
     */
    public static List<Integer> integerStringsToList(Set<Integer> items) {
        return new ArrayList<Integer>(items);
    }

    /**
     * Convert a list of Integers to an array of Strings.
     * 
     * @param items
     * @return
     */
    public static int[] integerListToStrings(List<Integer> items) {
        int[] arr = new int[items.size()];
        
        int count = 0;
        for(Integer i : items) {
        	arr[count] = i; 
        	count++;
        }
        
        return arr;
    }

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    public static String joinString(String[] str, String delimiter) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }

    /**
     * Get the spawn location.
     * 
     * @return
     *\/
    public Map<String,Object> getSpawnLocation() {
    	return locationToMap(etc.getServer().getWorlds()[0].ge);
    }*/

    /**
     * Run console command in the context of a player.
     *
     * @param command
     * @return
     *\/
    public boolean runConsoleCommand(String command, String player)
            throws APIException {
        etc.getServer().useConsoleCommand(command,
                XMLRPCPlayerAPI.getPlayerByName(player));
        return true;
    }

    /**
     * Returns whether a timer has expired.
     * 
     * @param name
     * @return
     *\/
    public boolean hasTimerExpired(String name) {
        return etc.getServer().isTimerExpired(name);
    }

    /**
     * Run console command.
     * 
     * @param command
     * @return
     *\/
    public boolean runConsoleCommand(String command) {
        etc.getServer().useConsoleCommand(command);
        return true;
    }*/

    /**
     * Get a list of plugins.
     *
     * @return
     */
    public List<Map<String,Object>> getPlugins() {
    	PluginManager loader = etc.getServer().getPluginManager();
    	
    	Plugin[] plugins = loader.getPlugins();
    	List<Map<String,Object>> pluginsr =
            new ArrayList<Map<String,Object>>(plugins.length);
    	
    	for(Plugin plugin : plugins) {
            Map<String,Object> kv = new HashMap<String,Object>();
            kv.put("name", plugin.getDescription());
            kv.put("enabled", plugin.isEnabled());
    		
    		pluginsr.add(kv);
    	}
        return pluginsr;
    }

    /**
     * Get a plugin.
     *
     * @return
     * @throws APIException
     */
    public Map<String,Object> getPlugin(String id)
            throws APIException {
    	PluginManager loader = etc.getServer().getPluginManager();
        Plugin plugin = loader.getPlugin(id);
        if (plugin == null) {
            throw new APIException(NO_SUCH_PLUGIN, "Plugin does not exist");
        }
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("name", id);
        kv.put("enabled", plugin.isEnabled());
        return kv;
    }

    /**
     * Enable a plugin.
     *
     * @param pluginName
     * @return
     */
    public boolean enablePlugin(String pluginName) {
        etc.getServer().getPluginManager().enablePlugin(etc.getServer().getPluginManager().getPlugin(pluginName));
        return true;
    }

    /**
     * Disable a plugin.
     *
     * @param pluginName
     * @return
     */
    public boolean disablePlugin(String pluginName) {
        etc.getServer().getPluginManager().disablePlugin(etc.getServer().getPluginManager().getPlugin(pluginName));
        return true;
    }

    /**
     * Reload a plugin.
     *
     * @param pluginName
     * @return
     */
    public boolean reloadPlugin(String pluginName) {
        disablePlugin(pluginName);
        enablePlugin(pluginName);
        return true;
    }

    /**
     * Get allowed items.
     *
     * @return
     *\/
    public List<Integer> getAllowedItems() {
        return integerStringsToList(etc.getInstance().getAllowedItems());
    }

    /**
     * Set allowed items.
     * 
     * @param allowedItems
     * @return
     *\/
    public boolean setAllowedItems(List<Integer> allowedItems) {
        etc.getInstance().setAllowedItems(integerListToStrings(allowedItems));
        return true;
    }

    /**
     * Get allowed items.
     *
     * @return
     *\/
    public List<Integer> getDisallowedItems() {
        return integerStringsToList(etc.getInstance().getDisallowedItems());
    }

    /**
     * Set allowed items.
     *
     * @param allowedItems
     * @return
     *\/
    public boolean setDisallowedItems(List<Integer> disallowedItems) {
        etc.getInstance().setDisallowedItems(integerListToStrings(disallowedItems));
        return true;
    }

    /**
     * Adds a group.
     * 
     * @param name
     * @param prefix
     * @param parents
     * @param commands
     * @param isAdministrator
     * @param canModifyWorld
     * @param ignoresRestrictions
     * @param isDefault
     * @return
     *\/
    public boolean addGroup(String name, String prefix, String[] parents,
            String[] commands, boolean isAdministrator, boolean canModifyWorld,
            boolean ignoresRestrictions, boolean isDefault) {
        Group group = new Group();
        group.Administrator = isAdministrator;
        group.CanModifyWorld = canModifyWorld;
        group.Commands = commands;
        group.DefaultGroup = isDefault;
        group.IgnoreRestrictions = ignoresRestrictions;
        group.InheritedGroups = parents;
        group.Name = name;
        group.Prefix = prefix;
        etc.getDataSource().addGroup(group);
        return true;
    }

    /**
     * Add to reserve list.
     *
     * @param name
     * @return
     *\/
    public boolean addToReserveList(String name) {
        etc.getDataSource().addToReserveList(name);
        return true;
    }

    /**
     * Remove from reserve list.
     *
     * @param name
     * @return
     *\/
    public boolean removeFromReserveList(String name) {
        etc.getDataSource().addToReserveList(name);
        return true;
    }

    /**
     * Gets whether the whitelist is enabled.
     *
     * @return
     *\/
    public boolean isWhitelistEnabled() {
        return etc.getInstance().isWhitelistEnabled();
    }

    /**
     * Sets whether the whitelist is enabled.
     *
     * @return
     *\/
    public boolean setWhitelistEnabled(boolean enabled) {
        etc.getInstance().setWhitelistEnabled(enabled);
        return true;
    }

    /**
     * Get the whitelist message.
     *
     * @param name
     * @return
     *\/
    public String getWhitelistMessage() {
        return etc.getInstance().getWhitelistMessage();
    }

    /**
     * Set the whitelist kick message.
     *
     * @return
     *\/
    public boolean setWhitelistMessage(String message) {
        etc.getInstance().setWhitelistMessage(message);
        return true;
    }

    /**
     * Checks to see if there is a white list.
     *
     * @param name
     * @return
     *\/
    public boolean hasWhitelist() {
        return etc.getDataSource().hasWhitelist();
    }

    /**
     * Add to white list.
     *
     * @param name
     * @return
     *\/
    public boolean addToWhitelist(String name) {
        etc.getDataSource().addToWhitelist(name);
        return true;
    }

    /**
     * Remove from white list.
     *
     * @param name
     * @return
     *\/
    public boolean removeFromWhitelist(String name) {
        etc.getDataSource().addToWhitelist(name);
        return true;
    }

    /**
     * Modify a ban.
     * 
     * @param name
     * @param ip
     * @param reason
     * @param timestamp
     * @return
     * @throws APIException
     *\/
    public boolean modifyBan(String name, String ip, String reason, int timestamp)
            throws APIException {
        Ban ban = etc.getDataSource().getBan(name, ip);
        if (ban == null) {
            throw new APIException(NO_SUCH_BAN, "Ban does not exist");
        }
        ban.setReason(reason);
        ban.setTimestamp(timestamp);
        etc.getDataSource().modifyBan(ban);
        return true;
    }

    /**
     * Get a ban.
     *
     * @return
     * @throws APIException
     *\/
    public Map<String,Object> getBan(String name, String ip)
            throws APIException {
        Ban ban = etc.getDataSource().getBan(name, ip);
        if (ban == null) {
            throw new APIException(NO_SUCH_BAN, "Ban does not exist");
        }
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("id", ban.getId());
        kv.put("name", ban.getName());
        kv.put("ip", ban.getIp());
        kv.put("reason", ban.getReason());
        kv.put("timestamp", ban.getTimestamp());
        return kv;
    }

    /**
     * Checks if a user is banned.
     * 
     * @param name
     * @param ip
     * @return
     *\/
    public boolean isBanned(String name, String ip) {
        return etc.getDataSource().isOnBanList(name, ip);
    }

    /**
     * Adds a kit.
     * 
     * @param name
     * @param group
     * @param items
     * @param delay
     * @return
     *\/
    public boolean addKit(String name, String group, Map<String,Integer> items,
            int delay) {
        Kit kit = new Kit();
        kit.Delay = delay;
        kit.Group = group;
        kit.IDs = items;
        kit.Name = name;
        etc.getDataSource().addKit(kit);
        return true;
    }

    /**
     * Modify a kit.
     *
     * @param name
     * @param newName
     * @param group
     * @param items
     * @param delay
     * @return
     * @throws APIException
     *\/
    public boolean modifyKit(String name, String newName, String group,
            Map<String,Integer> items, int delay)
            throws APIException {
        Kit kit = etc.getDataSource().getKit(name);
        if (kit == null) {
            throw new APIException(NO_SUCH_KIT, "Kit does not exist");
        }
        kit.Delay = delay;
        kit.Group = group;
        kit.IDs = items;
        kit.Name = newName;
        etc.getDataSource().modifyKit(kit);
        return true;
    }

    /**
     * Get a kit.
     *
     * @return
     * @throws APIException
     *\/
    public Map<String,Object> getKit(String id)
            throws APIException {
        Kit kit = etc.getDataSource().getKit(id);
        if (kit == null) {
            throw new APIException(NO_SUCH_KIT, "Kit does not exist");
        }
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("name", kit.Name);
        kv.put("delay", kit.Delay);
        kv.put("group", kit.Group);
        kv.put("items", kit.IDs);
        return kv;
    }

    /**
     * Checks to see if there are kits.
     *
     * @param name
     * @return
     *\/
    public boolean hasKits() {
        return etc.getDataSource().hasKits();
    }

    /**
     * Checks to see if there are warps.
     *
     * @return
     *\/
    public boolean hasWarps() {
        return etc.getDataSource().hasWarps();
    }

    /**
     * Reload the ban list.
     *
     * @return
     *\/
    public boolean reloadBanList() {
        etc.getDataSource().loadBanList();
        return true;
    }

    /**
     * Reload groups.
     *
     * @return
     *\/
    public boolean reloadGroups() {
        etc.getDataSource().loadGroups();
        return true;
    }

    /**
     * Reload homes.
     *
     * @return
     *\/
    public boolean reloadHomes() {
        etc.getDataSource().loadHomes();
        return true;
    }

    /**
     * Reload kits.
     *
     * @return
     *\/
    public boolean reloadKits() {
        etc.getDataSource().loadKits();
        return true;
    }

    /**
     * Reload the warps.
     *
     * @return
     *\/
    public boolean reloadWarps() {
        etc.getDataSource().loadWarps();
        return true;
    }

    /**
     * Get the MOTD.
     *
     * @return
     *\/
    public String getMotd() {
        return joinString(etc.getInstance().getMotd(), "\n").replace("\r", "");
    }

    /**
     * Set the MOTD.
     *
     * @return
     *\/
    public boolean setMotd(String motd) {
        etc.getInstance().setMotd(motd.replace("\r", "").split("\n"));
        return true;
    }

    /**
     * Get the number of players.
     *
     * @return
     *\/
    public int getPlayerCount() {
        return etc.getServer().getPlayerList().size();
    }

    /**
     * Get the maximum number of players.
     *
     * @return
     *\/
    public int getPlayerLimit() {
        return etc.getInstance().getPlayerLimit();
    }

    /**
     * Set the maximum number of players.
     *
     * @return
     *\/
    public boolean setPlayerLimit(int limit) {
        etc.getInstance().setPlayerLimit(limit);
        return true;
    }

    /**
     * Get the spawn protection size.
     *
     * @return
     *\/
    public int getSpawnProtectionSize() {
        return etc.getInstance().getSpawnProtectionSize();
    }

    /**
     * Set the spawn protection size.
     *
     * @return
     *\/
    public boolean setSpawnProtectionSize(int size) {
        etc.getInstance().setSpawnProtectionSize(size);
        return true;
    }*/
    
}
