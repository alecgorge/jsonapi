package com.hoodcomputing.natpmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NatPmpDevice
{
  private boolean isShutdown = false;
  private Thread shutdownHookThread = null;
  private final Object shutdownLock = new Object();
  private MessageQueue messageQueue;

  public NatPmpDevice(boolean shutdownHookEnabled)
    throws NatPmpException
  {
    setShutdownHookEnabled(shutdownHookEnabled);

    Inet4Address gateway = getGatewayIP();

    if (gateway == null) {
      throw new NatPmpException("The network gateway cannot be located.");
    }

    if (!gateway.isSiteLocalAddress()) {
      throw new NatPmpException("The network gateway address is not RFC1918 compliant.");
    }

    this.messageQueue = MessageQueue.createMessageQueue(gateway);
  }

  public void enqueueMessage(Message message)
  {
    this.messageQueue.enqueueMessage(message);
  }

  public void clearQueue()
  {
    this.messageQueue.clearQueue();
  }

  public void waitUntilQueueEmpty()
  {
    this.messageQueue.waitUntilQueueEmpty();
  }

  public final boolean isShutdownHookEnabled()
  {
    synchronized (this.shutdownLock) {
      return this.shutdownHookThread != null;
    }
  }

  public final void setShutdownHookEnabled(boolean enabled)
  {
    synchronized (this.shutdownLock) {
      if (isShutdownHookEnabled())
      {
        if (!enabled)
        {
          Runtime.getRuntime().removeShutdownHook(this.shutdownHookThread);
          this.shutdownHookThread = null;
        }

      }
      else if (enabled)
      {
        Thread t = new Thread(new Runnable() {
          public void run() {
            NatPmpDevice.this.shutdown();
          }
        }
        , "NatPmpDevice:ShutdownHook");

        Runtime.getRuntime().addShutdownHook(t);
        this.shutdownHookThread = t;
      }
    }
  }

  public void shutdown()
  {
    synchronized (this.shutdownLock)
    {
      setShutdownHookEnabled(false);

      this.messageQueue.shutdown();

      this.isShutdown = true;
    }
  }

  public Thread shutdownAsync(boolean daemon)
  {
    synchronized (this.shutdownLock)
    {
      setShutdownHookEnabled(false);

      Thread t = new Thread(new Runnable() {
        public void run() {
          NatPmpDevice.this.shutdown();
        }
      }
      , "NatPmpDevice:ShutdownAsync");

      t.setDaemon(daemon);
      t.start();

      return t;
    }
  }

  public boolean isShutdown()
  {
    synchronized (this.shutdownLock) {
      return this.isShutdown;
    }
  }

  static Inet4Address getGatewayIP()
  {
    String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    String exIP = "(?:" + _255 + "\\.){3}" + _255;

    String osName = System.getProperty("os.name");
    Pattern gatewayPattern;
    if (osName.equals("Mac OS X"))
    {
      gatewayPattern = Pattern.compile("^\\s*(?:default\\s*)(" + exIP + ").*");
    }
    else
    {
      if (osName.startsWith("Windows"))
        gatewayPattern = Pattern.compile("^\\s*(?:0\\.0\\.0\\.0\\s*){1,2}(" + exIP + ").*");
      else
        return null;
    }
    try
    {
      Process proc = Runtime.getRuntime().exec("netstat -rn");

      InputStream inputstream = proc.getInputStream();
      InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
      BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
      String line;
      while ((line = bufferedreader.readLine()) != null) {
        Matcher m = gatewayPattern.matcher(line);

        if (m.matches())
        {
          return (Inet4Address)Inet4Address.getByName(m.group(1));
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(NatPmpDevice.class.getName()).log(Level.SEVERE, "NatPmpDevice: Unable to determine gateway.", ex);
    }

    return null;
  }
}

/* Location:           /Users/alecgorge/Downloads/jNAT-PMPlib 0.1 (1)/jNAT-PMPlib.jar
 * Qualified Name:     com.hoodcomputing.natpmp.NatPmpDevice
 * JD-Core Version:    0.6.0
 */