package com.hoodcomputing.natpmp;

public abstract interface MessageResponseInterface
{
  public abstract void responseReceived(Message paramMessage);

  public abstract void noResponseReceived(Message paramMessage);

  public abstract void exceptionGenerated(Message paramMessage, NatPmpException paramNatPmpException);
}

/* Location:           /Users/alecgorge/Downloads/jNAT-PMPlib 0.1 (1)/jNAT-PMPlib.jar
 * Qualified Name:     com.hoodcomputing.natpmp.MessageResponseInterface
 * JD-Core Version:    0.6.0
 */