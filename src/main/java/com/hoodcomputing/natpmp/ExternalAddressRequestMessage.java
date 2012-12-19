package com.hoodcomputing.natpmp;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;




public class ExternalAddressRequestMessage extends Message
{
  private byte[] payload = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  private Inet4Address externalAddress;

  public ExternalAddressRequestMessage(MessageResponseInterface listener)
  {
    super(MessageType.ExternalAddress, listener);
  }

  public Inet4Address getExternalAddress()
    throws NatPmpException
  {
    return this.externalAddress;
  }

  void parseResponse(byte[] response) throws NatPmpException {
    try {
      this.externalAddress = ((Inet4Address)Inet4Address.getByAddress(Arrays.copyOfRange(response, 8, 12)));
    } catch (UnknownHostException ex) {
      throw new NatPmpException("Unable to parse external address.", ex);
    }
  }

  byte[] getRequestPayload() {
    return (byte[])(byte[])this.payload.clone();
  }

  byte getOpcode() {
    return 0;
  }
}

/* Location:           /Users/alecgorge/Downloads/jNAT-PMPlib 0.1 (1)/jNAT-PMPlib.jar
 * Qualified Name:     com.hoodcomputing.natpmp.ExternalAddressRequestMessage
 * JD-Core Version:    0.6.0
 */