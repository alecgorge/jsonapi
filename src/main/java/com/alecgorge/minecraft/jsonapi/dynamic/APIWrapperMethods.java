package com.alecgorge.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.java_websocket.util.Base64;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.APIException;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterface.CommandType;
import com.alecgorge.minecraft.jsonapi.McRKit.api.RTKInterfaceException;
import com.alecgorge.minecraft.jsonapi.api.BukGetAPIMethods;
import com.alecgorge.minecraft.jsonapi.api.CommandMethods;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIAPIMethods;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
import com.alecgorge.minecraft.jsonapi.chat.BukkitForgeRealisticChat;
import com.alecgorge.minecraft.jsonapi.chat.BukkitRealisticChat;
import com.alecgorge.minecraft.jsonapi.chat.IRealisticChat;
import com.alecgorge.minecraft.jsonapi.util.OfflinePlayerLoader;
import com.alecgorge.minecraft.jsonapi.util.PropertiesFile;
import com.alecgorge.minecraft.jsonapi.util.RecursiveDirLister;
import com.alecgorge.minecraft.permissions.PermissionWrapper;

public class APIWrapperMethods implements JSONAPIMethodProvider {
	private Logger outLog = JSONAPI.instance.outLog;
	public PermissionWrapper permissions;

	public Economy econ;
	public Chat chat;
	public BukGetAPIMethods bukget;
	public JSONAPIAPIMethods jsonapi;
	public CommandMethods commands;

	public APIWrapperMethods(Server server) {
		bukget = new BukGetAPIMethods(server);
		jsonapi = new JSONAPIAPIMethods(server);
		permissions = new PermissionWrapper(server);
		commands = new CommandMethods(server);

		if (server.getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
			if (rsp != null) {
				econ = rsp.getProvider();
			}
			RegisteredServiceProvider<Chat> rsp2 = server.getServicesManager().getRegistration(Chat.class);
			if (rsp2 != null) {
				chat = rsp2.getProvider();
			}
		}
	}

	private Server Server = JSONAPI.instance.getServer();
	private static APIWrapperMethods instance;

	public static APIWrapperMethods getInstance() {
		if (instance == null) {
			instance = new APIWrapperMethods(JSONAPI.instance.getServer());
		}
		return instance;
	}
		
	public List<String> getWorldNames() {
		List<String> names = new ArrayList<String>();
		for(org.bukkit.World world : getServer().getWorlds()) {
			names.add(world.getName());
		}
		return names;
	}
	
	public int onlinePlayerCount() {
		Object o = Server.getOnlinePlayers();
		
		if(o instanceof Player[]) {
			return ((Player[]) o).length;
		}
		else if(o instanceof Collection<?>) {
			return ((Collection<?>) o).size();
		}
		
		return 0;
	}

	public HashMap<Integer, ItemStack> removePlayerInventoryItem(String playerName, int itemID) {
		try {
			Player p = getPlayerExact(playerName);
			HashMap<Integer, ItemStack> c = p.getInventory().removeItem(new ItemStack(itemID, 64 * 64));
			p.saveData();

			return c;
		} catch (NullPointerException e) {
			return null;
		}
	}

	public List<OfflinePlayer> opList() {
		List<OfflinePlayer> ops = new ArrayList<OfflinePlayer>();

		for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
			if (p.isOp()) {
				ops.add(p);
			}
		}

		return ops;
	}
	
	public boolean setPlayerHealth(String playerName, int health) {
		Player p = getPlayerExact(playerName);
		p.setHealth((double)health);
		p.saveData();
		return true;
	}
	
	public boolean setPlayerFoodLevel(String playerName, int health) {
		Player p = getPlayerExact(playerName);
		p.setFoodLevel(health);
		p.saveData();
		return true;
	}

	public boolean removeEnchantmentsFromPlayerInventorySlot(String playerName, int slot, List<Object> enchantments) {
		try {
			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it;

			if (slot == inv.getHeldItemSlot())
				it = inv.getHelmet();
			else if (slot == 102)
				it = inv.getChestplate();
			else if (slot == 101)
				it = inv.getLeggings();
			else if (slot == 100)
				it = inv.getBoots();
			else
				it = inv.getItem(slot);

			for (Object o : enchantments) {
				it.removeEnchantment(Enchantment.getById(Integer.valueOf(o.toString())));
			}

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean addEnchantmentToPlayerInventorySlot(String playerName, int slot, int enchantmentID, int level) {
		try {
			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it;

			if (slot == 103)
				it = inv.getHelmet();
			else if (slot == 102)
				it = inv.getChestplate();
			else if (slot == 101)
				it = inv.getLeggings();
			else if (slot == 100)
				it = inv.getBoots();
			else
				it = inv.getItem(slot);

			it.addEnchantment(Enchantment.getById(enchantmentID), level);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean addEnchantmentsToPlayerInventorySlot(String playerName, int slot, List<Object> enchantments) {
		try {
			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it;

			if (slot == inv.getHeldItemSlot())
				it = inv.getHelmet();
			else if (slot == 102)
				it = inv.getChestplate();
			else if (slot == 101)
				it = inv.getLeggings();
			else if (slot == 100)
				it = inv.getBoots();
			else
				it = inv.getItem(slot);

			for (int i = 0; i < enchantments.size(); i++) {
				JSONObject o = (JSONObject) enchantments.get(i);
				it.addEnchantment(Enchantment.getById(Integer.valueOf(o.get("enchantment").toString())), Integer.valueOf(o.get("level").toString()));
			}

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean setPlayerInventorySlot(String playerName, int slot, int blockID, int quantity) {
		try {
			if (blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it = new ItemStack(blockID, quantity);

			if (slot == 103)
				inv.setHelmet(it);
			else if (slot == 102)
				inv.setChestplate(it);
			else if (slot == 101)
				inv.setLeggings(it);
			else if (slot == 100)
				inv.setBoots(it);
			else
				inv.setItem(slot, it);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean setPlayerInventorySlotWithData(String playerName, int slot, int blockID, final int data, int quantity) {
		try {
			if (blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it = (new MaterialData(blockID, (byte) data)).toItemStack(quantity);

			if (slot == 103)
				inv.setHelmet(it);
			else if (slot == 102)
				inv.setChestplate(it);
			else if (slot == 101)
				inv.setLeggings(it);
			else if (slot == 100)
				inv.setBoots(it);
			else
				inv.setItem(slot, it);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}

	}

	public boolean setPlayerInventorySlotWithDataAndDamage(String playerName, int slot, int blockID, final int data, int damage, int quantity) {
		try {
			if (blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			
			ItemStack it = (new MaterialData(blockID, (byte) data)).toItemStack(quantity);
			it.setDurability((short)data);
			
			if (slot == 103)
				inv.setHelmet(it);
			else if (slot == 102)
				inv.setChestplate(it);
			else if (slot == 101)
				inv.setLeggings(it);
			else if (slot == 100)
				inv.setBoots(it);
			else
				inv.setItem(slot, it);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}

	}

	public boolean setPlayerInventorySlotWithDataDamageAndEnchantments(String playerName, int slot, int blockID, final int data, int damage, int quantity, Object[] enchantments) {
		try {
			if (blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it = (new MaterialData(blockID, (byte) data)).toItemStack(quantity);
			it.setDurability((short)data);

			for (int i = 0; i < enchantments.length; i++) {
				JSONObject o = (JSONObject) enchantments[i];
				it.addEnchantment(Enchantment.getById(Integer.valueOf(o.get("enchantment").toString())), Integer.valueOf(o.get("level").toString()));
			}

			if (slot == 103)
				inv.setHelmet(it);
			else if (slot == 102)
				inv.setChestplate(it);
			else if (slot == 101)
				inv.setLeggings(it);
			else if (slot == 100)
				inv.setBoots(it);
			else
				inv.setItem(slot, it);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}

	}

	public boolean setPlayerInventorySlot(String playerName, int slot, int blockID, int damage, int quantity) {
		try {
			if (blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it = new ItemStack(blockID, quantity, Short.valueOf(String.valueOf(damage)).shortValue());

			if (slot == 103)
				inv.setHelmet(it);
			else if (slot == 102)
				inv.setChestplate(it);
			else if (slot == 101)
				inv.setLeggings(it);
			else if (slot == 100)
				inv.setBoots(it);
			else
				inv.setItem(slot, it);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public void setPlayerGameMode(String playerName, int gameMode) throws Exception {
		Player p = getPlayerExact(playerName);
		
		p.setGameMode(GameMode.getByValue(gameMode));		
		p.saveData();
	}

	public boolean clearPlayerInventorySlot(String playerName, int slot) {
		try {
			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			int cnt = inv.getSize();

			if (slot == 103)
				inv.clear(cnt + 3);
			else if (slot == 102)
				inv.clear(cnt + 2);
			else if (slot == 101)
				inv.clear(cnt + 1);
			else if (slot == 100)
				inv.clear(cnt + 0);
			else
				inv.clear(slot);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean updatePlayerInventorySlot(String playerName, int slot, int newAmount) {
		try {
			Player p = getPlayerExact(playerName);
			ItemStack s = p.getInventory().getItem(slot);
			s.setAmount(newAmount);
			p.getInventory().setItem(slot, s);

			p.saveData();

			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public List<String> getPluginFiles(String pluginName) {
		try {
			File dir = Server.getPluginManager().getPlugin(pluginName).getDataFolder();
			RecursiveDirLister d = new RecursiveDirLister(dir);

			return d.getFileListing();
		} catch (Exception e) {
			// e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	@API_Method(namespace = "", name="polyfill_getPluginVersion", isProvidedByV2API=false)
	public String get345version(String name) {
		return "3.6.7"; // needed because of faulty Adminium 2.2.1 version checking
	}
	
	private IRealisticChat chatUtility = null;
		
	public IRealisticChat getRealisticChatProvider() {
		if(chatUtility != null) {
			return chatUtility;
		}
		
		chatUtility = new BukkitRealisticChat();
		if(!chatUtility.canHandleChats()) {
			chatUtility = new BukkitForgeRealisticChat();
		}
		
		if(chatUtility == null) {
			chatUtility = new IRealisticChat() {
				
				@Override
				public void pluginDisable() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean chatWithName(String message, String name) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean canHandleChats() {
					// TODO Auto-generated method stub
					return false;
				}
			};
		}
		
		return chatUtility;
	}
	
	public boolean chatWithName(String message, String name) {
		getRealisticChatProvider().chatWithName(message, name);
		return true;
	}
	
	public void pluginDisable() {
		getRealisticChatProvider().pluginDisable();
	}

	public int broadcastMessage(String message) {
		JSONAPI.instance.jsonServer.logChat("", message);
		return Server.broadcastMessage(message);
	}

	public List<String> getWhitelist() throws APIException {
		List<String> a = new ArrayList<String>();
		for (OfflinePlayer p : Server.getWhitelistedPlayers()) {
			a.add(p.getName());
		}
		return a;
	}

	public List<String> getBannedPlayers() throws APIException {
		List<String> a = new ArrayList<String>();
		for (OfflinePlayer p : Server.getBannedPlayers()) {
			a.add(p.getName());
		}
		return a;
	}

	public List<String> getBannedIPs() throws APIException {
		return new ArrayList<String>(Server.getIPBans());
	}

	public boolean banWithReason(String name, String reason) {
		try {
			getServer().getBanList(Type.NAME).addBan(name, reason, null, "JSONAPI API call");
			Bukkit.getPlayerExact(name).kickPlayer(reason);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean enablePlugin(String name) {
		try {
			Server.getPluginManager().enablePlugin(Server.getPluginManager().getPlugin(name));
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean disablePlugin(String name) {
		try {
			Server.getPluginManager().disablePlugin(Server.getPluginManager().getPlugin(name));
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean giveItem(String name, int id, int quant) {
		try {
			Player p = getPlayerExact(name);
			p.getInventory().addItem(new ItemStack(id, quant));
			p.saveData();
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean giveItem(String name, int id, int quant, int data) throws Exception {
		try {
            byte realData = Byte.valueOf(String.valueOf(data));
			Player p = getPlayerExact(name);
            MaterialData materialData = new MaterialData(id, realData);
			p.getInventory().addItem(materialData.toItemStack(quant));
			p.saveData();
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean giveItemDrop(String name, int id, int quant) {
		try {
			Player p = getPlayerExact(name);
			p.getWorld().dropItem(p.getLocation(), new ItemStack(id, quant));
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean giveItemDrop(String name, int id, int quant, int data) throws Exception {
		try {
			Player p = getPlayerExact(name);
			ItemStack stack = new ItemStack(id, quant);
			stack.setData(new MaterialData(id, Byte.valueOf(String.valueOf(data)).byteValue()));
			p.getWorld().dropItem(p.getLocation(), stack);
			p.saveData();
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public void runCommand(String... obj) {
		StringBuilder command = new StringBuilder();
		for (String s : obj) {
			command.append(s);
		}

		String cmd = command.toString();

		outLog.info("Command run by remote user: '" + cmd + "'");

		Server.dispatchCommand(getServer().getConsoleSender(), cmd);
	}

	public void runCommand(String obj) {
		runCommand(new String[] { obj });
	}

	public void runCommand(String obj, String obj2) {
		runCommand(new String[] { obj, obj2 });
	}

	public void runCommand(String obj, String obj2, String obj3) {
		runCommand(new String[] { obj, obj2, obj3 });
	}

	public void runCommand(String obj, String obj2, String obj3, String obj4) {
		runCommand(new String[] { obj, obj2, obj3, obj4 });
	}

	public void runCommand(String obj, String obj2, String obj3, String obj4, String obj5) {
		runCommand(new String[] { obj, obj2, obj3, obj4, obj5 });
	}

	public void runCommand(String obj, String obj2, String obj3, String obj4, String obj5, String obj6) {
		runCommand(new String[] { obj, obj2, obj3, obj4, obj5, obj6 });
	}

	public void runCommand(String obj, String obj2, String obj3, String obj4, String obj5, String obj6, String obj7) {
		runCommand(new String[] { obj, obj2, obj3, obj4, obj5, obj6, obj7 });
	}

	public void runCommand(String obj, String obj2, String obj3, String obj4, String obj5, String obj6, String obj7, String obj8) {
		runCommand(new String[] { obj, obj2, obj3, obj4, obj5, obj6, obj7, obj8 });
	}

	public void runCommand(String obj, String obj2, String obj3, String obj4, String obj5, String obj6, String obj7, String obj8, String obj9) {
		runCommand(new String[] { obj, obj2, obj3, obj4, obj5, obj6, obj7, obj8, obj9 });
	}

	public Map<String, String> getPropertiesFile(String fileName) throws Exception {
		if ((new File(fileName + ".properties")).exists()) {
			PropertiesFile p = new PropertiesFile(fileName + ".properties");
			return p.returnMap();
		} else {
			throw new FileNotFoundException(fileName + ".properties was not found");
		}
	}
	
	public String createFile(String path) throws IOException {
		File f = new File(path);
		if(!f.exists()) {
			f.createNewFile();
		}
		return path;
	}
	
	public String createFolder(String path) throws IOException {
		File f = new File(path);
		if(!f.exists()) {
			f.mkdirs();
		}
		return path;
	}

	public String getFileContents(String fileName) throws APIException {
		if ((new File(fileName)).exists()) {
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(new File(fileName));
				FileChannel fc = stream.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				/* Instead of using default, pass in a decoder. */
				return Charset.forName("UTF-8").decode(bb).toString();
			} catch (Exception e) {
				throw new APIException(fileName + " could not have its files extracte!");
			} finally {
				try {
					stream.close();
				} catch (Exception e) {
					throw new APIException(fileName + " could not be closed!");
				}
			}
		} else {
			throw new APIException(fileName + " doesn't exist!");
		}
	}

	public boolean deleteFileOrFolder(String fileName) {
		File f;
		if ((f = new File(fileName)).exists()) {
			try {
				if(f.isDirectory()) {
					return deleteDirectory(f);
				}
				else {
					return f.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean deleteDirectory(File directory) {
		if (directory.exists()){
			File[] files = directory.listFiles();
			
			if (files != null){
				for (int i=0; i<files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
		}
		
		return directory.delete();
	}

	public boolean renameFileOrFolder(String oldName, String newName) {
		File f;
		if ((f = new File(oldName)).exists()) {
			try {
				return f.renameTo(new File(newName));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean copyFileOrFolder(String oldName, String newName) {
		File sourceFile;
		if ((sourceFile = new File(oldName)).exists()) {
			File destFile = new File(newName);
			boolean res = true;

			try {
				if (!destFile.exists()) {
					destFile.createNewFile();
				}

				FileChannel source = null;
				FileChannel destination = null;

				try {
					source = new FileInputStream(sourceFile).getChannel();
					destination = new FileOutputStream(destFile).getChannel();
					destination.transferFrom(source, 0, source.size());
				} catch (Exception e) {
					res = false;
				} finally {
					if (source != null) {
						source.close();
					}
					if (destination != null) {
						destination.close();
					}
				}
			} catch (Exception e) {
				res = false;
			}
			return res;
		} else {
			return false;
		}
	}

	public String getFileBinaryBase64(String fileName) throws APIException {
		if ((new File(fileName)).exists()) {
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(new File(fileName));
				FileChannel fc = stream.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				/* Instead of using default, pass in a decoder. */
				byte[] b = new byte[bb.remaining()];
				bb.get(b);

				return Base64.encodeBytes(b);
			} catch (Exception e) {
				throw new APIException(fileName + " could not have its files extracte!");
			} finally {
				try {
					stream.close();
				} catch (Exception e) {
					throw new APIException(fileName + " could not be closed!");
				}
			}
		} else {
			throw new APIException(fileName + " doesn't exist!");
		}
	}

	public boolean setFileBinaryBase64(String fileName, String base64) throws APIException {
		FileOutputStream stream = null;
		try {
			File f = new File(fileName);

			f.createNewFile();

			stream = new FileOutputStream(f);
			stream.write(Base64.decode(base64));
			try {
				stream.close();
			} catch (IOException e) {
				throw new APIException(fileName + " could not be closed!");
			}
		} catch (Exception e) {
			throw new APIException(fileName + " could not have its files extracte!");
		} finally {
			try {
				stream.flush();
				stream.close();
			} catch (Exception e) {
				throw new APIException(fileName + " could not be closed!");
			}
		}
		return true;
	}

	public boolean setFileContents(String fileName, String contents) throws APIException {
		FileOutputStream stream = null;
		try {
			JSONAPI.dbug("opening: " + fileName);
			File f = new File(fileName);
			JSONAPI.dbug("opened");

			f.createNewFile();

			stream = new FileOutputStream(f);
			
			JSONAPI.dbug("output stream created");
			stream.write(contents.getBytes(Charset.forName("UTF-8")));
			JSONAPI.dbug("output stream written");
			try {
				stream.flush();
				JSONAPI.dbug("flushed output");
				stream.close();
				JSONAPI.dbug("closed output");
			} catch (IOException e) {
				throw new APIException(fileName + " could not be closed!");
			}
		} catch (IOException e) {
			throw new APIException(fileName + " could not be written to!");
		}
		return true;
	}

	public boolean appendFileContents(String fileName, String contents) throws APIException {
		FileOutputStream stream = null;
		try {
			File f = new File(fileName);

			f.createNewFile();

			stream = new FileOutputStream(f, true);
			stream.write(contents.getBytes(Charset.forName("UTF-8")));
			try {
				stream.flush();
				stream.close();
			} catch (IOException e) {
				throw new APIException(fileName + " could not be closed!");
			}
		} catch (IOException e) {
			throw new APIException(fileName + " could not be written to!");
		}
		return true;
	}

	public boolean editPropertiesFile(String fileName, String type, String key, String value) throws FileNotFoundException {
		if ((new File(fileName + ".properties")).exists()) {
			PropertiesFile p = new PropertiesFile(fileName + ".properties");
			if (type.toLowerCase().equals("boolean")) {
				p.setBoolean(key, Boolean.valueOf(value.toString()));
			} else if (type.toLowerCase().equals("long")) {
				p.setLong(key, Long.valueOf(value.toString()));
			} else if (type.toLowerCase().equals("int")) {
				p.setInt(key, Integer.valueOf(value.toString()));
			} else if (type.toLowerCase().equals("string")) {
				p.setString(key, value.toString());
			} else if (type.toLowerCase().equals("double")) {
				p.setDouble(key, Double.valueOf(value.toString()));
			}
			p.save();
			return true;
		} else {
			throw new FileNotFoundException(fileName + ".properties was not found");
		}
	}

	public boolean setPlayerLevel(String player, int level) {
		Player p = getPlayerExact(player);
		p.setLevel(level);
		p.saveData();
		return true;
	}

	public boolean setPlayerExperience(String player, float level) {
		Player p = getPlayerExact(player);
		p.setExp(level);
		p.saveData();
		return true;
	}

	public double getJavaMaxMemory() {
		return Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0;
	}

	public double getJavaMemoryUsage() {
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
	}

	public double getDiskUsage() {
		return ((new File(".")).getTotalSpace() - (new File(".")).getFreeSpace()) / 1024.0 / 1024.0;
	}

	public double getDiskSize() {
		return (new File(".")).getTotalSpace() / 1024.0 / 1024.0;
	}

	public double getDiskFreeSpace() {
		return (new File(".")).getFreeSpace() / 1024.0 / 1024.0;
	}
	
	public JSONObject testClock() {
		return JSONAPI.instance.getTickRateCounter().getJSONObject();
	}


	public List<JSONObject> getStreamWithLimit(String streamName, int count) {
		List<JSONAPIStreamMessage> stack = JSONAPI.instance.getStreamManager().getStream(streamName).getStack();

		count = count == -1 ? stack.size() : (stack.size() < count ? stack.size() : count);

		ArrayList<JSONObject> a = new ArrayList<JSONObject>();

		synchronized (stack) {
			for (int i = stack.size() - count; i < stack.size(); i++) {
				a.add(stack.get(i).toJSONObject());
			}
		}
		return a;
	}

	public List<JSONObject> getStream(String streamName) {
		return getStreamWithLimit(streamName, -1);
	}

	public List<JSONObject> getConsoleLogs(int count) {
		return getStreamWithLimit("console", count);
	}

	public List<JSONObject> getConsoleLogs() {
		return getConsoleLogs(-1);
	}

	public List<JSONObject> getChatLogs(int count) {
		return getStreamWithLimit("chat", count);
	}

	public List<JSONObject> getEggLogs(int count) {
		return getStreamWithLimit("egg", count);
	}

	public List<JSONObject> getFormattedChatLogs(int count) {
		return getStreamWithLimit("formatted_chat", count);
	}

	public List<JSONObject> getChatLogs() {
		return getChatLogs(-1);
	}

	public List<JSONObject> getConnectionLogs(int count) {
		return getStreamWithLimit("connections", count);
	}

	public List<JSONObject> getConnectionLogs() {
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

	public List<String> getDirectory(String path) {
		try {
			File dir = new File(path);
			RecursiveDirLister d = new RecursiveDirLister(dir);

			return d.getFileListing();
		} catch (Exception e) {
			// e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	public List<String> getSingleDirectory(String path) {
		try {
			File dir = new File(path);
			RecursiveDirLister d = new RecursiveDirLister(dir);

			return d.getSingleFileListing();
		} catch (Exception e) {
			// e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	public void setBlockData(String w, int x, int y, int z, int data) {
		Server.getWorld(w).getBlockAt(x, y, z).setData((byte) data);
	}

        public void explodeAtLocation(
                String w,
                double x,
                double y,
                double z,
                double power,
                boolean setFire,
                boolean breakBlocks) {
		Server.getWorld(w).createExplosion(x, y, z, (float)power, setFire, breakBlocks);
        }

	public boolean teleport(String player1, String player2) {
		Player p = getPlayerExact(player1);
		p.teleport(getPlayerExact(player2));
		p.saveData();

		return true;
	}

	public boolean setWorldTime(String worldName, int time) {
		Server.getWorld(worldName).setTime(Long.valueOf(time));

		return true;
	}

	public boolean setWorldDifficulty(String worldName, int diff) {
		Server.getWorld(worldName).setDifficulty(Difficulty.getByValue(diff));

		return true;
	}

	// I'm a real boy! I swear!
	public Server getServer() {
		return Server;
	}

	public List<String> getPlayerNames() {
		List<String> names = new ArrayList<String>();

		for (Player p : Server.getOnlinePlayers()) {
			names.add(p.getName());
		}

		return names;
	}

	public List<String> getOfflinePlayerNames() {
		List<String> names = new ArrayList<String>();
		List<String> online = getPlayerNames();

		for (OfflinePlayer p : Server.getOfflinePlayers()) {
			if (!online.contains(p.getName())) {
				names.add(p.getName());
			}
		}

		return names;
	}

	public List<OfflinePlayer> getOfflinePlayers() {
		List<OfflinePlayer> o = new ArrayList<OfflinePlayer>();
		List<String> online = getPlayerNames();

		for (OfflinePlayer p : Server.getOfflinePlayers()) {
			if (!online.contains(p.getName())) {
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
			getServer().getBanList(Type.NAME).pardon(playerName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean whitelist(String playerName) {
		try {
			Bukkit.getOfflinePlayer(OfflinePlayerLoader.matchUser(playerName)).setWhitelisted(true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean unwhitelist(String playerName) {
		try {
			Bukkit.getOfflinePlayer(OfflinePlayerLoader.matchUser(playerName)).setWhitelisted(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean op(String playerName) {
		try {
			OfflinePlayer p = Bukkit.getOfflinePlayer(OfflinePlayerLoader.matchUser(playerName));

			if (p.isOnline()) {
				p.getPlayer().sendMessage("You are now OP");
			}

			p.setOp(true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean deop(String playerName) {
		try {
			OfflinePlayer p = Bukkit.getOfflinePlayer(OfflinePlayerLoader.matchUser(playerName));

			if (p.isOnline()) {
				p.getPlayer().sendMessage("You are no longer OP");
			}

			p.setOp(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Player getPlayerExact(String playerName) {
		Player player = Server.getPlayerExact(playerName);
		if (player == null) {
			player = JSONAPI.loadOfflinePlayer(playerName);
		}

		return player;
	}

	public boolean teleport(String playername, int x, int y, int z) {
		try {
			Player player = getPlayerExact(playername);
			player.teleport(new Location(player.getWorld(), x, y, z));
			player.saveData();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean teleport(String playername, String world, int x, int y, int z) {
		try {
			Player p = getPlayerExact(playername);
			p.teleport(new Location(Server.getWorld(world), x, y, z));
			p.saveData();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String[] getSignText(String world, int x, int y, int z) throws Exception {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();

		if (d instanceof Sign) {
			return ((Sign) d).getLines();
		}

		return null;
	}

	public boolean setSignText(String world, int x, int y, int z, String[] lines) {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();

		if (d instanceof Sign) {
			for (int i = 0; i < lines.length; i++) {
				((Sign) d).setLine(i, lines[i]);
			}
			((Sign) d).update();
			
			return true;
		}

		return false;
	}
	
	public boolean setSignText(String world, int x, int y, int z, List<String> lines) {
		String[] a = new String[lines.size()];
		lines.toArray(a);
		return setSignText(world, x, y, z, a);
	}

	public boolean setSignText(String world, int x, int y, int z, int line, String txt) {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();

		if (d instanceof Sign) {
			((Sign) d).setLine(line, txt);
			((Sign) d).update();
			return true;
		}

		return false;
	}

	public Inventory getChestContents(String world, int x, int y, int z) throws Exception {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();

		if (d instanceof Chest) {
			return ((Chest) d).getInventory();
		}

		return null;
	}

	public boolean giveChestItem(String world, int x, int y, int z, int slot, int blockID, int quantity) {
		try {
			if (blockID == 0) {
				return clearChestSlot(world, x, y, z, slot);
			}

			Inventory inv = ((Chest) Server.getWorld(world).getBlockAt(x, y, z).getState()).getInventory();
			ItemStack it = new ItemStack(blockID, quantity);
			inv.setItem(slot, it);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean giveChestItem(String world, int x, int y, int z, int slot, int blockID, final int data, int quantity) {
		try {
			if (blockID == 0) {
				return clearChestSlot(world, x, y, z, slot);
			}

			Inventory inv = ((Chest) Server.getWorld(world).getBlockAt(x, y, z).getState()).getInventory();
			ItemStack it = (new MaterialData(blockID, (byte) data)).toItemStack(quantity);
			inv.setItem(slot, it);

			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public boolean clearChestSlot(String world, int x, int y, int z, int slot) {
		try {
			Inventory inv = ((Chest) Server.getWorld(world).getBlockAt(x, y, z).getState()).getInventory();
			inv.clear(slot);

			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean spawn(String world, double x, double y, double z, String mobName) {
		return getServer().getWorld(world).spawnEntity(new Location(getServer().getWorld(world), x, y, z), EntityType.fromName(mobName)) != null;
	}	
}
