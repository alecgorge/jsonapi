package com.alecgorge.minecraft.jsonapi.packets.netty.router;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class RoutedHttpResponse {
	FullHttpResponse	res;
	FullHttpRequest		request;

	public RoutedHttpResponse(FullHttpRequest request, FullHttpResponse res) {
		this.request = request;
		this.res = res;
	}

	public FullHttpResponse getFullHttpResponse() {
		return res;
	}

	public FullHttpRequest getFullHttpRequest() {
		return request;
	}
}
