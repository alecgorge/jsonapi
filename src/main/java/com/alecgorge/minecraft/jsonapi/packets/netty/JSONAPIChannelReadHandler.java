//#if mc17OrNewer=="yes"
package com.alecgorge.minecraft.jsonapi.packets.netty;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.ByteBufInputStream;
import net.minecraft.util.io.netty.buffer.ByteBufOutputStream;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandler.Sharable;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.nio.NioEventLoopGroup;
import net.minecraft.util.io.netty.handler.codec.MessageToByteEncoder;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;

@Sharable
public class JSONAPIChannelReadHandler extends ChannelInboundHandlerAdapter {
	NioEventLoopGroup eventGroup;

	public JSONAPIChannelReadHandler(final NioEventLoopGroup eventGroup) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		this.eventGroup = eventGroup;
	}

	List<Entry<String, ChannelHandler>> handlers = new ArrayList<Entry<String, ChannelHandler>>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Channel child = (Channel) msg;

		JSONAPI.dbug("channelRead pipeline hashcode: " + ctx.channel().pipeline().hashCode());
		
//		final NioEventLoopGroup e = this.eventGroup;
		child.pipeline().addFirst(new JSONAPIChannelDecoder());
		
//		ChannelPipeline p = child.pipeline();
//		p.addFirst("http_handler", new ChannelInboundHandlerAdapter() {
//			@Override
//			public void channelRead(ChannelHandlerContext ctx, Object msg) {
//				System.out.println("read: " + msg);
//
//				if (msg instanceof HttpRequest) {
//					DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("test data", CharsetUtil.US_ASCII));
//
//					res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
//					res.headers().set(HttpHeaders.Names.CONTENT_LENGTH, res.content().readableBytes());
//
//					ctx.write(res).addListener(ChannelFutureListener.CLOSE);
//				}
//			}
//
//			@Override
//			public void channelReadComplete(ChannelHandlerContext ctx) {
//				System.out.println("read complete");
//				ctx.flush();
//			}
//		});
//        p.addFirst("http_encoder", new HttpResponseEncoder());
//        p.addFirst("http_decoder", new HttpRequestDecoder());
		
		this.eventGroup.register(child);

		ctx.fireChannelRead(msg);
	}

	public class HTTPRequest extends ByteBufInputStream {

		public HTTPRequest(ByteBuf buf) {
			super(buf);
		}
	}

	public class JSONAPIChannelEncoder extends MessageToByteEncoder<HTTPRequest> {
		JSONServer server = JSONAPI.instance.getJSONServer();

		@Override
		protected void encode(ChannelHandlerContext ctx, HTTPRequest req, ByteBuf buf) throws Exception {
			InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();

			ByteBufOutputStream oup = new ByteBufOutputStream(buf);
			server.new HTTPSession(req, oup, addr.getAddress(), true);
		}
	}
}
//#endif
