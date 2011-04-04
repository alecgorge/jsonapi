package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;

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
		return Server.getPlayer(playerName).getInventory().removeItem(new ItemStack(itemID));
	}
	
	public void setPlayerInventorySlot (String playerName, int slot, int blockID, int quantity) {
		Server.getPlayer(playerName).getInventory().setItem(slot, new ItemStack(blockID, quantity));
	}
	
	public void enablePlugin(String name) {
		Server.getPluginManager().enablePlugin(Server.getPluginManager().getPlugin(name));
	}
	
	public void disablePlugin(String name) {
		Server.getPluginManager().disablePlugin(Server.getPluginManager().getPlugin(name));
	}
	
	public void giveItem (String name, int id, int quant) {
		Server.getPlayer(name).getInventory().addItem(new ItemStack(id, quant));
	}
	
	public void giveItemDrop (String name, int id, int quant) {
		Server.getPlayer(name).getWorld().dropItem(Server.getPlayer(name).getLocation(), new ItemStack(id, quant));
	}
	
	public void runCommand (String command) {
		System.out.println("running command: "+command);
		Server.dispatchCommand(this, command);
	}
	
	public void runCommand (String start, String append) {
		runCommand(start+" "+append);
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
