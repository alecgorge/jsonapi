package com.alecgorge.java.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpResponse {
	InputStream in;
	BufferedReader reader;
	InputStreamReader inreader;
	int statuscode;
	
	public HttpResponse(int statuscode, InputStream in) {
		this.statuscode = statuscode;
		this.in = in;
	}
	
	public int getStatusCode() {
		return statuscode;
	}
	
	public InputStream getInputStream() {
		return in;
	}
	
	public InputStreamReader getInputStreamReader() {
		if(inreader == null) inreader = new InputStreamReader(in);
		return inreader;
	}
	
	public BufferedReader getReader() {
		if(reader == null) reader = new BufferedReader(getInputStreamReader());
		return reader;
	}
	
	public String getReponse () {
		return toString();
	}
	
	public String toString() {
		try {
			InputStreamReader r = getInputStreamReader();
			
			StringBuilder b = new StringBuilder();
	        char[] buffer = new char[4*1024];
	        int n = 0;
	        while (n >= 0) {
	            n = r.read(buffer, 0, buffer.length);
	            if (n > 0) {
	                b.append(buffer, 0, n);
	            }
	        }
			in.close();
	        
	        return b.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
