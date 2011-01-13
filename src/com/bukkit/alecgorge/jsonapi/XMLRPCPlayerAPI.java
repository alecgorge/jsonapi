package com.bukkit.alecgorge.jsonapi;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Inventory;
import org.bukkit.ItemStack;
import org.bukkit.Location;
import org.bukkit.Player;

/**
 * Shamelessly stolen from sk89...
 * @author sk89q
 */
public class XMLRPCPlayerAPI {
    /**
     * Error code reported when a player cannot be found by his/her name.
     */
    private static final int PLAYER_NOT_FOUND = 1;
	private JSONApi etc;

    public XMLRPCPlayerAPI(JSONApi plugin) {
    	this.etc = plugin;
	}

	/**
     * Gets a player by name.
     *
     * @param name
     * @return
     * @throws APIXMLRPCException
     */
    public Player getPlayerByName(String name) throws APIException {
        for (Player player : etc.getServer().getOnlinePlayers()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }

        throw new APIException(PLAYER_NOT_FOUND, "Name does not exist");
    }

    /**
     * Get the map containing a player's information.
     *
     * @param player
     * @return
     */
    private Map<String,Object> getPlayerInfoMap(Player player) {
        // TODO Find replacement methods for the ones commented out.
    	Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("name", player.getName());
        //kv.put("isAdmin", player.getAdmin());
        //kv.put("canBuild", player.canBuild());
        //kv.put("canIgnoreRestrictions", player.canIgnoreRestrictions());
        //kv.put("canModifyWorld", player.canModifyWorld());
        //kv.put("isMuted", player.isMuted());
        //kv.put("color", player.getColor());
        kv.put("health", player.getHealth());
        kv.put("ip", player.getAddress());//getIP());
        kv.put("itemInHand", player.getItemInHand());
        kv.put("x", player.getLocation().getX());
        kv.put("y", player.getLocation().getY());
        kv.put("z", player.getLocation().getZ());
        kv.put("yaw", (double)player.getLocation().getPitch());
        kv.put("pitch", (double)player.getLocation().getYaw());
        //kv.put("prefix", player.getPrefix());
        return kv;
    }

    /**
     * Gets a list of players and information about them.
     *
     * @return
     */
    public List<Map> getPlayers() {
        List<Map> players = new ArrayList<Map>();

        for (Player player : etc.getServer().getOnlinePlayers()) {
            players.add(getPlayerInfoMap(player));
        }

        return players;
    }

    /**
     * Gets a list of player names.
     *
     * @return
     */
    public List<String> getPlayerNames() {
        List<String> players = new ArrayList<String>();

        for (Player player : etc.getServer().getOnlinePlayers()) {
            players.add(player.getName());
        }

        return players;
    }

    /**
     * Get information about a player.
     *
     * @param name
     * @return
     * @throws APIXMLRPCException
     */
    public Map<String,Object> getPlayerInfo(String name) throws APIException {
        Player player = getPlayerByName(name);
        return getPlayerInfoMap(player);
    }

    /**
     * Give a player an item.
     *
     * @param name
     * @param itemID
     * @param amount
     * @return
     * @throws APIXMLRPCException
     * @return
     */
    public boolean giveItem(String name, int itemID, int amount)
            throws APIException {
        Player player = getPlayerByName(name);
        
        ItemStack stack = new ItemStack(itemID);
        stack.setAmount(amount);
        player.getInventory().addItem(stack);
        
        return true;
    }

    /**
     * Give a player an item drop.
     *
     * @param name
     * @param itemID
     * @param amount
     * @return
     * @throws APIXMLRPCException
     * @return
     */
    public boolean giveItemDrop(String name, int itemID, int amount)
            throws APIException {
        Player player = getPlayerByName(name);
        
        ItemStack stack = new ItemStack(itemID);
        stack.setAmount(amount);
        
        // TODO Check that giving a player a drop does mean dropping it on their location.
        player.getWorld().dropItem(player.getLocation(), stack);
       
        return true;
    }

    /**
     * Set a position of a player.
     *
     * @param name
     * @param x
     * @param y
     * @param z
     * @throws APIXMLRPCException
     * @return
     */
    public boolean teleportTo(String name, double x, double y, double z)
            throws APIException {
        Player player = getPlayerByName(name);
        
        Location newLocation = player.getLocation().clone();
        newLocation.setX(x);
        newLocation.setY(y);
        newLocation.setZ(z);
        
        player.teleportTo(newLocation);
        return true;
    }

    /**
     * Set a position of a player.
     *
     * @param name
     * @param x
     * @param y
     * @param z
     * @param pitch
     * @param yaw
     * @throws APIXMLRPCException
     * @return
     */
    public boolean teleportTo(String name, double x, double y, double z,
            double pitch, double yaw)
            throws APIException {
        Player player = getPlayerByName(name);
        
        Location location = new Location(player.getWorld(), x, y, z);
        location.setPitch((float)pitch);
        location.setYaw((float)yaw);
        
        player.teleportTo(location);
        return true;
    }

    /**
     * Toggles a player's mute status.
     *
     * @param name
     * @return
     * @throws APIXMLRPCException
     */
    public boolean toggleMute(String name) throws APIException {
    	return false;
    	// TODO Figure out the replacement for toggleMute
    	//Player player = getPlayerByName(name);
        //return player.toggleMute();
    }

    /**
     * Kick a player.
     *
     * @param name
     * @param reason
     * @throws APIXMLRPCException
     * @return
     */
    public boolean kick(String name, String reason)
            throws APIException {
    	return false;
    	// TODO Figure out replacement for kick
    	//Player player = getPlayerByName(name);
        //player.kick(reason);
        //return true;
    }

    /**
     * Send a player a message.
     *
     * @param name
     * @param msg
     * @throws APIXMLRPCException
     * @return
     */
    public boolean sendMessage(String name, String msg)
            throws APIException {
        Player player = getPlayerByName(name);
        player.sendMessage(msg);
        return true;
    }

    /**
     * Broadcast a message.
     *
     * @param msg
     * @return
     */
    public boolean broadcastMessage(String msg) {
        for (Player player : etc.getServer().getOnlinePlayers()) {
            player.sendMessage(msg);
        }
        return true;
    }

    /**
     * Get a player's inventory.
     *
     * @param name
     * @throws APIXMLRPCException
     */
    public Map<Integer,Map<String,Integer>> getInventory(String name)
            throws APIException {
        Player player = getPlayerByName(name);
        Inventory  inventory = player.getInventory();
        Map<Integer,Map<String,Integer>> out =
                new HashMap<Integer,Map<String,Integer>>();
        for (int i = 0; i <= 35; i++) {
            addItem(out, inventory, i);
        }
        for (int i = 100; i <= 103; i++) {
            addItem(out, inventory, i);
        }

        return out;
    }

    /**
     * Remove the item in a player's inventory slot.
     *
     * @param name
     * @param slot
     * @return
     * @throws APIException
     */
    public boolean removeInventorySlot(String name, int slot)
            throws APIException {
        // TODO currently no way to remove something from a player's inventory.
    	//Player player = getPlayerByName(name);
        //Inventory inventory = player.getInventory();
        //inventory.removeItemFromSlot(slot);
        return false;
    }

    /**
     * Remove some items from a player's inventory.
     * 
     * @param name
     * @param itemID
     * @param amount
     * @return
     * @throws APIException
     */
    public boolean removeInventoryItem(String name, int itemID, int amount)
            throws APIException {
        // TODO Can't do this either yet.
    	
    	//Player player = getPlayerByName(name);
        //Inventory inventory = player.getInventory();
        //Item item = new Item(itemID, amount);
    	//inventory.removeItem(item);
    	//inventory.update();
        return false;
    }

    /**
     * Helper method for getInventory().
     *
     * @param out
     * @param inventory
     * @param slot
     */
    private void addItem(Map<Integer,Map<String,Integer>> out,
    		Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item != null) {
            Map<String,Integer> kv = new HashMap<String,Integer>();
            kv.put("itemID", item.getTypeID());
            kv.put("amount", item.getAmount());
            out.put(slot, kv);
        }
    }

    /**
     * Get a list of warps accessible by a player.
     *
     * @param name
     * @return
     * @throws APIException
     */
    public String[] getAccessibleWarps(String name)
            throws APIException {
    	// TODO No warp support yet
        //Player player = getPlayerByName(name);
        //return etc.getDataSource().getWarpNames(player).split(",");
    	return new String[0];
    }

    /**
     * Get a list of accessible kits.
     * 
     * @param name
     * @return
     * @throws APIException
     */
    public String[] getAccessibleKits(String name)
            throws APIException {
    	// TODO No kit support yet.
    	//Player player = getPlayerByName(name);
        //return etc.getDataSource().getKitNames(player).split(",");
    	return new String[0];
    }
}
