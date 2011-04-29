package com.bekvon.bukkit.mctelnet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map.Entry;
import org.bukkit.util.config.ConfigurationNode;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class MCTelnet extends JavaPlugin {
	private ServerSocket listenerSocket;
	private ArrayList < TelnetListener > clientHolder;
	private MinecraftServer mcserv;
	private Thread listenerThread;
	private boolean run = false;
	int port = 8765;
	InetAddress listenAddress;

	public MCTelnet() {

	}

	//public MCTelnet(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
	//    super(pluginLoader, instance, desc, folder, plugin, cLoader);
	//}
	public void onDisable() {
		run = false;
		if (listenerSocket != null) {
			try {
				synchronized(listenerSocket) {
					if (listenerSocket != null) listenerSocket.close();
				}
			} catch (IOException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Logger.getLogger(MCTelnet.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void onEnable() {
		try {
			Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] - Starting Up! Version: " + this.getDescription().getVersion() + " by bekvon");
			run = true;
			this.getConfiguration().load();
			testConfig();
			if (this.getConfiguration().getBoolean("encryptPasswords", false)) encryptPasswords();
			port = this.getConfiguration().getInt("telnetPort", port);
			try {
				String address = this.getConfiguration().getString("listenAddress", null);
				if (address != null) listenAddress = InetAddress.getByName(address);
			} catch (Exception ex) {
				System.out.println("[MCTelnet] Exception when trying to binding to custom address:" + ex.getMessage());
			}
			if (listenAddress != null) {
				listenerSocket = new java.net.ServerSocket(port, 10, listenAddress);
			} else {
				listenerSocket = new java.net.ServerSocket(port, 10);
			}
			clientHolder = new ArrayList < TelnetListener > ();
			listenerThread = new Thread(new Runnable() {
				public void run() {
					acceptConnections();
				}
			});
			listenerThread.start();
			Field cfield = CraftServer.class.getDeclaredField("console");
			cfield.setAccessible(true);
			mcserv = (MinecraftServer) cfield.get((CraftServer) getServer());
			Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] - Listening on: " + listenerSocket.getInetAddress().getHostAddress() + ":" + port);
		} catch (Exception ex) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, "[MCTelnet] - Unable to Enable! Error: " + ex.getMessage());
			this.setEnabled(false);
		}
	}

	private void encryptPasswords() {
		boolean isEncrypt = this.getConfiguration().getBoolean("rootEncrypted", false);
		if (!isEncrypt) {
			this.getConfiguration().setProperty("rootPass", hashPassword(this.getConfiguration().getString("rootPass")));
			this.getConfiguration().setProperty("rootEncrypted", true);
			this.getConfiguration().save();
		}
		Map < String, ConfigurationNode > users = this.getConfiguration().getNodes("users");
		if (users != null) {
			Iterator < Entry < String, ConfigurationNode >> thisIt = users.entrySet().iterator();
			if (thisIt != null) {
				while (thisIt.hasNext()) {
					Entry < String, ConfigurationNode > thisEntry = thisIt.next();
					if (thisEntry != null) {
						ConfigurationNode thisNode = thisEntry.getValue();
						if (thisNode != null && !thisNode.getBoolean("passEncrypted", false)) {
							this.getConfiguration().setProperty("users." + thisEntry.getKey() + ".password", hashPassword(thisNode.getString("password")));
							this.getConfiguration().setProperty("users." + thisEntry.getKey() + ".passEncrypted", true);
							this.getConfiguration().save();
						}
					}
				}
			}
		}
	}

	public static String hashPassword(String password) {
		String hashword = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(password.getBytes());
			BigInteger hash = new BigInteger(1, md5.digest());
			hashword = hash.toString(16);
		} catch (NoSuchAlgorithmException nsae) {}
		return hashword;
	}

	private void acceptConnections() {
		while (run) {
			try {
				Socket client = listenerSocket.accept();
				if (client != null) {
					clientHolder.add(new TelnetListener(client, mcserv, this));
					System.out.print("[MCTelnet] - Client connected: " + client.getInetAddress().toString());
				}
				for (int i = 0; i < clientHolder.size(); i++) {
					TelnetListener thisListener = clientHolder.get(i);
					if (thisListener.isAlive() == false) clientHolder.remove(i);
				}
			} catch (IOException ex) {
				run = false;
			}
		}
		Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] - Shutting Down!");
		for (int i = 0; i < clientHolder.size(); i++) {
			TelnetListener temp = clientHolder.get(i);
			temp.killClient();
		}
		listenerSocket = null;
		mcserv = null;
		clientHolder.clear();
		clientHolder = null;
		this.setEnabled(false);
	}

	private void testConfig() {
		String testConfig = this.getConfiguration().getString("telnetPort");
		if (testConfig == null || testConfig.equals("")) {
			this.getConfiguration().setProperty("telnetPort", 8765);
			this.getConfiguration().save();
		}
		testConfig = this.getConfiguration().getString("listenAddress");
		if (testConfig == null || testConfig.equals("")) {
			this.getConfiguration().setProperty("listenAddress", "0.0.0.0");
			this.getConfiguration().save();
		}
		testConfig = this.getConfiguration().getString("rootPass");
		if (testConfig == null || testConfig.equals("")) {
			this.getConfiguration().setProperty("rootPass", "abcd");
			this.getConfiguration().setProperty("rootEncrypted", false);
			this.getConfiguration().save();
		}
		testConfig = this.getConfiguration().getString("rootUser");
		if (testConfig == null || testConfig.equals("")) {
			this.getConfiguration().setProperty("rootUser", "console");
			this.getConfiguration().save();
		}
		testConfig = this.getConfiguration().getString("encryptPasswords");
		if (testConfig == null || testConfig.equals("")) {
			this.getConfiguration().setProperty("encryptPasswords", true);
			this.getConfiguration().save();
		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (this.isEnabled()) {
			if (cmd.getName().equals("telnetreload")) {
				if (sender instanceof ConsoleCommandSender) {
					this.getConfiguration().load();
					testConfig();
					if (this.getConfiguration().getBoolean("encryptPasswords", false)) encryptPasswords();
					sender.sendMessage("[MCTelnet] - Reloaded Config...");
					for (int i = 0; i < clientHolder.size(); i++) {
						TelnetListener thisListener = clientHolder.get(i);
						thisListener.sendMessage("[MCTelnet] - Telnet Restarting...");
						thisListener.killClient();
					}
				}
				return true;
			}
		}
		return super.onCommand(sender, cmd, commandLabel, args);
	}
}