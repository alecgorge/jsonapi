package com.hoodcomputing.natpmp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public abstract class Message
{
  private MessageType type;
  private DatagramSocket socket;
  private boolean responseParsed;
  private byte[] response;
  private NatPmpException responseException;
  private MessageResponseInterface listener;
  private ResultCode resultCode;
  private Long secondsSinceEpoch;

  Message(MessageType type, MessageResponseInterface listener)
  {
    this.type = type;
    this.listener = listener;

    if (type == null)
      throw new NullPointerException("MessageType must not be null.");
  }

  abstract byte[] getRequestPayload();

  abstract byte getOpcode();

  abstract void parseResponse(byte[] paramArrayOfByte)
    throws Throwable;

  public NatPmpException getResponseException()
  {
    return this.responseException;
  }

  public ResultCode getResultCode()
    throws NatPmpException
  {
    return this.resultCode;
  }

  public Long getSecondsSinceEpoch()
    throws NatPmpException
  {
    return this.secondsSinceEpoch;
  }

  private void setResponseException(NatPmpException responseException)
  {
    this.responseException = responseException;
  }

  void notifyListener()
  {
    if (this.response == null) {
      if (this.listener != null) {
        this.listener.noResponseReceived(this);
      }
      return;
    }

    try
    {
      internalNotifyListener();
    } catch (NatPmpException ex) {
      setResponseException(ex);
    }

    if (getResponseException() != null) {
      if (this.listener != null) {
        this.listener.exceptionGenerated(this, getResponseException());
      }

    }
    else if (this.listener != null)
      this.listener.responseReceived(this);
  }

  private void internalNotifyListener()
    throws NatPmpException
  {
    this.secondsSinceEpoch = Long.valueOf(longFromByteArray(this.response, 4));
    switch (intFromByteArray(this.response, 2)) {
    case 0:
      this.resultCode = ResultCode.Success;
      break;
    case 1:
      this.resultCode = ResultCode.UnsupportedVersion;
      break;
    case 2:
      this.resultCode = ResultCode.NotAuthorizedRefused;
      break;
    case 3:
      this.resultCode = ResultCode.NetworkFailure;
      break;
    case 4:
      this.resultCode = ResultCode.OutOfResources;
      break;
    case 5:
      this.resultCode = ResultCode.UnsupportedOpcode;
      break;
    default:
      throw new NatPmpException("Unsupported Result Code: " + intFromByteArray(this.response, 2));
    }

    if (this.resultCode != ResultCode.Success) {
      throw new NatPmpException("Message was not successful. The returned message code was " + this.resultCode.toString() + ".");
    }

    if (this.response[1] != getOpcode() + -128) {
      throw new NatPmpException("Incorrect opcode received from gateway.");
    }

    try
    {
      parseResponse(this.response);
    } catch (Throwable ex) {
      if ((ex instanceof NatPmpException)) {
        throw ((NatPmpException)ex);
      }
      throw new NatPmpException("Exception encountered during parsing of response.", ex);
    }
  }

  MessageType getMessageType()
  {
    return this.type;
  }

  static final long longFromByteArray(byte[] src, int offset)
  {
    return ((src[offset] & 0xFF) << 24) + ((src[(offset + 1)] & 0xFF) << 16) + ((src[(offset + 2)] & 0xFF) << 8) + (src[(offset + 3)] & 0xFF);
  }

  static final void longToByteArray(long value, byte[] array, int offset)
  {
    array[offset] = (byte)(int)(value >>> 24);
    array[(offset + 1)] = (byte)(int)(value >>> 16);
    array[(offset + 2)] = (byte)(int)(value >>> 8);
    array[(offset + 3)] = (byte)(int)value;
  }

  static final int intFromByteArray(byte[] src, int offset)
  {
    return ((src[offset] & 0xFF) << 8) + (src[(offset + 1)] & 0xFF);
  }

  static final void intToByteArray(int value, byte[] array, int offset)
  {
    array[offset] = (byte)(value >>> 8);
    array[(offset + 1)] = (byte)value;
  }

  synchronized void sendMessage(Inet4Address destination)
  {
    try
    {
      sendMessageInternal(destination);
    } catch (NatPmpException ex) {
      setResponseException(ex);
    }
  }

  private void sendMessageInternal(Inet4Address destination) throws NatPmpException
  {
    if (this.socket != null) {
      throw new NatPmpException("Message is already being sent.");
    }

    try
    {
      this.socket = new DatagramSocket();
      this.socket.connect(destination, 5351);
      this.socket.setSoTimeout(250);
    }
    catch (IOException ex) {
      if (this.socket != null) {
        this.socket.close();
        this.socket = null;
      }

      throw new NatPmpException("Exception during socket setup.", ex);
    }

    sendMessageInternal();

    this.socket.close();
  }

  private void sendMessageInternal()
    throws NatPmpException
  {
    for (int attempts = 9; attempts > 0; attempts--)
    {
      try {
        byte[] payload = getRequestPayload();
        payload[0] = 0;
        payload[1] = getOpcode();
        DatagramPacket packet = new DatagramPacket(payload, payload.length, this.socket.getRemoteSocketAddress());
        this.socket.send(packet);
      } catch (PortUnreachableException ex) {
        throw new NatPmpException("The gateway is unreachable.");
      } catch (IOException ex) {
        throw new NatPmpException("Exception while sending packet.", ex);
      }

      try
      {
        byte[] localResponse = new byte[16];
        DatagramPacket packet = new DatagramPacket(localResponse, 0, 16);
        this.socket.receive(packet);

        if (packet.getLength() > 0) {
          this.response = localResponse;
          return;
        }
      } catch (SocketTimeoutException ex) {
      }
      catch (PortUnreachableException ex) {
        throw new NatPmpException("The gateway is unreachable.");
      } catch (IOException ex) {
        throw new NatPmpException("Exception while waiting for packet to be received.", ex);
      }

      try
      {
        this.socket.setSoTimeout(this.socket.getSoTimeout() * 2);
      } catch (SocketException ex) {
        throw new NatPmpException("Exception while increasing socket timeout time.", ex);
      }
    }
  }
}

/* Location:           /Users/alecgorge/Downloads/jNAT-PMPlib 0.1 (1)/jNAT-PMPlib.jar
 * Qualified Name:     com.hoodcomputing.natpmp.Message
 * JD-Core Version:    0.6.0
 */