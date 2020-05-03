package com.alecgorge.minecraft.jsonapi.packets.netty.router;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.packets.netty.APIv2Handler;

public class JSONAPIDefaultRoutes {
	JSONAPI	api;

	public JSONAPIDefaultRoutes(final JSONAPI api) {
		this.api = api;

		RouteMatcher r = api.getRouter();
		r.noMatch(new Handler<FullHttpResponse, RoutedHttpRequest>() {
			@Override
			public FullHttpResponse handle(RoutedHttpRequest event) {
				api.outLog.info("[JSONAPI] [HTTP] 404 " + event.getFullHttpRequest().getUri());
				return buildResponse(HttpResponseStatus.OK, "text/plain", event.getFullHttpRequest().getUri() + " wasn't found. This is a Minecraft server. HTTP on this port by JSONAPI. JSONAPI by Alec Gorge.\n");
			}
		});
		
		r.everyMatch(new Handler<Void, RoutedHttpResponse>() {
			
			@Override
			public Void handle(RoutedHttpResponse event) {
				api.outLog.info("[JSONAPI] [HTTP] " +
									event.getFullHttpResponse().getStatus().code() + " " +
									event.getFullHttpRequest().getMethod().toString() + " " +
									event.getFullHttpRequest().getUri());
				return null;
			}
		});

        Handler<FullHttpResponse, RoutedHttpRequest> apiHandler = new Handler<FullHttpResponse, RoutedHttpRequest>() {
            @Override
            public FullHttpResponse handle(RoutedHttpRequest event) {
                APIv2Handler h = new APIv2Handler(event.request);
                api.outLog.info("Request was matched");

                return h.serve();
            }
        };

        r.get("/api/2/call", apiHandler);
        r.post("/api/2/call", apiHandler);

        r.get("/api/2/version", apiHandler);
        r.post("/api/2/version", apiHandler);

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
