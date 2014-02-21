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
	List<Entry<String, ChannelHandler>>	handlers	= new ArrayList<Entry<String, ChannelHandler>>();
	NioEventLoopGroup					eventGroup;
	JSONAPI								api;

	public JSONAPIChannelReadHandler(JSONAPI api, final NioEventLoopGroup eventGroup) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		this.eventGroup = eventGroup;
		this.api = api;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Channel child = (Channel) msg;

		JSONAPI.dbug("channelRead pipeline hashcode: " + ctx.channel().pipeline().hashCode());

		child.pipeline().addFirst(new JSONAPIChannelDecoder(api));
		this.eventGroup.register(child);

		ctx.fireChannelRead(msg);
	}

	public class HTTPRequest extends ByteBufInputStream {

		public HTTPRequest(ByteBuf buf) {
			super(buf);
		}
	}

	public class JSONAPIChannelEncoder extends MessageToByteEncoder<HTTPRequest> {
		JSONServer	server	= JSONAPI.instance.getJSONServer();

		@Override
		protected void encode(ChannelHandlerContext ctx, HTTPRequest req, ByteBuf buf) throws Exception {
			InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();

			ByteBufOutputStream oup = new ByteBufOutputStream(buf);
			server.new HTTPSession(req, oup, addr.getAddress(), true);
		}
	}
}
//#endif
