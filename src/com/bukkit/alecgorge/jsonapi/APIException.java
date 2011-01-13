package com.bukkit.alecgorge.jsonapi;


//import org.apache.xmlrpc.XmlRpcException;

/**
 *
 * @author sk89q
 */
public class APIException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3014431928944408933L;

	public APIException(int code, String msg) {
        super("CODE: "+code+", "+msg);
    }
}
