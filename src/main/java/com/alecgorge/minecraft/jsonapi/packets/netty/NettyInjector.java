package com.alecgorge.minecraft.jsonapi.packets.netty;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

import java.lang.reflect.Field;
import java.util.List;

//#if mc17OrNewer=="yes"
//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.*;
//#else
import net.minecraft.server.v1_7_R1.*;
import org.bukkit.craftbukkit.v1_7_R1.*;
//#endif

import net.minecraft.util.io.netty.channel.ChannelPromise;
import net.minecraft.util.io.netty.channel.nio.NioEventLoopGroup;
//#endif

import org.bukkit.Bukkit;

public class NettyInjector {
	public NettyInjector() {
		injectNewChildHandler();
	}
	
	@SuppressWarnings("unchecked")
	void injectNewChildHandler() {
		//#if "a" == "b" && mc17OrNewer=="yes"
		try {
			CraftServer craftServer = (CraftServer)Bukkit.getServer();
			MinecraftServer mcServer = craftServer.getServer();			
			ServerConnection sCon = mcServer.ag();
			
			Field field_channelPromiseList = sCon.getClass().getDeclaredField("e");
			field_channelPromiseList.setAccessible(true);
			
			Field field_eventGroup = sCon.getClass().getDeclaredField("c");
			field_eventGroup.setAccessible(true);
			
			List<ChannelPromise> channelPromiseList = (List<ChannelPromise>)field_channelPromiseList.get(sCon);

			for(ChannelPromise promise : channelPromiseList) {
				NioEventLoopGroup eventGroup = (NioEventLoopGroup) field_eventGroup.get(sCon);

				JSONAPI.dbug(promise.channel().pipeline().addFirst("jsonapi", new JSONAPIChannelReadHandler(eventGroup)));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		//#endif
	}
}
