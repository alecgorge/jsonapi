//RTK UDP API, Revision 5
//(C) Nick Stones-Havas 2011
//Revision date: April 27th, 2011
/*-------------------
 
 CHANGELOG
 
 --Revision 5--
 * Added support for restart reschedules
 
 --Revision 4--
 * Added support for hold and start API functions
 
 --Revision 3--
 * Added enum javadoc descriptions
 
 --Revision 2--
 * Added Javadoc comments
 * Made getReference() static.
 
 --Revision 1--
 * Made the singleton reference have private accesss.
 * Added an accessor method to retrieve the singleton reference
 
 --Revision 0--
 * Initial release
 
 -------------------*/

package com.ramblingwood.minecraft.jsonapi.McRKit.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;


/**
 * The <em>RTKInterface</em> handles all of the UDP API calls to the RemoteToolkit Minecraft wrapper.
 * 
 * @author <a href="mailto:nick@drdanick.com">Nick Stones-Havas</a>
 * @version 2, 09/02/2011
 */
public class RTKInterface{
	//singleton reference
	private static RTKInterface singleton = null;
	
	//hostname
	private String host;
	
	//port
	private int port;
	
	//username
	private String user;
	
	//password
	private String password;
	
	//registered listeners
	private LinkedList<RTKListener> listeners;
	
	/**
	 * Defines the type of action to perform.
	 */
	public enum CommandType{
		/**
		 * Requests a restart
		 */
		RESTART,
		/**
		 * Recquests a forceful restart
		 */
		FORCE_RESTART,
		/**
		 * Requests a forceful stop
		 */
		FORCE_STOP,
		/**
		 * Requests the wrapper version
		 */
		VERSION,
		/**
		 * Disables wrapper restarts <b>WARNING: As of now this does not stop scheduled events, only the restart-on-stop event.</b>
		 */
		DISABLE_RESTARTS,
		/**
		 * Enables wrapper restarts
		 */
		ENABLE_RESTARTS,
		
		/**
		 * Stops the server until START_SERVER is called
		 */
		HOLD_SERVER,
		
		/**
		 * Unholds and starts a held server
		 */
		UNHOLD_SERVER,
		
		/**
		 * Reschedules the next restart.
		 * NOTE: The rescheduled restart time String is a required parameter and must be supplied when calling <code>executeCommand</code>.
		 * @see #executeCommand
		 */
		RESCHEDULE_RESTART
	}
	
	private RTKInterface(int port, String host, String user, String password){
		this.port=port;
		this.host=host;
		singleton=this;
		this.user=user.toLowerCase();
		this.password=password.toLowerCase();
		listeners = new LinkedList<RTKListener>();
	}
	/**
	 * Instantiates an RTKInterface singleton.
	 * @param port The port to send API events to.
	 * @param host The host name to send API events to.
	 * @param user The wrapper username to send API calls as.
	 * @param password The wrapper password that is tied to username.
	 * @throws RTKInterfaceException if RTKInterface has already been instantiated.
	 */
	public static RTKInterface createRTKInterface(int port,String host,String user,String password)throws RTKInterfaceException{
		if(singleton==null){
			return new RTKInterface(port,host,user,password);
		}else{
			throw new RTKInterfaceException("RTKInterface already instantiated.");
		}
	}
	
	/**
	 * Returns the singleton reference
	 */
	public static RTKInterface getReference(){
		return singleton;
	}
	
	/**
	 * Registers an RTKListener to dispatch inbound events to.
	 * @param listener The listener that is to be registered.
	 */
	public void registerRTKListener(RTKListener listener){
		listeners.add(listener);
	}
	/**
	 * Deregisters an RTKListener.
	 * @param listener The listener that is to be deregistered.
	 */
	public void deregisterRTKListener(RTKListener listener){
		listeners.remove(listener);
	}
	
	private void despatchStringToListeners(String s){
		for(RTKListener listener:listeners)
			listener.onRTKStringReceived(s);
	}
	
	/**
	 * Executes an api function.
	 * @param type The type of wrapper function to perform.
	 * @param commandParameter used to specify extra parameters used by some CommandTypes.
	 * @throws IOException
	 */
	public void executeCommand(CommandType type, String commandParameter)throws IOException, RTKInterfaceException{
		String packet = null;
		String suffix = ":"+user+":"+password;
		if(type==CommandType.RESTART)
			packet = "restart"+suffix;
		else if(type==CommandType.FORCE_RESTART)
			packet = "forcerestart"+suffix;
		else if(type==CommandType.FORCE_STOP)
			packet = "forcerestop"+suffix;
		else if(type==CommandType.VERSION)
			packet = "version"+suffix;
		else if(type==CommandType.DISABLE_RESTARTS)
			packet = "disable"+suffix;
		else if(type==CommandType.ENABLE_RESTARTS)
			packet = "enable"+suffix;
		else if(type==CommandType.HOLD_SERVER)
			packet = "hold"+suffix;
		else if(type==CommandType.UNHOLD_SERVER)
			packet = "unhold"+suffix;
		else if(type==CommandType.RESCHEDULE_RESTART){
			if(commandParameter==null || commandParameter.trim().equals(""))
				throw new RTKInterfaceException("Illegal command parameter specified");
			packet = "reschedule:"+commandParameter+suffix;
		}else
			throw new RTKInterfaceException("Illegal command type specified");
		dispatchUDPPacket(packet,host,port);
	}
	
	private void dispatchUDPPacket(String packet, String host, int port)throws IOException{
		try{
			final DatagramSocket ds = new DatagramSocket();
			DatagramPacket dp = new DatagramPacket(packet.getBytes(), packet.getBytes().length,InetAddress.getByName(host),port);
			ds.send(dp);
			new Thread(){
				public void run(){
					byte[] buffer = new byte[65536];
					DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
					try{
						ds.setSoTimeout(5000);
						ds.receive(incoming);
						String temp = new String(incoming.getData());
						despatchStringToListeners(temp.trim());
					}catch(SocketTimeoutException e){
						despatchStringToListeners("RTK_TIMEOUT");
					}catch(Exception e){
						System.err.println("Unexpected Socket error: "+e);
						e.printStackTrace();
					}
				}
			}.start();
		}catch(IOException e){
			throw e;
		}
	}
}