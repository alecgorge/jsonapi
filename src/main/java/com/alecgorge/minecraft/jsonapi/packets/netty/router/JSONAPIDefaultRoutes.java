package com.alecgorge.minecraft.jsonapi.packets.netty.router;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.handler.codec.http.DefaultFullHttpResponse;
import net.minecraft.util.io.netty.handler.codec.http.FullHttpResponse;
import net.minecraft.util.io.netty.handler.codec.http.HttpResponseStatus;
import net.minecraft.util.io.netty.handler.codec.http.HttpVersion;
import net.minecraft.util.io.netty.util.CharsetUtil;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.packets.netty.APIv2Handler;

public class JSONAPIDefaultRoutes {
	JSONAPI	api;

	public JSONAPIDefaultRoutes(JSONAPI api) {
		this.api = api;

		RouteMatcher r = api.getRouter();
		r.noMatch(new Handler<FullHttpResponse, RoutedHttpRequest>() {
			@Override
			public FullHttpResponse handle(RoutedHttpRequest event) {
				return buildResponse(HttpResponseStatus.OK, "text/plain", "Whatever you wanted wasn't found. This is a Minecraft server. HTTP on this port by JSONAPI. JSONAPI by Alec Gorge.\n");
			}
		});
		
		r.get("/api/2/call", new Handler<FullHttpResponse, RoutedHttpRequest>() {
			@Override
			public FullHttpResponse handle(RoutedHttpRequest event) {
				APIv2Handler h = new APIv2Handler(event.request);

				return h.serve();
			}
		});

		r.get("/api/2/version", new Handler<FullHttpResponse, RoutedHttpRequest>() {
			@Override
			public FullHttpResponse handle(RoutedHttpRequest event) {
				APIv2Handler h = new APIv2Handler(event.request);

				return h.serve();
			}
		});

		r.get("/", new Handler<FullHttpResponse, RoutedHttpRequest>() {
			@Override
			public FullHttpResponse handle(RoutedHttpRequest event) {
				return buildResponse(HttpResponseStatus.OK, "text/plain", "This is a Minecraft server. HTTP on this port by JSONAPI. JSONAPI by Alec Gorge.\n");
			}
		});
	}
	
	public static FullHttpResponse buildResponse(HttpResponseStatus resp, String type, String body) {
		ByteBuf buf = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
		FullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, resp, buf);
		r.headers().set("Access-Control-Allow-Origin", "*");
		r.headers().set("Content-Length", buf.readableBytes());

		return r;
	}
}
