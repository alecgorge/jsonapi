package com.ramblingwood.minecraft.jsonapi.api;

public class APIMethodName {
	private String namespace = "";
	private String method = "";
	
	public APIMethodName(String namespace, String method) {
		this.namespace = namespace;
		this.method = method;
	}
	
	public APIMethodName(String combined) {
		String[] parts = combined.split("\\.", 2);
		if(parts.length == 1) {
			method = parts[0];
		}
		else {
			namespace = parts[0];
			method = parts[1];
		}
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getMethodName() {
		return method;
	}
	
	public boolean matches(String test) {
		return test.equals((namespace.equals("") ? "" : namespace+".")+method);
	}
}