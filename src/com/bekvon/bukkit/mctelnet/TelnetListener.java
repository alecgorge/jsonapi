/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.mctelnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.minecraft.server.ICommandListener;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author Administrator
 */
public class TelnetListener extends Handler implements CommandSender, ICommandListener {

	private boolean run;
	private boolean isAuth;
	private String authUser;
	private boolean isRoot;

	private Thread listenThread;
	Socket clientSocket;
	MinecraftServer mcserv;
	BufferedReader instream;
	BufferedWriter outstream;
	MCTelnet parent;
	String ip;
	String passRegex = "[^a-zA-Z0-9\\-\\.\\_]";
	String commandRegex = "[^a-zA-Z0-9 \\-\\.\\_]";

	public TelnetListener(Socket inSock, MinecraftServer imcserv, MCTelnet iparent) {
		run = true;
		mcserv = imcserv;
		clientSocket = inSock;
		parent = iparent;
		passRegex = parent.getConfiguration().getString("passwordRegex", passRegex);
		commandRegex = parent.getConfiguration().getString("commandRegex", commandRegex);
		ip = clientSocket.getInetAddress().toString();
		listenThread = new Thread(new Runnable() {
			public void run() {
				mainLoop();
			}
		});
		listenThread.start();
	}

	private void mainLoop() {
		try {
			instream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outstream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			//sendTelnetCommand(251,3);
			//sendTelnetCommand(253,3);
			sendTelnetCommand(251, 34);
			sendTelnetCommand(253, 34);
			sendTelnetCommand(252, 1);
			sendTelnetCommand(253, 1);
			outstream.write("[MCTelnet] - Session Started!\r\n");
			outstream.flush();
		} catch (IOException ex) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			run = false;
		}
		if (!clientSocket.getInetAddress().isLoopbackAddress() || !parent.getConfiguration().getBoolean("allowAuthlessLocalhost", false)) {
			authenticateLoop();
		} else {
			isAuth = true;
			isRoot = true;
			authUser = parent.getConfiguration().getString("rootUser");
		}
		commandLoop();
		shutdown();
	}

	private void authenticateLoop() {
		int retrys = 0;
		while (run && clientSocket.isConnected() && isAuth == false) {
			try {
				outstream.write("Username:");
				outstream.flush();
				String username = instream.readLine().replaceAll(passRegex, "");
				sendTelnetCommand(251, 1);
				sendTelnetCommand(254, 1);
				outstream.write("Password:");
				outstream.flush();
				String pw = instream.readLine().replaceAll(passRegex, "");
				outstream.write("\r\n");
				sendTelnetCommand(252, 1);
				sendTelnetCommand(253, 1);
				String rootuser = parent.getConfiguration().getString("rootUser");
				String rootpass = parent.getConfiguration().getString("rootPass");
				if (rootuser != null && !rootuser.equals("") && username.equals(rootuser)) {
					if (parent.getConfiguration().getBoolean("rootEncrypted", false)) {
						pw = MCTelnet.hashPassword(pw);
					}
					if (rootpass != null && !rootpass.equals("") && pw.equals(rootpass)) {
						authUser = rootuser;
						isAuth = true;
						isRoot = true;
					}
				} else {
					ConfigurationNode parentnode = parent.getConfiguration().getNode("users");
					if (parentnode != null) {
						ConfigurationNode usernode = parentnode.getNode(username);
						if (usernode != null) {
							String userpw = usernode.getString("password");
							if (usernode.getBoolean("passEncrypted", false)) {
								pw = MCTelnet.hashPassword(pw);
							}
							if (pw.equals(userpw)) {
								authUser = username;
								isAuth = true;
							}
						}
					}
				}
				if (isAuth) {
					outstream.write("Logged In as " + authUser + "!\r\n:");
					outstream.flush();
				} else {
					Thread.sleep(2000);
					outstream.write("Invalid Username or Password!\r\n\r\n");
					outstream.flush();
				}
				retrys++;
				if (retrys == 3 && isAuth == false) {
					try {
						outstream.write("Too many failed login attempts!");
						outstream.flush();
					} catch (Exception ex) {}
					return;
				}
			} catch (Exception ex) {
				run = false;
				authUser = null;
				isAuth = false;
				isRoot = false;
			}
		}
	}

	private void commandLoop() {
		try {
			if (isAuth) {
				ConfigurationNode usernode;
				String commands = "";
				String[] validCommands = new String[0];
				if (isRoot) {
					Logger.getLogger("Minecraft").addHandler(this);
				} else {
					usernode = parent.getConfiguration().getNode("users").getNode(authUser);
					if (usernode != null) {
						if (usernode.getString("log", "false").equals("true")) {
							Logger.getLogger("Minecraft").addHandler(this);
						}
						commands = usernode.getString("commands");
						if (commands != null) {
							validCommands = commands.split("\\,");
						}
						for (int i = 0; i < validCommands.length; i++) {
							String thisCommand = validCommands[i];
							validCommands[i] = thisCommand.trim();
						}
					}
				}
				while (run && clientSocket.isConnected() && isAuth) {
					String command = "";
					command = instream.readLine().replaceAll(commandRegex, "");
					if (command.equals("exit")) {
						run = false;
						clientSocket.close();
						return;
					}
					boolean elevate = false;
					boolean allowCommand = false;
					if (command.startsWith("sudo ")) {
						if (!isRoot) {
							elevate = true;
						}
						command = command.substring(5);
					}
					if (!isRoot) {
						for (int i = 0; i < validCommands.length; i++) {
							if (command.equals(validCommands[i]) || (command.startsWith(validCommands[i] + " "))) {
								allowCommand = true;
								i = validCommands.length;
							}
						}
					}
					if (elevate && !allowCommand) {
						sendTelnetCommand(251, 1);
						sendTelnetCommand(254, 1);
						outstream.write("Root Password:");
						outstream.flush();
						String pw = instream.readLine().replaceAll(passRegex, "");
						outstream.write("\r\n");
						sendTelnetCommand(252, 1);
						sendTelnetCommand(253, 1);
						String rootpass = parent.getConfiguration().getString("rootPass");
						if (parent.getConfiguration().getBoolean("rootEncrypted", false)) {
							pw = MCTelnet.hashPassword(pw);
						}
						if (pw.equals(rootpass)) {
							allowCommand = true;
						}
					}
					if (!clientSocket.isClosed()) {
						if (isRoot || allowCommand) {
							//((CraftServer)getServer()).dispatchCommand(new ConsoleCommandSender(getServer()), command);
							mcserv.issueCommand(command, this);
							System.out.println("[MCTelnet] " + authUser + " issued command: " + command);

						} else {
							if (!command.equals("")) {
								outstream.write("You do not have permission to use this command...\r\n:");
								outstream.flush();
							}
						}
					}
				}
			}
		} catch (Exception ex) {}
	}

	public boolean isAlive() {
		return run;
	}

	public void killClient() {
		try {
			run = false;
			outstream.write("[MCTelnet] - Closing Connection!");
			clientSocket.close();
		} catch (IOException ex) {}
	}

	private void shutdown() {
		try {
			run = false;
			Logger.getLogger("Minecraft").removeHandler(this);
			Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] Closing connection: " + ip);
			if (!clientSocket.isClosed()) {
				outstream.write("[MCTelnet] - Closing Connection!");
				clientSocket.close();
			}
			mcserv = null;
			parent = null;
		} catch (Exception ex) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			run = false;
		}
	}

	@Override
	public void publish(LogRecord record) {
		try {
			if (!clientSocket.isClosed()) {
				outstream.write(record.getMessage() + "\r\n:");
				outstream.flush();
			}
		} catch (IOException ex) {}
	}

	@Override
	public void flush() {
		if (clientSocket.isConnected()) {
			try {
				outstream.flush();
			} catch (IOException ex) {}
		}
	}


	public void sendMessage(String string) {
		if (clientSocket.isConnected()) {
			try {
				outstream.write(string + "\r\n:");
				outstream.flush();
			} catch (IOException ex) {}
		}
	}

	public boolean isOp() {
		if (parent.getConfiguration().getBoolean("allowOPsAll", false)) return parent.getServer().getPlayer(authUser).isOp();
		return false;
	}

	public boolean isPlayer() {
		return false;
	}

	public Server getServer() {
		return parent.getServer();
	}

	@Override
	public void close() throws SecurityException {
		shutdown();
	}

	private void sendTelnetCommand(int command, int option) {
		if (clientSocket.isConnected()) {
			try {
				String tcmd = ("" + ((char) 255) + ((char) command) + ((char) option));
				outstream.write(tcmd);
				outstream.flush();
			} catch (IOException ex) {}
		}
	}

	public String getName() {
		return authUser;
	}
}