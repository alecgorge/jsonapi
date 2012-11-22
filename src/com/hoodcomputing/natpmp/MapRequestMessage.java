package com.hoodcomputing.natpmp;

public class MapRequestMessage extends Message
{
  private int internalPort;
  private int requestedExternalPort;
  private Integer externalPort;
  private long requestedPortMappingLifetime;
  private Long portMappingLifetime;

  public MapRequestMessage(boolean isTCP, int internalPort, int requestedExternalPort, long requestedPortMappingLifetime, MessageResponseInterface listener)
  {
    super(isTCP ? MessageType.MapTCP : MessageType.MapUDP, listener);

    this.internalPort = internalPort;
    this.requestedExternalPort = requestedExternalPort;
    this.requestedPortMappingLifetime = requestedPortMappingLifetime;
  }

  byte[] getRequestPayload() {
    byte[] request = new byte[12];
    request[2] = 0;
    request[3] = 0;
    intToByteArray(getInternalPort(), request, 4);
    intToByteArray(getRequestedExternalPort(), request, 6);
    longToByteArray(getRequestedPortMappingLifetime(), request, 8);

    return request;
  }

  void parseResponse(byte[] response) throws NatPmpException {
    int returnedInternalPort = intFromByteArray(response, 8);
    if (returnedInternalPort != this.internalPort) {
      throw new NatPmpException("The internal port returned from the gateway was not the same as the one sent.");
    }
    this.internalPort = returnedInternalPort;

    this.externalPort = Integer.valueOf(intFromByteArray(response, 10));
    this.portMappingLifetime = Long.valueOf(longFromByteArray(response, 12));
  }

  byte getOpcode() {
    return (byte) (getMessageType() == MessageType.MapUDP ? 1 : 2);
  }

  public int getInternalPort()
  {
    return this.internalPort;
  }

  public int getRequestedExternalPort()
  {
    return this.requestedExternalPort;
  }

  public Integer getExternalPort()
    throws NatPmpException
  {
    return this.externalPort;
  }

  public long getRequestedPortMappingLifetime()
  {
    return this.requestedPortMappingLifetime;
  }

  public Long getPortMappingLifetime()
    throws NatPmpException
  {
    return this.portMappingLifetime;
  }
}

/* Location:           /Users/alecgorge/Downloads/jNAT-PMPlib 0.1 (1)/jNAT-PMPlib.jar
 * Qualified Name:     com.hoodcomputing.natpmp.MapRequestMessage
 * JD-Core Version:    0.6.0
 */