package com.alecgorge.minecraft.jsonapi.packets.netty;

import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static net.minecraft.util.io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static net.minecraft.util.io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;

import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelFutureListener;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.util.io.netty.handler.codec.http.DefaultFullHttpRequest;
import net.minecraft.util.io.netty.handler.codec.http.DefaultFullHttpResponse;
import net.minecraft.util.io.netty.handler.codec.http.FullHttpRequest;
import net.minecraft.util.io.netty.handler.codec.http.FullHttpResponse;
import net.minecraft.util.io.netty.handler.codec.http.HttpMethod;
import net.minecraft.util.io.netty.handler.codec.http.QueryStringDecoder;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import net.minecraft.util.io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import net.minecraft.util.io.netty.util.CharsetUtil;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

class JSONAPIHandler extends SimpleChannelInboundHandler<Object> {
	private static final String			WEBSOCKET_PATH	= "/api/2/websocket";

	private WebSocketServerHandshaker	handshaker;
	private boolean						continueSending	= true;

	private JSONAPI						api;

	public JSONAPIHandler(JSONAPI api) {
		this.api = api;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		}
		else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		QueryStringDecoder uri = new QueryStringDecoder(req.getUri());
		if (uri.path().equals(WEBSOCKET_PATH)) {
			// Handshake
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
			handshaker = wsFactory.newHandshaker(req);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
			}
			else {
				handshaker.handshake(ctx.channel(), req);
			}
		}
		else {
			api.getRouter().serveRequest(ctx, req);
		}
	}

	private void handleWebSocketFrame(final ChannelHandlerContext ctx, WebSocketFrame frame) {
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			continueSending = false;
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}

		TextWebSocketFrame txt = ((TextWebSocketFrame) frame);

		FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.GET, txt.text());
		if (APIv2Handler.canServe(new QueryStringDecoder(req.getUri()))) {
			APIv2Handler h = new APIv2Handler(req);

			if (h.isStream()) {
				final StreamingResponse s = h.subscribe();
				(new Thread(new Runnable() {
					@Override
					public void run() {
						String line = "";

						JSONAPI.dbug("starting streaming response");
						while (continueSending && (line = s.nextLine()) != null) {
							ctx.channel().write(new TextWebSocketFrame(line.trim()));
						}

						try {
							JSONAPI.dbug("closing streaming response continueSending: " + continueSending);
							s.close();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				})).start();
			}
			else {
				ctx.channel().write(new TextWebSocketFrame(h.serve().content()));
			}
		}
		else {
			FullHttpResponse resp = api.getRouter().getResponse(ctx, req);
			ctx.channel().write(new TextWebSocketFrame(resp.content().toString(CharsetUtil.UTF_8)));
		}
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		JSONAPI.dbug("exception caught");
		continueSending = false;
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		JSONAPI.dbug("channel inactive");
		continueSending = false;
	}

	private static String getWebSocketLocation(FullHttpRequest req) {
		return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
	}
}
