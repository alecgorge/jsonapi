package com.alecgorge.java.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class HttpResponse {
	protected InputStream inputStream;
	protected BufferedReader bufferedReader;
	protected InputStreamReader inputStreamReader;
	protected int statuscode;
	protected Map<String, List<String>> headers;

	public HttpResponse(int statuscode, InputStream in, Map<String, List<String>> map) {
		this.statuscode = statuscode;
		this.inputStream = in;
		this.headers = map;
	}

	public boolean hasHeader(String key) {
		return headers.containsKey(key);
	}

	public String getHeader(String key) {
		return headers.get(key).get(0);
	}

	public List<String> getHeaderList(String key) {
		return headers.get(key);
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statuscode;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public InputStreamReader getInputStreamReader() {
		if (inputStreamReader == null)
			inputStreamReader = new InputStreamReader(inputStream);
		return inputStreamReader;
	}

	public BufferedReader getReader() {
		if (bufferedReader == null)
			bufferedReader = new BufferedReader(getInputStreamReader());
		return bufferedReader;
	}

	public String getReponse() {
		return toString();
	}
	
	public String getBody() {
		return toString();
	}

	public String toString() {
		try {
			InputStreamReader r = getInputStreamReader();

			StringBuilder b = new StringBuilder();
			char[] buffer = new char[4 * 1024];
			int n = 0;
			while (n >= 0) {
				n = r.read(buffer, 0, buffer.length);
				if (n > 0) {
					b.append(buffer, 0, n);
				}
			}
			inputStream.close();

			return b.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
