package com.alecgorge.minecraft.jsonapi.packets.netty;

import io.netty.channel.Channel;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.packets.netty.router.JSONAPIDefaultRoutes;

public class JSONAPINettyInjector {
	NettyInjector injector = null;
	
	public JSONAPINettyInjector(final JSONAPI api) {
		injector = new NettyInjector() {
	        @Override
	        protected void injectChannel(Channel channel) {
	            channel.pipeline().addFirst(new JSONAPIChannelDecoder(api));
	        }
	    };
	    injector.inject();
	    
	    new JSONAPIDefaultRoutes(api);
	}
	
	public void close() {
		injector.close();
	}
}
