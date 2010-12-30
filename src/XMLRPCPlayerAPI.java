

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shamelessly stolen from sk89...
 * @author sk89q
 */
public class XMLRPCPlayerAPI {
    /**
     * Error code reported when a player cannot be found by his/her name.
     */
    private static final int PLAYER_NOT_FOUND = 1;

    /**
     * Gets a player by name.
     *
     * @param name
     * @return
     * @throws APIXMLRPCException
     */
    public static Player getPlayerByName(String name) throws APIException {
        for (Player player : etc.getServer().getPlayerList()) {
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
        Map<String,Object> kv = new HashMap<String,Object>();
        kv.put("name", player.getName());
        kv.put("isAdmin", player.getAdmin());
        kv.put("canBuild", player.canBuild());
        kv.put("canIgnoreRestrictions", player.canIgnoreRestrictions());
        kv.put("canModifyWorld", player.canModifyWorld());
        kv.put("isMuted", player.isMuted());
        kv.put("color", player.getColor());
        kv.put("health", player.getHealth());
        kv.put("ip", player.getIP());
        kv.put("itemInHand", player.getItemInHand());
        kv.put("x", player.getLocation().x);
        kv.put("y", player.getLocation().y);
        kv.put("z", player.getLocation().z);
        kv.put("yaw", (double)player.getLocation().rotX);
        kv.put("pitch", (double)player.getLocation().rotY);
        kv.put("prefix", player.getPrefix());
        return kv;
    }

    /**
     * Gets a list of players and information about them.
     *
     * @return
     */
    public List<Map> getPlayers() {
        List<Map> players = new ArrayList<Map>();

        for (Player player : etc.getServer().getPlayerList()) {
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

        for (Player player : etc.getServer().getPlayerList()) {
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
        player.giveItem(itemID, amount);
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
        player.giveItemDrop(itemID, amount);
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
        player.teleportTo(x, y, z, player.getRotation(), player.getPitch());
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
        player.teleportTo(x, y, z, (float)pitch, (float)yaw);
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
        Player player = getPlayerByName(name);
        return player.toggleMute();
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
        Player player = getPlayerByName(name);
        player.kick(reason);
        return true;
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
        for (Player player : etc.getServer().getPlayerList()) {
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
        PlayerInventory  inventory = (PlayerInventory)player.getInventory();
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
        Player player = getPlayerByName(name);
        Inventory inventory = player.getInventory();
        inventory.removeItem(slot);
        inventory.update();
        return true;
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
        Player player = getPlayerByName(name);
        Inventory inventory = player.getInventory();
        Item item = new Item(itemID, amount);
        inventory.removeItem(item);
        inventory.update();
        return true;
    }

    /**
     * Helper method for getInventory().
     *
     * @param out
     * @param inventory
     * @param slot
     */
    private void addItem(Map<Integer,Map<String,Integer>> out,
    		PlayerInventory inventory, int slot) {
        Item item = inventory.getItemFromSlot(slot);
        if (item != null) {
            Map<String,Integer> kv = new HashMap<String,Integer>();
            kv.put("itemID", item.getItemId());
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
        Player player = getPlayerByName(name);
        return etc.getDataSource().getWarpNames(player).split(",");
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
        Player player = getPlayerByName(name);
        return etc.getDataSource().getKitNames(player).split(",");
    }
}
