package com.alecgorge.minecraft.jsonapi.packets.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.io.IOException;

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
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		QueryStringDecoder uri = new QueryStringDecoder(req.getUri());
		if (uri.path().equals(WEBSOCKET_PATH)) {
			// Handshake
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
			handshaker = wsFactory.newHandshaker(req);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
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

		FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, txt.text());
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
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
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
		return "ws://" + req.headers().get(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
	}
}
