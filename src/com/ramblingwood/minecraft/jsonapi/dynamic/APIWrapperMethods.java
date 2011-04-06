package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;
import com.ramblingwood.minecraft.jsonapi.PropertiesFile;

public class APIWrapperMethods implements CommandSender {
	private Server Server = JSONAPI.instance.getServer();
	private static APIWrapperMethods instance;
	
	public static APIWrapperMethods getInstance () {
		if(instance == null) {
			instance = new APIWrapperMethods();
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
	
	public boolean setPlayerInventorySlot (String playerName, Integer slot, Integer blockID, Integer quantity) {
		try {
			Server.getPlayer(playerName).getInventory().setItem(slot, new ItemStack(blockID, quantity));
			return true;
		}
		catch (NullPointerException e) {
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
			Server.getPlayer(name).getInventory().addItem(new ItemStack(id, quant));
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
	
	public void runCommand (String command) {
		System.out.println("running command: "+command);
		Server.dispatchCommand(this, command);
	}
	
	public void runCommand (String start, String append) {
		runCommand(start+" "+append);
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
