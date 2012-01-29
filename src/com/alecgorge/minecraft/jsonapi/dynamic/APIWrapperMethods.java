package com.alecgorge.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.APIException;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterface.CommandType;
import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterfaceException;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
import com.alecgorge.minecraft.jsonapi.util.PropertiesFile;
import com.alecgorge.minecraft.jsonapi.util.RecursiveDirLister;
import com.alecgorge.minecraft.permissions.PermissionWrapper;

public class APIWrapperMethods {
	private Logger log = Logger.getLogger("Minecraft");
	private Logger outLog = Logger.getLogger("JSONAPI");
	public PermissionWrapper permissions;
	public NetworkManager manager;
	
	public APIWrapperMethods(Server server) {
		permissions = new PermissionWrapper(server);
	}

	private Server Server = JSONAPI.instance.getServer();
	private static APIWrapperMethods instance;
	
	public static APIWrapperMethods getInstance () {
		if(instance == null) {
			instance = new APIWrapperMethods(JSONAPI.instance.getServer());
		}
		return instance;
	}
	
	public HashMap<Integer,ItemStack> removePlayerInventoryItem (String playerName, int itemID) {
		try {
			return Server.getPlayerExact(playerName).getInventory().removeItem(new ItemStack(itemID));
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	
	public boolean removeEnchantmentsFromPlayerInventorySlot (String playerName, int slot, List<Object> enchantments) {
		try {
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it;
			
			if(slot == inv.getHeldItemSlot()) it = inv.getHelmet();
			else if(slot == 102) it = inv.getChestplate();
			else if(slot == 101) it = inv.getLeggings();
			else if(slot == 100) it = inv.getBoots();
			else it = inv.getItem(slot);
			
			for(Object o : enchantments) {
				it.removeEnchantment(Enchantment.getById(Integer.valueOf(o.toString())));
			}
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean addEnchantmentToPlayerInventorySlot (String playerName, int slot, int enchantmentID, int level) {
		try {
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it;
			
			if(slot == inv.getHeldItemSlot()) it = inv.getHelmet();
			else if(slot == 102) it = inv.getChestplate();
			else if(slot == 101) it = inv.getLeggings();
			else if(slot == 100) it = inv.getBoots();
			else it = inv.getItem(slot);
			
			it.addEnchantment(Enchantment.getById(enchantmentID), level);
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean addEnchantmentsToPlayerInventorySlot (String playerName, int slot, List<Object> enchantments) {
		try {
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it;
			
			if(slot == inv.getHeldItemSlot()) it = inv.getHelmet();
			else if(slot == 102) it = inv.getChestplate();
			else if(slot == 101) it = inv.getLeggings();
			else if(slot == 100) it = inv.getBoots();
			else it = inv.getItem(slot);
			
			for(int i = 0; i < enchantments.size(); i++) {
				JSONObject o = (JSONObject) enchantments.get(i);
				it.addEnchantment(Enchantment.getById(Integer.valueOf(o.get("enchantment").toString())), Integer.valueOf(o.get("level").toString()));
			}
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean setPlayerInventorySlot (String playerName, int slot, int blockID, int quantity) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}
			
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it = new ItemStack(blockID, quantity);
			
			if(slot == 103) inv.setHelmet(it);
			else if(slot == 102) inv.setChestplate(it);
			else if(slot == 101) inv.setLeggings(it);
			else if(slot == 100) inv.setBoots(it);
			else inv.setItem(slot, it);
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean setPlayerInventorySlotWithData(String playerName, int slot, int blockID, final int data, int quantity) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}
			
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it = (new MaterialData(blockID, (byte)data)).toItemStack(quantity);
			
			if(slot == 103) inv.setHelmet(it);
			else if(slot == 102) inv.setChestplate(it);
			else if(slot == 101) inv.setLeggings(it);
			else if(slot == 100) inv.setBoots(it);
			else inv.setItem(slot, it);
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
		
	}
	
	public boolean setPlayerInventorySlotWithDataAndDamage(String playerName, int slot, int blockID, final int data, int damage, int quantity) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}
			
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it = (new MaterialData(blockID, (byte)data)).toItemStack(quantity);
			it.setDurability(Short.valueOf(String.valueOf(damage)).shortValue());
			
			if(slot == 103) inv.setHelmet(it);
			else if(slot == 102) inv.setChestplate(it);
			else if(slot == 101) inv.setLeggings(it);
			else if(slot == 100) inv.setBoots(it);
			else inv.setItem(slot, it);
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
		
	}
	
	public boolean setPlayerInventorySlotWithDataDamageAndEnchantments(String playerName, int slot, int blockID, final int data, int damage, int quantity, List<Object> enchantments) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}
			
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it = (new MaterialData(blockID, (byte)data)).toItemStack(quantity);
			it.setDurability(Short.valueOf(String.valueOf(damage)).shortValue());
			
			for(int i = 0; i < enchantments.size(); i++) {
				JSONObject o = (JSONObject) enchantments.get(i);
				it.addEnchantment(Enchantment.getById(Integer.valueOf(o.get("enchantment").toString())), Integer.valueOf(o.get("level").toString()));
			}
			
			if(slot == 103) inv.setHelmet(it);
			else if(slot == 102) inv.setChestplate(it);
			else if(slot == 101) inv.setLeggings(it);
			else if(slot == 100) inv.setBoots(it);
			else inv.setItem(slot, it);
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
		
	}
	
	public boolean setPlayerInventorySlot (String playerName, int slot, int blockID, int damage, int quantity) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			ItemStack it = new ItemStack(blockID, quantity, Short.valueOf(String.valueOf(damage)).shortValue());
			
			if(slot == 103) inv.setHelmet(it);
			else if(slot == 102) inv.setChestplate(it);
			else if(slot == 101) inv.setLeggings(it);
			else if(slot == 100) inv.setBoots(it);
			else inv.setItem(slot, it);
			
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public void setPlayerGameMode(String playerName, int gameMode) throws Exception {
		Server.getPlayerExact(playerName).setGameMode(GameMode.getByValue(gameMode));
	}
	
	public boolean clearPlayerInventorySlot (String playerName, int slot) {
		try {
			PlayerInventory inv = Server.getPlayerExact(playerName).getInventory();
			int cnt = inv.getSize();
			
			if(slot == 103) inv.clear(cnt + 3);
			else if(slot == 102) inv.clear(cnt + 2);
			else if(slot == 101) inv.clear(cnt + 1);
			else if(slot == 100) inv.clear(cnt + 0);
			else inv.clear(slot);

			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean updatePlayerInventorySlot(String playerName, int slot, int newAmount) {
		try {
			ItemStack s = Server.getPlayerExact(playerName).getInventory().getItem(slot);
			s.setAmount(newAmount);
			Server.getPlayerExact(playerName).getInventory().setItem(slot, s);
			
			return true;
		}
		catch(NullPointerException e) {
			return false;
		}
	}
	
	public List<String> getPluginFiles(String pluginName) {
		try {
			File dir = Server.getPluginManager().getPlugin(pluginName).getDataFolder();
			RecursiveDirLister d = new RecursiveDirLister(dir);
			
			return d.getFileListing();
		}
		catch(Exception e) {
			// e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	class FauxPlayer extends CraftPlayer {
		String name;
		
		public FauxPlayer(String name, FauxEntityPlayer ent) {
			super((CraftServer) JSONAPI.instance.getServer(), ent);

			this.name = name;
			
			((FauxNetServerHandler)getHandle().netServerHandler).setPlayer(this);
		}

		@Override
		public String getName () {
			return name;
		}
		
		@Override
		public boolean isOnline () {
			return true;
		}
		
		@Override
		public boolean isOp () {
			return true;
		}
		
		@Override
		public void sendMessage(String message) {
			System.out.println("message:"+message);
		}
	}
	
	class FauxEntityPlayer extends EntityPlayer {

		public FauxEntityPlayer(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager) {
			super(minecraftserver, world, s, iteminworldmanager);
			
			Socket ss = null;
			try {
				ss = new Socket("localhost", fauxPort);
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
			NetworkManager m = new NetworkManager(ss, "???", new NetHandler() {
				
				@Override
				public boolean c() {
					// TODO Auto-generated method stub
					return false;
				}
			});

			netServerHandler = new FauxNetServerHandler(((CraftServer)getServer()).getServer(), m, this);
			
			try {
				ss.close();
			} catch (IOException e) {
			}
		}
		
	}
	
	class FauxNetServerHandler extends NetServerHandler {
		private CraftPlayer _player;
	
		public FauxNetServerHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
			super(minecraftserver, networkmanager, entityplayer);
		}
		
		public void setPlayer(CraftPlayer p) {
			_player = p;
		}
		
		public CraftPlayer getPlayerExact() {
			return _player;
		}
	}
	
	private HashMap<String, FauxPlayer> joinedList = new HashMap<String, FauxPlayer>();
	private ServerSocket fauxServer = null;
	private int fauxPort = 0;
	
	@SuppressWarnings("unchecked")
	public boolean chatWithName(String message, String name) {
		if(fauxServer == null) {
			try {
				fauxServer = new ServerSocket(0);
				fauxPort = fauxServer.getLocalPort();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			FauxPlayer player;
			if(joinedList.containsKey(name)) {
				player = joinedList.get(name);
			}
			else {
				// this is the biggest hack ever.
				player = new FauxPlayer(name, new FauxEntityPlayer(
						((CraftServer)Server).getServer(),
						((CraftWorld)Server.getWorlds().get(0)).getHandle(),
						name,
						new ItemInWorldManager(((CraftServer)Server).getServer().getWorldServer(0))
					)
				);
				joinedList.put(name, player);
				
				// default join event from CraftBukkit / src / main / java / net / minecraft / server / ServerConfigurationManager.java
				// PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player, "\u00A7e" + player.getName() + " joined the game.");
				// Server.getPluginManager().callEvent(playerJoinEvent);				
			}
			
			// for some reason this is need to prevent the chat event being processed before the join event
			// Thread.sleep(500);
			
			((CraftServer)Server).getServer().server.getHandle().players.add(player.getHandle()); 
		
			// copied from CraftBukkit / src / main / java / net / minecraft / server / NetServerHandler.java 
			PlayerChatEvent event = new PlayerChatEvent(player, message);
			Server.getPluginManager().callEvent(event);
			
			((CraftServer)Server).getServer().server.getHandle().players.remove(player.getHandle()); 
	
			// NOTE: HeroChat always cancels
			if (event.isCancelled()) {
				return true;
			}
	
			// default message from CraftBukkit / src / main / java / net / minecraft / server / NetServerHandler.java#chat(String s)
			message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
			Logger.getLogger("Minecraft").info(message);
			for (Player recipient : event.getRecipients()) {
				recipient.sendMessage(message);
			}
						
			// end biggest hack ever			
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void disconnectAllFauxPlayers () {
		for(String name : joinedList.keySet()) {
			Player player = joinedList.get(name);
			PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(player, "\u00A7e" + player.getName() + " left the game.");
			Server.getPluginManager().callEvent(playerQuitEvent);			
		}
	}

	public List<String> getWhitelist () throws APIException {
		List<String> a = new ArrayList<String>();
		for(OfflinePlayer p : Server.getWhitelistedPlayers()) {
			a.add(p.getName());
		}
		return a;
	}
	
	public List<String> getBannedPlayers () throws APIException {
		List<String> a = new ArrayList<String>();
		for(OfflinePlayer p : Server.getBannedPlayers()) {
			a.add(p.getName());
		}
		return a;
	}
	
	public List<String> getBannedIPs () throws APIException {
		return new ArrayList<String>(Server.getIPBans());
	}
	
	public boolean banWithReason(String name, String reason) {
		try {
			Bukkit.getOfflinePlayer(name).setBanned(true);
			Bukkit.getPlayerExact(name).kickPlayer(reason);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public boolean enablePlugin(String name) {
		try {
			Server.getPluginManager().enablePlugin(Server.getPluginManager().getPlugin(name));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean disablePlugin(String name) {
		try {
			Server.getPluginManager().disablePlugin(Server.getPluginManager().getPlugin(name));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}			
	}
	
	public boolean giveItem (String name, int id, int quant) {
		try {
			Server.getPlayerExact(name).getInventory().addItem(new ItemStack(id, quant));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}			
	}
	
	public boolean giveItem (String name, int id, int quant, int data) throws Exception {
		try {
			if(data < 0 || data > 15) {
				throw new Exception("The given data needs to be in decimal form and between 0 and 15");
			}
			Server.getPlayerExact(name).getInventory().addItem(new ItemStack(id, quant, (short)0, Byte.valueOf(String.valueOf(data)).byteValue()));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}			
	}
	
	public boolean giveItemDrop (String name, int id, int quant) {
		try {
			Server.getPlayerExact(name).getWorld().dropItem(Server.getPlayerExact(name).getLocation(), new ItemStack(id, quant));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean giveItemDrop (String name, int id, int quant, int data) throws Exception {
		try {
			if(data < 0 || data > 15) {
				throw new Exception("The given data needs to be in decimal form and between 0 and 15");
			}
			Server.getPlayerExact(name).getWorld().dropItem(Server.getPlayerExact(name).getLocation(), new ItemStack(id, quant, (short)0, Byte.valueOf(String.valueOf(data)).byteValue()));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}			
	}
	
	public void runCommand (String...obj) {
		StringBuilder command = new StringBuilder();
		for(String s : obj) {
			command.append(s);
		}
		
		String cmd = command.toString();
		
		outLog.info("Command run by remote user: '" + cmd + "'");
		
		Server.dispatchCommand(getServer().getConsoleSender(), cmd);
	}
	
	public void runCommand (String obj) {
		runCommand(new String[] {obj});
	}
	
	public void runCommand (String obj, String obj2) {
		runCommand(new String[] {obj, obj2});
	}
	
	public void runCommand (String obj, String obj2, String obj3) {
		runCommand(new String[] {obj, obj2, obj3});
	}
	
	public void runCommand (String obj, String obj2, String obj3, String obj4) {
		runCommand(new String[] {obj, obj2, obj3, obj4});
	}
	
	public void runCommand (String obj, String obj2, String obj3, String obj4, String obj5) {
		runCommand(new String[] {obj, obj2, obj3, obj4, obj5});
	}
	
	public void runCommand (String obj, String obj2, String obj3, String obj4, String obj5, String obj6) {
		runCommand(new String[] {obj, obj2, obj3, obj4, obj5, obj6});
	}
	
	public void runCommand (String obj, String obj2, String obj3, String obj4, String obj5, String obj6, String obj7) {
		runCommand(new String[] {obj, obj2, obj3, obj4, obj5, obj6, obj7});
	}
	
	public void runCommand (String obj, String obj2, String obj3, String obj4, String obj5, String obj6, String obj7, String obj8) {
		runCommand(new String[] {obj, obj2, obj3, obj4, obj5, obj6, obj7, obj8});
	}
	
	public void runCommand (String obj, String obj2, String obj3, String obj4, String obj5, String obj6, String obj7, String obj8, String obj9) {
		runCommand(new String[] {obj, obj2, obj3, obj4, obj5, obj6, obj7, obj8, obj9});
	}
	
	public Map<String, String> getPropertiesFile (String fileName) throws Exception {
		if((new File(fileName+".properties")).exists()) {
			PropertiesFile p = new PropertiesFile(fileName+".properties");
			return p.returnMap();
		}
		else {
			throw new FileNotFoundException(fileName+".properties was not found");
		}
	}
	
	public String getFileContents (String fileName) throws APIException {
		if((new File(fileName)).exists()) {
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(new File(fileName));
				FileChannel fc = stream.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				/* Instead of using default, pass in a decoder. */
				return Charset.forName("UTF-8").decode(bb).toString();
			}
			catch (Exception e) {
				throw new APIException(fileName+" could not have its files extracte!");
			}
			finally {
				try {
					stream.close();
				}
				catch (Exception e) {
					throw new APIException(fileName+" could not be closed!");
				}
			}
		}
		else {
			throw new APIException(fileName+" doesn't exist!");
		}
	}
	
	public boolean setFileContents (String fileName, String contents) throws APIException {
		if((new File(fileName)).exists()) {
			FileOutputStream stream = null;
			try {
				stream = new FileOutputStream(new File(fileName));
				stream.write(contents.getBytes(Charset.forName("UTF-8")));
				try {
					stream.close();
				} catch (IOException e) {
					throw new APIException(fileName+" could not be closed!");
				}
			} catch (IOException e) {
				throw new APIException(fileName+" could not be written to!");
			}
			return true;			
		}
		else {
			throw new APIException(fileName+" doesn't exist!");
		}
	}
	
	public boolean appendFileContents (String fileName, String contents) throws APIException {
		if((new File(fileName)).exists()) {
			FileOutputStream stream = null;
			try {
				stream = new FileOutputStream(new File(fileName), true);
				stream.write(contents.getBytes(Charset.forName("UTF-8")));
				try {
					stream.close();
				} catch (IOException e) {
					throw new APIException(fileName+" could not be closed!");
				}
			} catch (IOException e) {
				throw new APIException(fileName+" could not be written to!");
			}
			return true;			
		}
		else {
			throw new APIException(fileName+" doesn't exist!");
		}
	}
	
	public boolean editPropertiesFile (String fileName, String type, String key, String value) throws FileNotFoundException {
		if((new File(fileName+".properties")).exists()) {
			PropertiesFile p = new PropertiesFile(fileName+".properties");
			if(type.toLowerCase().equals("boolean")) {
				p.setBoolean(key, Boolean.valueOf(value.toString()));
			}
			else if(type.toLowerCase().equals("long")) {
				p.setLong(key, Long.valueOf(value.toString()));
			}
			else if(type.toLowerCase().equals("int")) {
				p.setInt(key, Integer.valueOf(value.toString()));
			}
			else if(type.toLowerCase().equals("string")) {
				p.setString(key, value.toString());
			}
			else if(type.toLowerCase().equals("double")) {
				p.setDouble(key, Double.valueOf(value.toString()));
			}
			p.save();
			return true;
		}
		else {
			throw new FileNotFoundException(fileName+".properties was not found");
		}
	}

	public long getJavaMaxMemory () {
		return Runtime.getRuntime().maxMemory();
	}
	
	public long getJavaMemoryUsage () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	
	public long getDiskUsage () {
		return (new File(".")).getTotalSpace() - (new File(".")).getFreeSpace();
	}
	
	public long getDiskSize () {
		return (new File(".")).getTotalSpace();
	}
	
	public long getDiskFreeSpace () {
		return (new File(".")).getFreeSpace();
	}
	
	public List<JSONObject> getStreamWithLimit (String streamName, int count) {
		List<JSONAPIStreamMessage> stack = JSONAPI.instance.getStreamManager().getStream(streamName).getStack();
		
		count = count == -1 ? stack.size() : (stack.size() < count ? stack.size() : count);
		
		ArrayList<JSONObject> a = new ArrayList<JSONObject>();
		
		synchronized (stack) {
			for(int i = stack.size() - count; i < stack.size(); i++) {
				a.add(stack.get(i).toJSONObject());
			}			
		}
		return a;
	}
	
	public List<JSONObject> getStream (String streamName) {
		return getStreamWithLimit(streamName, -1);
	}
	
	public List<JSONObject> getConsoleLogs (int count) {
		return getStreamWithLimit("console", count);
	}
	
	public List<JSONObject> getConsoleLogs () {
		return getConsoleLogs(-1);
	}
	
	public List<JSONObject> getChatLogs (int count) {
		return getStreamWithLimit("chat", count);
	}
	
	
	public List<JSONObject> getChatLogs () {
		return getChatLogs(-1);
	}
	public List<JSONObject> getConnectionLogs (int count) {
		return getStreamWithLimit("connections", count);
	}
	
	public List<JSONObject> getConnectionLogs () {
		return getConnectionLogs(-1);
	}

	boolean isRTKloaded = false;
	
	// RTK methods
	public boolean restartServer() throws IOException, RTKInterfaceException {
		JSONAPI.instance.rtkAPI.executeCommand(CommandType.RESTART, null);
		return true;
	}
	
	public boolean stopServer() throws IOException, RTKInterfaceException {
		JSONAPI.instance.rtkAPI.executeCommand(CommandType.HOLD_SERVER, null);
		return true;
	}
	
	public boolean rescheduleServerRestart(String format) throws IOException, RTKInterfaceException {
		JSONAPI.instance.rtkAPI.executeCommand(CommandType.RESCHEDULE_RESTART, format);
		return true;
	}
	// end RTK methods
	
	public List<String> getDirectory (String path) {
		try {
			File dir = new File(path);
			RecursiveDirLister d = new RecursiveDirLister(dir);
			
			return d.getFileListing();
		}
		catch(Exception e) {
			// e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	public List<String> getSingleDirectory (String path) {
		try {
			File dir = new File(path);
			RecursiveDirLister d = new RecursiveDirLister(dir);
			
			return d.getSingleFileListing();
		}
		catch(Exception e) {
			// e.printStackTrace();
			return new ArrayList<String>();
		}		
	}
	
	public void setBlockData(String w, int x, int y, int z, int data) {
		Server.getWorld(w).getBlockAt(x, y, z).setData((byte)data);
	}
	
	public boolean teleport(String player1, String player2) {
		Server.getPlayerExact(player1).teleport(Server.getPlayerExact(player2));
		
		return true;
	}
	
	public boolean setWorldTime(String worldName, int time) {
		Server.getWorld(worldName).setTime(Long.valueOf(time));
		
		return true;
	}
	
	// I'm a real boy! I swear!
	public Server getServer() {
		return Server;
	}
	
	public List<String> getPlayerNames () {
		List<String> names = new ArrayList<String>();
		
		for(Player p : Server.getOnlinePlayers()) {
			names.add(p.getName());
		}
		
		return names;
	}
	
	public List<String> getOfflinePlayerNames () {
		List<String> names = new ArrayList<String>();
		List<String> online = getPlayerNames();
		
		for(OfflinePlayer p : Server.getOfflinePlayers()) {
			if(!online.contains(p.getName())) {
				names.add(p.getName());
			}
		}
		
		return names;
	}
	
	public List<OfflinePlayer> getOfflinePlayers () {
		List<OfflinePlayer> o = new ArrayList<OfflinePlayer>();
		List<String> online = getPlayerNames();
		
		for(OfflinePlayer p : Server.getOfflinePlayers()) {
			if(!online.contains(p.getName())) {
				o.add(p);
			}
		}
		
		return o;
	}
	
	public boolean ban(String playerName) {
		return banWithReason(playerName, "Banned by admin.");
	}
	
	public boolean unban(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setBanned(false);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean whitelist(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setWhitelisted(true);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean unwhitelist(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setWhitelisted(false);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	public boolean op(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setOp(true);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean deop(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setOp(false);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean teleport(String playername, int x, int y, int z) {
		try {
			Player player = Server.getPlayerExact(playername);
			player.teleport(new Location(player.getWorld(), x, y, z));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public boolean teleport(String playername, String world, int x, int y, int z) {
		try {
			Server.getPlayerExact(playername).teleport(new Location(Server.getWorld(world), x, y, z));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public String[] getSignText(String world, int x, int y, int z) throws Exception {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();
		
		if(d instanceof Sign) {
			return ((Sign) d).getLines();
		}
		
		throw new Exception("That isn't a sign!");
	}
	
	public boolean setSignText(String world, int x, int y, int z, List<String> lines) {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();
		
		if(d instanceof Sign) {
			for(int i = 0; i < lines.size(); i++) {
				((Sign) d).setLine(i, lines.get(i));
			}
			return true;
		}
		
		return false;
	}
	
	public boolean setSignTextLine(String world, int x, int y, int z, int line, String txt) {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();
		
		if(d instanceof Sign) {
			((Sign) d).setLine(line, txt);
			return true;
		}
		
		return false;
	}
	
	public Inventory getChestContents(String world, int x, int y, int z) throws Exception {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();
		
		if(d instanceof Chest) {
			return ((Chest) d).getInventory();
		}
		
		throw new Exception("That isn't a chest!");
	}
	
	public boolean giveChestItem (String world, int x, int y, int z, int slot, int blockID, int quantity) {
		try {
			if(blockID == 0) {
				return clearChestSlot(world, x, y, z, slot);
			}
			
			Inventory inv = ((Chest)Server.getWorld(world).getBlockAt(x, y, z).getState()).getInventory();
			ItemStack it = new ItemStack(blockID, quantity);
			inv.setItem(slot, it);
			
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public boolean giveChestItem(String world, int x, int y, int z, int slot, int blockID, final int data, int quantity) {
		try {
			if(blockID == 0) {
				return clearChestSlot(world, x, y, z, slot);
			}
			
			Inventory inv = ((Chest)Server.getWorld(world).getBlockAt(x, y, z).getState()).getInventory();
			ItemStack it = (new MaterialData(blockID, (byte)data)).toItemStack(quantity);
			inv.setItem(slot, it);
			
			return true;
		}
		catch (Exception e) {
			return false;
		}
		
	}
	
	public boolean clearChestSlot (String world, int x, int y, int z, int slot) {
		try {
			Inventory inv = ((Chest)Server.getWorld(world).getBlockAt(x, y, z).getState()).getInventory();
			inv.clear(slot);

			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
