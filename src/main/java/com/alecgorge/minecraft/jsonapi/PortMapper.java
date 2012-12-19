package com.alecgorge.minecraft.jsonapi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.PortMappingEntry;
import org.xml.sax.SAXException;

import com.hoodcomputing.natpmp.ExternalAddressRequestMessage;
import com.hoodcomputing.natpmp.MapRequestMessage;
import com.hoodcomputing.natpmp.NatPmpDevice;
import com.hoodcomputing.natpmp.NatPmpException;

public class PortMapper implements Runnable {
	JSONAPI api;
	Logger logger;
	final static short WAIT_TIME = 1;
	final static long MAPPING_DURATION = 3600;
	
	public PortMapper(JSONAPI api) {
		this.api = api;
		logger = api.outLog;
		
		Thread t = new Thread(this);
		t.run();
	}
	
	public void run() {
		mapPorts();
	}
	
	public void mapPorts() {
		boolean cont = true;
		while(cont) {
			try {
				jnat_pmp(api.port);
				jnat_pmp(api.port + 1);
				jnat_pmp(api.port + 2);
			}
			catch(NatPmpException e) {
				try {
					weupnp(api.port);
					weupnp(api.port + 1);
					weupnp(api.port + 2);
				}
				catch(Exception f) {
					logger.warning("Couldn't map ports using NAT-PNP or weupnp.");
					cont = false;
				}
			}
			try {
				Thread.sleep(MAPPING_DURATION * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void jnat_pmp(int port) throws NatPmpException {
        // To find the device, simply construct the class. An exception is
        // thrown if the device cannot be located or if the network is not
        // RFC1918.
        // When the device is constructed, you have to tell it whether you
        // want it to automatically shutdown with the JVM or if you'll take
        // the responsibility of shutting it down yourself. Refer to the
        // constructor documentation for the details. In this case, we'll
        // let it shut down with the JVM.
		NatPmpDevice pmpDevice = new NatPmpDevice(true);

        // The next step is always to determine the external address of
        // the device. This is done by constructing the request message
        // and enqueueing it.
        ExternalAddressRequestMessage extAddr = new ExternalAddressRequestMessage(null);
        pmpDevice.enqueueMessage(extAddr);

        // Now, we can set up a port mapping. Refer to the javadoc for
        // the parameter values. This message sets up a TCP redirect from
        // a gateway-selected available external port to the local port
        // 5000. The lifetime is 120 seconds. In implementation, you would
        // want to consider having a longer lifetime and periodicly sending
        // a MapRequestMessage to prevent it from expiring.
        MapRequestMessage map = new MapRequestMessage(true, port, port, MAPPING_DURATION, null);
        pmpDevice.enqueueMessage(map);
        pmpDevice.waitUntilQueueEmpty();

        // All set!
        
        // Please refer to the javadoc if you run into trouble. As always,
        // contact a developer on the SourceForge project or post in the
        // forums if you have questions.
	}
	
	public void weupnp(int SAMPLE_PORT) throws SocketException, UnknownHostException, IOException, SAXException, ParserConfigurationException, InterruptedException {
		    GatewayDiscover discover = new GatewayDiscover();
		    discover.discover();
		    GatewayDevice d = discover.getValidGateway();

		    if (null != d) {
//		      logger.log(Level.INFO, "Gateway device found.\n{0} ({1})", new Object[] { d.getModelName(), d.getModelDescription() });
		    } else {
//		      logger.info("No valid gateway device found.");
		      return;
		    }

		    InetAddress localAddress = d.getLocalAddress();
//		    logger.log(Level.INFO, "Using local address: {0}", localAddress);
//		    String externalIPAddress = d.getExternalIPAddress();
//		    logger.log(Level.INFO, "External address: {0}", externalIPAddress);
		    PortMappingEntry portMapping = new PortMappingEntry();

//		    logger.log(Level.INFO, "Attempting to map port {0}", Integer.valueOf(SAMPLE_PORT));
//		    logger.log(Level.INFO, "Querying device to see if mapping for port {0} already exists", Integer.valueOf(SAMPLE_PORT));

		    if (!d.getSpecificPortMappingEntry(SAMPLE_PORT, "TCP", portMapping)) {
//		      logger.info("Sending port mapping request");

		      if (d.addPortMapping(SAMPLE_PORT, SAMPLE_PORT, localAddress.getHostAddress(), "TCP", "JSONAPI")) {
//		        logger.log(Level.INFO, "Mapping succesful: waiting {0} seconds before removing mapping.", Short.valueOf(WAIT_TIME));
		      } else {
//		        logger.info("Port mapping addition failed");
		      }
		    }
		    else {
//		      logger.info("Port was already mapped.");
		    }
	}
}
