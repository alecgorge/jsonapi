package com.alecgorge.minecraft.jsonapi.packets.netty;

//#if mc17OrNewer=="yes"
import io.netty.channel.Channel;

import com.alecgorge.minecraft.jsonapi.packets.netty.router.JSONAPIDefaultRoutes;
//#endif

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class JSONAPINettyInjector {
	//#if mc17OrNewer=="yes"
	NettyInjector injector = null;
	//#endif
	
	public JSONAPINettyInjector(final JSONAPI api) {
		//#if mc17OrNewer=="yes"
		injector = new NettyInjector() {
	        @Override
	        protected void injectChannel(Channel channel) {
	            channel.pipeline().addFirst(new JSONAPIChannelDecoder(api));
	        }
	    };
	    injector.inject();
	    
	    new JSONAPIDefaultRoutes(api);
	    //#endif
	}
	
	public void close() {
		//#if mc17OrNewer=="yes"
		injector.close();
		//#endif
	}
}
