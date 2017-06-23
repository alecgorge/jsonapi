//RTK UDP API, Revision 7
//(C) Nick Stones-Havas 2011-2012
//Revision date: March 8th, 2012
/*-------------------
 
 CHANGELOG
 
 --Revision 7--
 * Added support for preemptive hashing.
 * Fixed FORCE_STOP command.
 * Interface is no longer a singleton.
 * Replaced listener notifications with a blocking method.
 
 --Revision 6--
 * Added support for usernames/passwords with mixed case.
 
 --Revision 5--
 * Added support for restart reschedules.
 
 --Revision 4--
 * Added support for hold and start API functions.
 
 --Revision 3--
 * Added enum javadoc descriptions.
 
 --Revision 2--
 * Added Javadoc comments.
 * Made getReference() static.
 
 --Revision 1--
 * Made the singleton reference have private accesss.
 * Added an accessor method to retrieve the singleton reference.
 
 --Revision 0--
 * Initial release.
 
 -------------------*/

package com.alecgorge.minecraft.jsonapi.McRKit.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * The <em>RTKInterface</em> handles all of the UDP API calls to the RemoteToolkit Minecraft wrapper.
 * 
 * @author <a href="mailto:nick@drdanick.com">Nick Stones-Havas</a>
 * @version 7, 08/03/2012
 */
public class RTKInterface{
	//hostname
	private String host;
	
	//port
	private int port;
	
	//username
	private String user;
	
	//password
	private String password;
	
	//last recorded response from the server
	private String lastResponse;
	
	/**
	 * Defines the type of action to perform.
	 */
	public enum CommandType {
		/**
		 * Request a restart
		 */
		RESTART,
		
		/**
		 * Recquest a forceful restart
		 */
		FORCE_RESTART,
		
		/**
		 * Request a forceful stop
		 */
		FORCE_STOP,
		
		/**
		 * Request the wrapper version
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
		 * Stop the server until UNHOLD_SERVER is called
		 */
		HOLD_SERVER,
		
		/**
		 * Unhold and start a held server
		 */
		UNHOLD_SERVER,
		
		/**
		 * Reschedule the next restart.
		 * NOTE: The rescheduled restart time String is a required parameter and must be supplied when calling <code>executeCommand</code>.
		 * @see #executeCommand
		 */
		RESCHEDULE_RESTART
	}
	
	/**
	 * Instantiates an RTKInterface.
	 * @param port The port to send API events to.
	 * @param host The host name to send API events to.
	 * @param user The wrapper username to send API calls as.
	 * @param password The wrapper password that is tied to username.
	 * @param salt the String to salt the password with.
	 */
	public RTKInterface(int port, String host, String user, String password, String salt) {
		this.port = port;
		this.host = host;
		try {
			MessageDigest md5 = MessageDigest.getInstance("SHA1");
			
			this.user = RTKInterface.convertToHexString(md5.digest((user + salt).getBytes()), "UTF-8");
			
			md5.reset();
			this.password = RTKInterface.convertToHexString(md5.digest((password + salt).getBytes()), "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Get the last response that was received from the server.<p>
	 * If no response has been received from the server since the current request was made, 
	 * this method will block until either a response is recorded, or the request times out.
	 * @throws InterruptedException if the underlying wait() call is interrupted.
	 */
	public synchronized String getLastResponse() throws InterruptedException {
		if(lastResponse == null)
			wait();
		return lastResponse;
	}
	
	private void setResponse(String response) {
		lastResponse = response;
	}
	
	private static final byte[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	private static String convertToHexString(byte[] bytes, String encoding) throws UnsupportedEncodingException {
		byte[] result = new byte[bytes.length * 2];
		int i = 0;
		for(byte b : bytes) {
			result[i++] = HEX[(b & 0xf0) >>> 4];
			result[i++] = HEX[b & 0x0f];
		}
		
		return new String(result, encoding);
	}
	
	/**
	 * Executes an API function.
	 * @param type The type of wrapper function to perform.
	 * @param commandParameter used to specify extra parameters used by some CommandTypes.
	 * @throws IOException If an IOException occurs when making the request.
	 * @throws RTKInterfaceException if the command is invalid.
	 */
	public void executeCommand(CommandType type, String commandParameter) throws IOException, RTKInterfaceException {
		String packet = null;
		String suffix = ":2:"+user+":"+password;
		
		if(type==CommandType.RESTART) {
			packet = "restart"+suffix;
			
		} else if(type==CommandType.FORCE_RESTART) {
			packet = "forcerestart"+suffix;
			
		} else if(type==CommandType.FORCE_STOP) {
			packet = "forcestop"+suffix;
			
		} else if(type==CommandType.VERSION) {
			packet = "version"+suffix;
			
		} else if(type==CommandType.DISABLE_RESTARTS) {
			packet = "disable"+suffix;
			
		} else if(type==CommandType.ENABLE_RESTARTS) {
			packet = "enable"+suffix;
			
		} else if(type==CommandType.HOLD_SERVER) {
			packet = "hold"+suffix;
			
		} else if(type==CommandType.UNHOLD_SERVER) {
			packet = "unhold"+suffix;
			
		} else if(type==CommandType.RESCHEDULE_RESTART) {
			if(commandParameter==null || commandParameter.trim().equals("")) {
				throw new RTKInterfaceException("Illegal command parameter specified");
			}
			packet = "reschedule:"+commandParameter+suffix;
			
		} else {
			throw new RTKInterfaceException("Illegal command type specified");
		}
		
		dispatchUDPPacket(packet, host, port);
	}
	
	private void dispatchUDPPacket(String packet, String host, int port) throws IOException {
		final Object lock = this;
		lastResponse = null;
		final DatagramSocket ds = new DatagramSocket();
		DatagramPacket dp;
		try {
			dp = new DatagramPacket(packet.getBytes(), packet.getBytes("UTF-8").length, InetAddress.getByName(host), port);
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		
		ds.send(dp);
		
		new Thread() {
			public void run() {
				byte[] buffer = new byte[65536];
				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
				try {
					ds.setSoTimeout(5000);
					ds.receive(incoming);
					String temp = new String(incoming.getData());
					setResponse(temp.trim());
				} catch(SocketTimeoutException e){
					setResponse("timeout");
				} catch(Exception e){
					System.err.println("Unexpected Socket error: "+e);
					e.printStackTrace();
				}
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		}.start();
	}
}