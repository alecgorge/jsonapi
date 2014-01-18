package com.alecgorge.minecraft.jsonapi.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.bukkit.Server;

import com.alecgorge.java.http.HttpResponse;
import com.alecgorge.java.http.MutableHttpRequest;

public class BukGetAPIMethods {
	Server server;
	
	public BukGetAPIMethods(Server server) {
		this.server = server;
	}
	
	public boolean installPluginFromJARUrl(String url) throws Exception {
		MutableHttpRequest req = new MutableHttpRequest(new URL(url));
		File pluginFile = new File("plugins/", url.substring(url.lastIndexOf("/")+1));
		FileOutputStream ou = new FileOutputStream(pluginFile);
		HttpResponse resp = req.get();
		
		if(resp.getStatusCode() == 200 || resp.getStatusCode() == 301 || resp.getStatusCode() == 302) {
			InputStream jar = resp.getInputStream();
			
			int read = 0;
			byte[] bytes = new byte[1024 * 10];
	 
			while ((read = jar.read(bytes)) != -1) {
				ou.write(bytes, 0, read);
			}
			
			ou.close();
			
			server.getPluginManager().loadPlugin(pluginFile);
			
			return true;
		}
		else {
			ou.close();
			return false;
		}
	}
}
