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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
import com.alecgorge.minecraft.jsonapi.util.PropertiesFile;
import com.alecgorge.minecraft.jsonapi.util.RecursiveDirLister;
import com.alecgorge.minecraft.permissions.PermissionWrapper;

public class APIWrapperMethods {
	private Logger outLog = Logger.getLogger("JSONAPI");
	public PermissionWrapper permissions;

	public Economy econ;
	public Chat chat;

	public APIWrapperMethods(Server server) {
		permissions = new PermissionWrapper(server);

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

	public HashMap<Integer, ItemStack> removePlayerInventoryItem(String playerName, int itemID) {
		try {
			Player p = getPlayerExact(playerName);
			HashMap<Integer, ItemStack> c = p.getInventory().removeItem(new ItemStack(itemID));
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
			it.setDurability(Short.valueOf(String.valueOf(damage)).shortValue());

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

	public boolean setPlayerInventorySlotWithDataDamageAndEnchantments(String playerName, int slot, int blockID, final int data, int damage, int quantity, List<Object> enchantments) {
		try {
			if (blockID == 0) {
				return clearPlayerInventorySlot(playerName, slot);
			}

			Player p = getPlayerExact(playerName);
			PlayerInventory inv = p.getInventory();
			ItemStack it = (new MaterialData(blockID, (byte) data)).toItemStack(quantity);
			it.setDurability(Short.valueOf(String.valueOf(damage)).shortValue());

			for (int i = 0; i < enchantments.size(); i++) {
				JSONObject o = (JSONObject) enchantments.get(i);
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

	public String get345version(String name) {
		return "3.6.7"; // needed because of faulty Adminium 2.2.1 version checking
	}
	
	class FauxPlayer extends CraftPlayer {
		String name;

		public FauxPlayer(String name, FauxEntityPlayer ent) {
			super((CraftServer) JSONAPI.instance.getServer(), ent);

			this.name = name;

			((FauxNetServerHandler) getHandle().netServerHandler).setPlayer(this);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isOnline() {
			return true;
		}

		@Override
		public boolean isOp() {
			return true;
		}
		
		@Override
		public void sendRawMessage(String message) {
			if(message.isEmpty()) return;
			
			JSONAPI.instance.jsonServer.logChat("", message);
		}

		/*
		@Override
		public void sendMessage(String message) {
			if(message.isEmpty()) return;
			
			System.out.println("message: " + message);
			
			JSONAPI.instance.jsonServer.logChat("", message);
		}*/
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
			NetworkManager m = null;
			try {
				m = new NetworkManager(ss, "???", new NetHandler() {

					@Override
					public boolean a() {
						// TODO Auto-generated method stub
						return false;
					}
				}, null);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			netServerHandler = new FauxNetServerHandler(((CraftServer) getServer()).getServer(), m, this);

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
	
	// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/NetServerHandler.java#L972
	// private void handleCommand(String s)
	private boolean handleCommand(String s, Player player) {
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s, new LazyPlayerSet());
        Server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        try {
        	Logger.getLogger("Minecraft").info(event.getPlayer().getName() + " issued server command: " + event.getMessage()); // CraftBukkit
            if (Server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
                return true;
            }
        } catch (org.bukkit.command.CommandException ex) {
            player.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            Logger.getLogger(NetServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
		
        return false;
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	public boolean chatWithName(String message, String name) {
		if (fauxServer == null) {
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
			if (joinedList.containsKey(name)) {
				player = joinedList.get(name);
			} else {
				// this is the biggest hack ever.
				player = new FauxPlayer(name, new FauxEntityPlayer(((CraftServer) Server).getServer(), ((CraftWorld) Server.getWorlds().get(0)).getHandle(), name, new ItemInWorldManager(((CraftServer) Server).getServer().getWorldServer(0))));
				joinedList.put(name, player);
				
				if(Server.getPlayerExact(name) == null) {
					PlayerJoinEvent joinE = new PlayerJoinEvent(player, "jsonapi fauxplayer join");
					Server.getPluginManager().callEvent(joinE);
				}
			}

			// ((CraftServer) Server).getServer().server.getHandle().players.add(player.getHandle());
			
			final MinecraftServer minecraftServer = ((CraftServer) Server).getServer().server.getServer();
			String s = message;
			boolean async = false;
			
			if(s.startsWith("/")) {
				return handleCommand(s, player);
			}

			// copied from CraftBukkit / src / main / java / net / minecraft /
			// server / NetServerHandler.java#chat(2)
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet());
            Server.getPluginManager().callEvent(event);

            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                // Evil plugins still listening to deprecated event
                final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                Waitable waitable = new Waitable() {
                    @Override
                    protected Object evaluate() {
                        Bukkit.getPluginManager().callEvent(queueEvent);

                        if (queueEvent.isCancelled()) {
                            return null;
                        }

                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        minecraftServer.console.sendMessage(message);
                        if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                            for (Object player : minecraftServer.getServerConfigurationManager().players) {
                                ((EntityPlayer) player).sendMessage(message);
                            }
                        } else {
                            for (Player player : queueEvent.getRecipients()) {
                                player.sendMessage(message);
                            }
                        }
                        return null;
                    }};
                if (async) {
                    minecraftServer.processQueue.add(waitable);
                } else {
                    waitable.run();
                }
                try {
                    waitable.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (java.util.concurrent.ExecutionException e) {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            } else {
                if (event.isCancelled()) {
                    return true;
                }

                s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                minecraftServer.console.sendMessage(s);
                if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                    for (Object recipient : minecraftServer.getServerConfigurationManager().players) {
                        ((EntityPlayer) recipient).sendMessage(s);
                    }
                } else {
                    for (Player recipient : event.getRecipients()) {
                        recipient.sendMessage(s);
                    }
                }
            }
            
            // PlayerQuitEvent quitE = new PlayerQuitEvent(player, "jsonapi fauplayer quit");
            // Server.getPluginManager().callEvent(quitE);
            
            return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void disconnectAllFauxPlayers() {
		for (String name : joinedList.keySet()) {
			Player player = joinedList.get(name);
			PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(player, "\u00A7e" + player.getName() + " left the game.");
			Server.getPluginManager().callEvent(playerQuitEvent);
		}
	}
	
	public int broadcast(String message) {
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
			Bukkit.getOfflinePlayer(name).setBanned(true);
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
			if (data < 0 || data > 15) {
				throw new Exception("The given data needs to be in decimal form and between 0 and 15");
			}
			Player p = getPlayerExact(name);
			p.getInventory().addItem(new ItemStack(id, quant, (short) 0, Byte.valueOf(String.valueOf(data)).byteValue()));
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
			if (data < 0 || data > 15) {
				throw new Exception("The given data needs to be in decimal form and between 0 and 15");
			}
			Player p = getPlayerExact(name);
			p.getWorld().dropItem(p.getLocation(), new ItemStack(id, quant, (short) 0, Byte.valueOf(String.valueOf(data)).byteValue()));
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
				return f.delete();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
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
			File f = new File(fileName);

			f.createNewFile();

			stream = new FileOutputStream(f);
			stream.write(contents.getBytes(Charset.forName("UTF-8")));
			try {
				stream.close();
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
			Bukkit.getOfflinePlayer(playerName).setBanned(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean whitelist(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setWhitelisted(true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean unwhitelist(String playerName) {
		try {
			Bukkit.getOfflinePlayer(playerName).setWhitelisted(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean op(String playerName) {
		try {
			OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);

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
			OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);

			if (p.isOnline()) {
				p.getPlayer().sendMessage("You are no longer OP");
			}

			p.setOp(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Player getPlayerExact(String playerName) {
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
			return true;
		}

		return false;
	}
	
	public boolean setSignText(String world, int x, int y, int z, List<String> lines) {
		String[] a = new String[lines.size()];
		lines.toArray(a);
		return setSignText(world, x, y, z, a);
	}

	public boolean setSignTextLine(String world, int x, int y, int z, int line, String txt) {
		BlockState d = Server.getWorld(world).getBlockAt(x, y, z).getState();

		if (d instanceof Sign) {
			((Sign) d).setLine(line, txt);
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
