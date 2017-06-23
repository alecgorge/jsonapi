//RTK UDP API
//(C) Nick Stones-Havas 2011
//Revision date: February 1st, 2011
package com.alecgorge.minecraft.jsonapi.McRKit.api;

/**
 * Thrown when an error pertaining to the RTKInterface is encountered.
 * 
 * @author <a href="mailto:nick@drdanick.com">Nick Stones-Havas</a>
 * @version 1, 09/02/2011
 */
public class RTKInterfaceException extends Exception{
	private static final long serialVersionUID = 8941405622025839196L;

	public RTKInterfaceException(String msg) {
		super(msg);
	}
}