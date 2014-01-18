//#if mc17OrNewer=="yes"
package com.alecgorge.minecraft.jsonapi.packets.netty;

import java.nio.charset.Charset;
import java.util.List;
import java.util.NoSuchElementException;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelOption;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.util.io.netty.handler.codec.http.HttpRequestDecoder;
import net.minecraft.util.io.netty.handler.codec.http.HttpResponseEncoder;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;

public class JSONAPIChannelDecoder extends ByteToMessageDecoder {
	JSONServer	server	= JSONAPI.instance.getJSONServer();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
		// use 4 bytes to detect HTTP or abort
		if (buf.readableBytes() < 4) {
			return;
		}

		buf.retain().retain();

		final int magic1 = buf.getUnsignedByte(buf.readerIndex());
		final int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);
		final int magic3 = buf.getUnsignedByte(buf.readerIndex() + 2);
		final int magic4 = buf.getUnsignedByte(buf.readerIndex() + 3);

		JSONAPI.dbug("decode channel pipeline hashcode: " + ctx.channel().pipeline().hashCode());
		JSONAPI.dbug("decode parent pipeline hashcode: " + ctx.channel().parent().pipeline().hashCode());
		JSONAPI.dbug("decode context pipeline hashcode: " + ctx.pipeline().hashCode());

		ChannelPipeline p = ctx.channel().parent().pipeline();
		if (isHttp(magic1, magic2, magic3, magic4)) {
			JSONAPI.dbug("found http: " + buf.toString(Charset.forName("UTF-8")));

			ctx.channel().config().setOption(ChannelOption.TCP_NODELAY, true);

			try {
				while (p.removeLast() != null)
					JSONAPI.dbug("removed a handler: " + p);
			}
			catch (NoSuchElementException e) {

			}

			JSONAPI.dbug("removed all");

			p.addFirst("encoder2", new HttpResponseEncoder());
			p.addFirst("handler", new HttpSnoopServerHandler());
			p.addFirst("encoder", new HttpResponseEncoder());
			p.addFirst("decoder", new HttpRequestDecoder());

			JSONAPI.dbug(p);

			p.fireChannelRead(buf);
		}
		else {
			try {
				p.remove(this);
			}
			catch (NoSuchElementException e) {
				// probably okay, it just needs to be off
				JSONAPI.dbug("NoSuchElementException");
			}

			JSONAPI.dbug(p);
		}
	}

	private boolean isHttp(int magic1, int magic2, int magic3, int magic4) {
		return magic1 == 'G' && magic2 == 'E' && magic3 == 'T' && magic4 == ' ' || // GET
		magic1 == 'P' && magic2 == 'O' && magic3 == 'S' && magic4 == 'T' || // POST
		magic1 == 'P' && magic2 == 'U' && magic3 == 'T' && magic4 == ' ' || // PUT
		magic1 == 'H' && magic2 == 'E' && magic3 == 'A' && magic4 == 'D' || // HEAD
		magic1 == 'O' && magic2 == 'P' && magic3 == 'T' && magic4 == 'I' || // OPTIONS
		magic1 == 'P' && magic2 == 'A' && magic3 == 'T' && magic4 == 'C' || // PATCH
		magic1 == 'D' && magic2 == 'E' && magic3 == 'L' && magic4 == 'E' || // DELETE
		magic1 == 'T' && magic2 == 'R' && magic3 == 'C' && magic4 == 'C' || // TRACE
		magic1 == 'C' && magic2 == 'O' && magic3 == 'N' && magic4 == 'N'; // CONNECT
	}
}
// #endif
