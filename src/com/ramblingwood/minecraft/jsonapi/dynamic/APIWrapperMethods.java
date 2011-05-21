package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.simpleForBukkit.JSONObject;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;
import com.ramblingwood.minecraft.jsonapi.PropertiesFile;
import com.ramblingwood.minecraft.jsonapi.streams.JSONAPIStream;

public class APIWrapperMethods extends ConsoleCommandSender {
	public APIWrapperMethods(Server server) {
		super(server);
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
			return Server.getPlayer(playerName).getInventory().removeItem(new ItemStack(itemID));
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	
	public boolean setPlayerInventorySlot (String playerName, int slot, int blockID, int quantity) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}
			
			PlayerInventory inv = Server.getPlayer(playerName).getInventory();
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
	
	public boolean setPlayerInventorySlot (String playerName, int slot, int blockID, int damage, int quantity) {
		try {
			if(blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			PlayerInventory inv = Server.getPlayer(playerName).getInventory();
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
	
	public boolean clearPlayerInventorySlot (String playerName, int slot) {
		try {
			PlayerInventory inv = Server.getPlayer(playerName).getInventory();
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
	
	public List<String> getWhitelist () throws IOException {
		String w = getFileContents("white-list.txt");
		List<String> a = new ArrayList<String>();
		for(String s : w.split("\n")) {
			a.add(s.trim());
		}
		return a;
	}
	
	public List<String> getBannedPlayers () throws IOException {
		String w = getFileContents("banned-players.txt");
		List<String> a = new ArrayList<String>();
		for(String s : w.split("\n")) {
			a.add(s.trim());
		}
		return a;
	}
	
	public List<String> getBannedIPs () throws IOException {
		String w = getFileContents("banned-ips.txt");
		List<String> a = new ArrayList<String>();
		for(String s : w.split("\n")) {
			a.add(s.trim());
		}
		return a;
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
			Server.getPlayer(name).getInventory().addItem(new ItemStack(id, quant));
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
			Server.getPlayer(name).getInventory().addItem(new ItemStack(id, quant, (short)0, Byte.valueOf(String.valueOf(data)).byteValue()));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}			
	}
	
	public boolean giveItemDrop (String name, int id, int quant) {
		try {
			Server.getPlayer(name).getWorld().dropItem(Server.getPlayer(name).getLocation(), new ItemStack(id, quant));
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
			Server.getPlayer(name).getWorld().dropItem(Server.getPlayer(name).getLocation(), new ItemStack(id, quant, (short)0, Byte.valueOf(String.valueOf(data)).byteValue()));
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}			
	}
	
	public void runCommand (String...obj) {
		StringBuffer command = new StringBuffer();
		for(String s : obj) {
			command.append(s);
		}
		
		if(Call.debug) {
			System.out.println("running command: "+command.toString());
		}
		Server.dispatchCommand(this, command.toString());
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
	
	public String getFileContents (String fileName) throws IOException {
		if((new File(fileName)).exists()) {
			FileInputStream stream = new FileInputStream(new File(fileName));
			try {
				FileChannel fc = stream.getChannel();
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				/* Instead of using default, pass in a decoder. */
				return Charset.defaultCharset().decode(bb).toString();
			}
			finally {
				stream.close();
			}
		}
		else {
			throw new FileNotFoundException(fileName+" was not found");
		}
	}
	
	public boolean setFileContents (String fileName, String contents) throws IOException {
		if((new File(fileName)).exists()) {
			FileOutputStream stream = new FileOutputStream(new File(fileName));
			try {
				stream.write(contents.getBytes(Charset.forName("UTF-8")));
			}
			finally {
				stream.close();
			}
			return true;			
		}
		else {
			throw new FileNotFoundException(fileName+" was not found");
		}
	}
	
	public boolean editPropertiesFile (String fileName, String type, String key, Object value) throws FileNotFoundException {
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
		return Runtime.getRuntime().totalMemory();
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
	
	public List<JSONObject> getConsoleLogs (int count) {
		ArrayList<JSONAPIStream> stack = new ArrayList<JSONAPIStream>(JSONAPI.instance.jsonServer.console);
		
		count = count == -1 ? stack.size() : (stack.size() < count ? stack.size() : count);
		
		ArrayList<JSONObject> a = new ArrayList<JSONObject>();
		for(int i = 0; i < count; i++) {
			a.add(stack.get(i).toJSONObject());
		}
		return a;
	}
	
	public List<JSONObject> getConsoleLogs () {
		return getConnectionLogs(-1);
	}
	
	public List<JSONObject> getChatLogs (int count) {
		ArrayList<JSONAPIStream> stack = new ArrayList<JSONAPIStream>(JSONAPI.instance.jsonServer.chat);
		
		count = count == -1 ? stack.size() : (stack.size() < count ? stack.size() : count);
		
		ArrayList<JSONObject> a = new ArrayList<JSONObject>();
		for(int i = 0; i < count; i++) {
			a.add(stack.get(i).toJSONObject());
		}
		return a;
	}
	
	
	public List<JSONObject> getChatLogs () {
		return getChatLogs(-1);
	}
	public List<JSONObject> getConnectionLogs (int count) {
		ArrayList<JSONAPIStream> stack = new ArrayList<JSONAPIStream>(JSONAPI.instance.jsonServer.connections);
		
		count = count == -1 ? stack.size() : (stack.size() < count ? stack.size() : count);
		
		ArrayList<JSONObject> a = new ArrayList<JSONObject>();
		for(int i = 0; i < count; i++) {
			a.add(stack.get(i).toJSONObject());
		}
		return a;
	}
	
	public List<JSONObject> getConnectionLogs () {
		return getConnectionLogs(-1);
	}

	@Override
	public Server getServer() {
		return Server;
	}

	@Override
	public boolean isOp() {
		return true;
	}

	@Override
	public void sendMessage(String arg0) {
		Logger.getLogger("Minecraft").info("[JSONAPI] [Java Wrapper] "+arg0);
	}
}
