package com.alecgorge.minecraft.jsonapi.api;

/**
 * This class represents an API method such as "remotetoolkit.startServer" or "getServer".
 * 
 * The part before the dot is called the namespace and the part after is called the method name.
 * 
 * @author alecgorge
 *
 */
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
	
	/**
	 * For the API method "foo.bar", this method will return "foo". For the API method "bar", this method will return "";
	 * 
	 * @return The namespace
	 */
	public String getNamespace() {
		return namespace;
	}
	
	/**
	 * For the API method "foo.bar", this method will return "bar". For the API method "bar", this method will return "bar";
	 * 
	 * @return The method name
	 */
	public String getMethodName() {
		return method;
	}
	
	/**
	 * For the API method "foo.bar", this method will return true if test is "foo.bar" and false otherwise. For the API method "bar", this method will return true if test is "bar" and false otherwise. 
	 * 
	 * @param test The fully-qualified string to test against.
	 * @return If test matches the API method represented.
	 */
	public boolean matches(String test) {
		return test.equals(this.toString());
	}
	
	public String toString () {
		return (namespace.equals("") ? "" : namespace+".")+method;		
	}
}