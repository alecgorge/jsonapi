package com.alecgorge.minecraft.jsonapi.packets.netty.router;

import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.handler.codec.http.FullHttpRequest;

public class RoutedHttpRequest {
	ChannelHandlerContext	ctx;
	FullHttpRequest			request;
	
	public RoutedHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		this.ctx = ctx;
		this.request = request;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}

	public FullHttpRequest getFullHttpRequest() {
		return request;
	}
}
