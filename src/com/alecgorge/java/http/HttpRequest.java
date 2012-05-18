package com.alecgorge.java.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
	String method = "GET";
	URL url;
	List<String> postParamKeys = new ArrayList<String>();
	List<String> postParamValues = new ArrayList<String>();

	List<String> getParamKeys = new ArrayList<String>();
	List<String> getParamValues = new ArrayList<String>();

	Map<String, String> headers = new HashMap<String, String>();

	HttpURLConnection conn = null;
	InputStream in = null;

	public int timeout = 10000;

	public static HttpRequest create(URL u) throws IOException {
		return new HttpRequest(u);
	}

	public HttpRequest(URL u) throws IOException {
		url = u;
	}

	public List<String> getPostKeys() {
		return postParamKeys;
	}

	public List<String> getPostValues() {
		return postParamValues;
	}

	public List<String> getGetKeys() {
		return getParamKeys;
	}

	public List<String> getGetValues() {
		return getParamValues;
	}

	public HttpRequest addPostValue(String key, String value) {
		postParamKeys.add(key);
		postParamValues.add(value);
		return this;
	}

	public HttpRequest addPostValue(String key, Object value) {
		addPostValue(key, value.toString());
		return this;
	}

	public HttpRequest setPostValues(Map<String, String> map) {
		postParamKeys = new ArrayList<String>(map.keySet());
		postParamValues = new ArrayList<String>(map.values());
		return this;
	}

	public HttpRequest addGetValue(String key, String value) {
		getParamKeys.add(key);
		getParamValues.add(value);
		return this;
	}

	public HttpRequest addGetValue(String key, Object value) {
		addGetValue(key, value.toString());
		return this;
	}

	public HttpRequest setGetValues(Map<String, String> map) {
		getParamKeys = new ArrayList<String>(map.keySet());
		getParamValues = new ArrayList<String>(map.values());
		return this;
	}

	public HttpRequest setHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}

	public HttpRequest setHeaders(Map<String, String> map) {
		headers = map;
		return this;
	}

	public HttpRequest setMethod(String m) {
		method = m;
		return this;
	}

	public int timeout() {
		return this.timeout;
	}

	public HttpRequest setTimeout(int timeoutMilliseconds) {
		timeout = timeoutMilliseconds;
		return this;
	}

	private String getGetURL() {
		StringBuilder b = new StringBuilder();

		String prefix = "";
		int l = getParamKeys.size();
		for (int i = 0; i < l; i++) {
			String k = getParamKeys.get(i);

			b.append(prefix);
			prefix = "&";

			b.append(encode(k)).append("=").append(encode(getParamValues.get(i)));
		}

		return b.toString();
	}

	private String getPostParms() {
		StringBuilder b = new StringBuilder();

		String prefix = "";
		int l = postParamKeys.size();
		for (int i = 0; i < l; i++) {
			String k = postParamKeys.get(i);

			b.append(prefix);
			prefix = "&";

			b.append(encode(k)).append("=").append(encode(postParamValues.get(i)));
		}

		return b.toString();
	}

	public HttpResponse request() throws IOException {
		return request(method);
	}

	public HttpResponse request(String requestMethod) throws IOException, SocketTimeoutException {
		if (getParamKeys.size() > 0) {
			String parms = getGetURL();

			String us = url.toString();
			if (us.contains("?")) {
				url = new URL(url.toString().concat(parms));
			} else {
				url = new URL(url.toString().concat("?").concat(parms));
			}
		}

		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(timeout);
		conn.setRequestMethod(requestMethod.toUpperCase());
		conn.setDoInput(true);

		if (headers.size() > 0) {
			for (String k : headers.keySet()) {
				conn.setRequestProperty(k, headers.get(k));
			}
		}

		if (postParamKeys.size() > 0) {
			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(getPostParms());
			wr.flush();
			wr.close();
		}

		if (conn.getResponseCode() >= 400) {
			in = conn.getErrorStream();
		} else {
			in = conn.getInputStream();
		}

		return new HttpResponse(conn.getResponseCode(), in, conn.getHeaderFields());
	}

	public HttpResponse get() throws IOException, SocketTimeoutException {
		return request("GET");
	}

	public HttpResponse head() throws IOException, SocketTimeoutException {
		return request("HEAD");
	}

	public HttpResponse post() throws IOException, SocketTimeoutException {
		return request("POST");
	}

	public HttpResponse put() throws IOException, SocketTimeoutException {
		return request("PUT");
	}

	public HttpResponse delete() throws IOException, SocketTimeoutException {
		return request("DELETE");
	}

	private String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	public void close() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (conn != null)
			conn.disconnect();
	}
}
