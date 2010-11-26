

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Shamelessly stolen from sk89...
 * @author sk89q
 */
public class XMLRPCServerAPI {
    private static final int NO_SUCH_PLUGIN = 100;
    private static final int NO_SUCH_BAN = 102;
    private static final int NO_SUCH_KIT = 105;
    
    /**
     * Converts a location to a List.
     * 
     * @param loc
     * @return
     */
    public static List<Double> locationToList(Location loc) {
        List<Double> l = new ArrayList<Double>(5);
        l.add(loc.x);
        l.add(loc.y);
        l.add(loc.z);
        l.add((double)loc.rotX);
        l.add((double)loc.rotY);
        return l;
    }

    /**
     * Converts a location to a map.
     * 
     * @param loc
     * @return
     */
    public static Map<String,Object> locationToMap(Location loc) {
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("x", loc.x);
        kv.put("y", loc.y);
        kv.put("z", loc.z);
        kv.put("pitch", loc.rotY);
        kv.put("yaw", loc.rotX);
        return kv;
    }

    /**
     * Convert arrays of integers stored as strings to List<Integer>.
     * 
     * @param raw
     * @return
     */
    public static List<Integer> integerStringsToList(String[] raw) {
        Set<Integer> items = new HashSet<Integer>();
        for (String n : raw) {
            try {
                int id = Integer.parseInt(n);
                items.add(id);
            } catch (NumberFormatException e) {
            }
        }
        return new ArrayList<Integer>(items);
    }

    /**
     * Convert a list of Integers to an array of Strings.
     * 
     * @param items
     * @return
     */
    public static String[] integerListToStrings(List<Integer> items) {
        Set<String> l = new HashSet<String>();
        for (Integer item : items) {
            l.add(String.valueOf(item));
        }
        return l.toArray(new String[0]);
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
     */
    public Map<String,Object> getSpawnLocation() {
        return locationToMap(etc.getServer().getSpawnLocation());
    }

    /**
     * Run console command in the context of a player.
     *
     * @param command
     * @return
     */
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
     */
    public boolean hasTimerExpired(String name) {
        return etc.getServer().isTimerExpired(name);
    }

    /**
     * Run console command.
     * 
     * @param command
     * @return
     */
    public boolean runConsoleCommand(String command) {
        etc.getServer().useConsoleCommand(command);
        return true;
    }

    /**
     * Get a list of plugins.
     *
     * @return
     */
    public List<Map<String,Object>> getPlugins() {
        PluginLoader loader = etc.getLoader();
        String[] pluginNameList = loader.getPluginList().split(",");
        List<Map<String,Object>> plugins =
                new ArrayList<Map<String,Object>>(pluginNameList.length);
        for (String pluginID : loader.getPluginList().split(",")) {
            pluginID = pluginID.replaceAll(" \\([DE]\\)$", "");
            Plugin plugin = loader.getPlugin(pluginID);
            if (plugin != null) {
                Map<String,Object> kv = new HashMap<String,Object>();
                kv.put("id", pluginID);
                kv.put("enabled", plugin.isEnabled());
                plugins.add(kv);
            }
        }
        return plugins;
    }

    /**
     * Get a plugin.
     *
     * @return
     * @throws APIException
     */
    public Map<String,Object> getPlugin(String id)
            throws APIException {
        PluginLoader loader = etc.getLoader();
        Plugin plugin = loader.getPlugin(id);
        if (plugin == null) {
            throw new APIException(NO_SUCH_PLUGIN, "Plugin does not exist");
        }
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("id", id);
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
        return etc.getLoader().enablePlugin(pluginName);
    }

    /**
     * Disable a plugin.
     *
     * @param pluginName
     * @return
     */
    public boolean disablePlugin(String pluginName) {
        etc.getLoader().disablePlugin(pluginName);
        return true;
    }

    /**
     * Reload a plugin.
     *
     * @param pluginName
     * @return
     */
    public boolean reloadPlugin(String pluginName) {
        etc.getLoader().reloadPlugin(pluginName);
        return true;
    }

    /**
     * Get allowed items.
     *
     * @return
     */
    public List<Integer> getAllowedItems() {
        return integerStringsToList(etc.getInstance().getAllowedItems());
    }

    /**
     * Set allowed items.
     * 
     * @param allowedItems
     * @return
     */
    public boolean setAllowedItems(List<Integer> allowedItems) {
        etc.getInstance().setAllowedItems(integerListToStrings(allowedItems));
        return true;
    }

    /**
     * Get allowed items.
     *
     * @return
     */
    public List<Integer> getDisallowedItems() {
        return integerStringsToList(etc.getInstance().getDisallowedItems());
    }

    /**
     * Set allowed items.
     *
     * @param allowedItems
     * @return
     */
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
     */
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
     */
    public boolean addToReserveList(String name) {
        etc.getDataSource().addToReserveList(name);
        return true;
    }

    /**
     * Remove from reserve list.
     *
     * @param name
     * @return
     */
    public boolean removeFromReserveList(String name) {
        etc.getDataSource().addToReserveList(name);
        return true;
    }

    /**
     * Gets whether the whitelist is enabled.
     *
     * @return
     */
    public boolean isWhitelistEnabled() {
        return etc.getInstance().isWhitelistEnabled();
    }

    /**
     * Sets whether the whitelist is enabled.
     *
     * @return
     */
    public boolean setWhitelistEnabled(boolean enabled) {
        etc.getInstance().setWhitelistEnabled(enabled);
        return true;
    }

    /**
     * Get the whitelist message.
     *
     * @param name
     * @return
     */
    public String getWhitelistMessage() {
        return etc.getInstance().getWhitelistMessage();
    }

    /**
     * Set the whitelist kick message.
     *
     * @return
     */
    public boolean setWhitelistMessage(String message) {
        etc.getInstance().setWhitelistMessage(message);
        return true;
    }

    /**
     * Checks to see if there is a white list.
     *
     * @param name
     * @return
     */
    public boolean hasWhitelist() {
        return etc.getDataSource().hasWhitelist();
    }

    /**
     * Add to white list.
     *
     * @param name
     * @return
     */
    public boolean addToWhitelist(String name) {
        etc.getDataSource().addToWhitelist(name);
        return true;
    }

    /**
     * Remove from white list.
     *
     * @param name
     * @return
     */
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
     */
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
     */
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
     */
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
     */
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
     */
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
     */
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
     */
    public boolean hasKits() {
        return etc.getDataSource().hasKits();
    }

    /**
     * Checks to see if there are warps.
     *
     * @return
     */
    public boolean hasWarps() {
        return etc.getDataSource().hasWarps();
    }

    /**
     * Reload the ban list.
     *
     * @return
     */
    public boolean reloadBanList() {
        etc.getDataSource().loadBanList();
        return true;
    }

    /**
     * Reload groups.
     *
     * @return
     */
    public boolean reloadGroups() {
        etc.getDataSource().loadGroups();
        return true;
    }

    /**
     * Reload homes.
     *
     * @return
     */
    public boolean reloadHomes() {
        etc.getDataSource().loadHomes();
        return true;
    }

    /**
     * Reload kits.
     *
     * @return
     */
    public boolean reloadKits() {
        etc.getDataSource().loadKits();
        return true;
    }

    /**
     * Reload the warps.
     *
     * @return
     */
    public boolean reloadWarps() {
        etc.getDataSource().loadWarps();
        return true;
    }

    /**
     * Get the MOTD.
     *
     * @return
     */
    public String getMotd() {
        return joinString(etc.getInstance().getMotd(), "\n").replace("\r", "");
    }

    /**
     * Set the MOTD.
     *
     * @return
     */
    public boolean setMotd(String motd) {
        etc.getInstance().setMotd(motd.replace("\r", "").split("\n"));
        return true;
    }

    /**
     * Get the number of players.
     *
     * @return
     */
    public int getPlayerCount() {
        return etc.getServer().getPlayerList().size();
    }

    /**
     * Get the maximum number of players.
     *
     * @return
     */
    public int getPlayerLimit() {
        return etc.getInstance().getPlayerLimit();
    }

    /**
     * Set the maximum number of players.
     *
     * @return
     */
    public boolean setPlayerLimit(int limit) {
        etc.getInstance().setPlayerLimit(limit);
        return true;
    }

    /**
     * Get the spawn protection size.
     *
     * @return
     */
    public int getSpawnProtectionSize() {
        return etc.getInstance().getSpawnProtectionSize();
    }

    /**
     * Set the spawn protection size.
     *
     * @return
     */
    public boolean setSpawnProtectionSize(int size) {
        etc.getInstance().setSpawnProtectionSize(size);
        return true;
    }
}
