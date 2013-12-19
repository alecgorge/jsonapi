package com.alecgorge.minecraft.jsonapi.packets.netty;

import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.getHost;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static net.minecraft.util.io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static net.minecraft.util.io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static net.minecraft.util.io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static net.minecraft.util.io.netty.handler.codec.http.HttpResponseStatus.OK;
import static net.minecraft.util.io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.util.io.netty.handler.codec.DecoderResult;
import net.minecraft.util.io.netty.handler.codec.http.Cookie;
import net.minecraft.util.io.netty.handler.codec.http.CookieDecoder;
import net.minecraft.util.io.netty.handler.codec.http.DefaultFullHttpResponse;
import net.minecraft.util.io.netty.handler.codec.http.FullHttpResponse;
import net.minecraft.util.io.netty.handler.codec.http.HttpContent;
import net.minecraft.util.io.netty.handler.codec.http.HttpHeaders;
import net.minecraft.util.io.netty.handler.codec.http.HttpObject;
import net.minecraft.util.io.netty.handler.codec.http.HttpRequest;
import net.minecraft.util.io.netty.handler.codec.http.LastHttpContent;
import net.minecraft.util.io.netty.handler.codec.http.QueryStringDecoder;
import net.minecraft.util.io.netty.handler.codec.http.ServerCookieEncoder;
import net.minecraft.util.io.netty.util.CharsetUtil;
import net.minecraft.util.io.netty.util.concurrent.Future;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;
    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    	JSONAPI.dbug("snoop context flush");
        ctx.flush();
        ctx.fireChannelReadComplete();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	JSONAPI.dbug("channelRead: " + msg.getClass() + ": " + msg);
    	super.channelRead(ctx, msg);
    } 
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    	JSONAPI.dbug("channelRead0: " + msg.getClass() + ": " + msg);
    	
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            buf.setLength(0);
            buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            buf.append("===================================\r\n");

            buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
            buf.append("HOSTNAME: ").append(getHost(request, "unknown")).append("\r\n");
            buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");

            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> h: headers) {
                    String key = h.getKey();
                    String value = h.getValue();
                    buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
                }
                buf.append("\r\n");
            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
            if (!params.isEmpty()) {
                for (Entry<String, List<String>> p: params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                    }
                }
                buf.append("\r\n");
            }

            appendDecoderResult(buf, request);
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                buf.append("END OF CONTENT\r\n");

                LastHttpContent trailer = (LastHttpContent) msg;
                if (!trailer.trailingHeaders().isEmpty()) {
                    buf.append("\r\n");
                    for (String name: trailer.trailingHeaders().names()) {
                        for (String value: trailer.trailingHeaders().getAll(name)) {
                            buf.append("TRAILING HEADER: ");
                            buf.append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                writeResponse(trailer, ctx);
            }
        }
    }

    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
        DecoderResult result = o.getDecoderResult();
        if (result.isSuccess()) {
            return;
        }

        buf.append(".. WITH DECODER FAILURE: ");
        buf.append(result.cause());
        buf.append("\r\n");
        
        JSONAPI.dbug("buf: " + buf.toString());
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.getDecoderResult().isSuccess()? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Encode the cookie.
        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie: cookies) {
                    response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
            response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"));
            response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"));
        }
        
        JSONAPI.dbug("response: " + response);

        // Write the response.
        ctx.pipeline().write(response).addListener(new GenericFutureListener<Future<? super Void>>() {

			@Override
			public void operationComplete(Future<? super Void> arg0) throws Exception {
				if(arg0.cause() != null) {
					arg0.cause().printStackTrace();
				}
				
				JSONAPI.dbug("writeAndFlush http snoop: " + arg0);
			}
		});

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
	}
