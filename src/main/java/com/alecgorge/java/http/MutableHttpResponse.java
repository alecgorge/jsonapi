package com.alecgorge.java.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class MutableHttpResponse extends HttpResponse {
	public MutableHttpResponse(int statuscode, InputStream in, Map<String, List<String>> map) {
		super(statuscode, in, map);
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setBufferedReader(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public void setInputStreamReader(InputStreamReader inputStreamReader) {
		this.inputStreamReader = inputStreamReader;
	}

	public void setStatuscode(int statuscode) {
		this.statuscode = statuscode;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
}
