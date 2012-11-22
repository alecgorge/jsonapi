/*
 *              weupnp - Trivial upnp java library
 *
 * Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 *
 */
package org.wetorrent.upnp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Handles the discovery of GatewayDevices, via the {@link discover()} method.
 */
public class GatewayDiscover {

    /**
     * The SSDP port
     */
    public static final int PORT = 1900;
    /**
     * The broadcast address to use when trying to contact UPnP devices
     */
    public static final String IP = "239.255.255.250";
    /**
     * The timeout to set for the initial broadcast request
     */
    private static final int TIMEOUT = 3000;
    /**
     * A map of the GatewayDevices discovered so far.
     * The assumption is that a machine is connected to up to a Gateway Device
     * per InetAddress
     */
    private Map<InetAddress, GatewayDevice> devices = new HashMap<InetAddress, GatewayDevice>();

    /**
     * The default constructor
     */
    public GatewayDiscover() {
    }

    /**
     * Discovers Gateway Devices on the network(s) the executing machine is
     * connected to.
     * 
     * The host may be connected to different networks via different network
     * interfaces.
     * Assumes that each network interface has a different InetAddress and
     * returns a map associating every GatewayDevice (responding to a broadcast
     * discovery message) with the InetAddress it is connected to.
     * 
     * @return a map containing a GatewayDevice per InetAddress
     * @throws SocketException
     * @throws UnknownHostException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Map<InetAddress, GatewayDevice> discover() throws SocketException, UnknownHostException, IOException, SAXException, ParserConfigurationException {

        DatagramSocket ssdp = new DatagramSocket();
        int port = ssdp.getLocalPort();

        final String searchMessage = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: " + IP + ":" + port + "\r\n" +
                "ST: " + "urn:schemas-upnp-org:device:InternetGatewayDevice:1" + "\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "\r\n";
        try {
            byte[] searchMessageBytes = searchMessage.getBytes();
            DatagramPacket ssdpDiscoverPacket = new DatagramPacket(searchMessageBytes, searchMessageBytes.length);
            ssdpDiscoverPacket.setAddress(InetAddress.getByName(IP));
            ssdpDiscoverPacket.setPort(PORT);

            ssdp.send(ssdpDiscoverPacket);
            ssdp.setSoTimeout(TIMEOUT);

            boolean waitingPacket = true;

            while (waitingPacket) {
                DatagramPacket receivePacket = new DatagramPacket(new byte[1536], 1536);
                try {
                    ssdp.receive(receivePacket);
                    byte[] receivedData = new byte[receivePacket.getLength()];
                    System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());

                    // TODO: devices should be a map, and receivePacket.address should be the key ;)
                    GatewayDevice d = parseMSearchReplay(receivedData);

                    /* Get local address as it appears to the Gateway */
                    InetAddress localAddress = getOutboundAddress(receivePacket.getSocketAddress());

                    d.setLocalAddress(localAddress);
                    devices.put(localAddress, d);
                } catch (SocketTimeoutException ste) {
                    waitingPacket = false;
                }
            }

            for (GatewayDevice device : devices.values()) {
                try {
                    device.loadDescription();
                } catch (Exception e) {
                }
            }
        } finally {
            ssdp.close();
        }
        return devices;
    }

    /**
     * Parses the reply from UPnP devices
     * @param reply the raw bytes received as a reply
     * @return the representation of a GatewayDevice
     */
    private GatewayDevice parseMSearchReplay(byte[] reply) {

        GatewayDevice device = new GatewayDevice();

        // XXX it would be better to pay attention to the encoding
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(reply)));
        String line = null;
        try {
            line = br.readLine().trim();
        } catch (IOException ex) {
        }

        while (line != null && line.trim().length() > 0) {

            if (line.startsWith("HTTP/1.")) {
            } else {
                String key = line.substring(0, line.indexOf(':'));
                String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;

                key = key.trim();
                if (value != null) {
                    value = value.trim();
                }

                if (key.compareToIgnoreCase("location") == 0) {
                    device.setLocation(value);
                } else if (key.compareToIgnoreCase("st") == 0) {
                    device.setSt(value);
                }
            }
            try {
                line = br.readLine().trim();
            } catch (IOException ex) {
            }
        }

        return device;
    }

    /**
     * Gets the valid gateway
     * @return the first GatewayDevice which is connected to the network, or
     * null if nost present
     */
    public GatewayDevice getValidGateway() {

        for (GatewayDevice device : devices.values()) {
            try {
                if (device.isConnected()) {
                    return device;
                }
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * Gets the (local) address that can be used to reach this host machine
     * from the remote party identified by <tt>remoteAddress</tt>.
     *
     * @param remoteAddress the address of the remote party.
     * @return the address that should be used to contact the local host from
     *      <tt>remoteAddress</tt>
     * @throws SocketException on network failure
     */
    private InetAddress getOutboundAddress(SocketAddress remoteAddress) throws SocketException {
        DatagramSocket sock = new DatagramSocket();
        // connect is needed to bind the socket and retrieve the local address
        // later (it would return 0.0.0.0 otherwise)
        sock.connect(remoteAddress);

        InetAddress localAddress = sock.getLocalAddress();

        sock.disconnect();
        sock = null;

        return localAddress;
    }
}
