package com.hoodcomputing.natpmp;

import java.net.Inet4Address;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

class MessageQueue
  implements Runnable
{
  private Inet4Address gatewayIP;
  private Thread thread;
  private LinkedList<Message> queue;
  private final Object queueLock = new Object();
  private final Object messageLock = new Object();
  private boolean shutdown = false;
  private final Object shutdownLock = new Object();

  private MessageQueue(Inet4Address gatewayIP)
  {
    this.gatewayIP = gatewayIP;

    this.thread = new Thread(this, "MessageQueue");
    this.thread.setDaemon(false);

    this.queue = new LinkedList();
  }

  static MessageQueue createMessageQueue(Inet4Address gatewayIP)
  {
    MessageQueue messageQueue = new MessageQueue(gatewayIP);
    messageQueue.thread.start();

    while (messageQueue.thread.getState() != Thread.State.TIMED_WAITING) {
      Thread.yield();
    }

    return messageQueue;
  }

  void enqueueMessage(Message message)
  {
    synchronized (this.queueLock) {
      this.queue.add(message);
      this.queueLock.notify();
    }
  }

  void clearQueue()
  {
    synchronized (this.queueLock) {
      this.queue.clear();
      this.queueLock.notify();
    }
  }

  void shutdown()
  {
    synchronized (this.shutdownLock) {
      this.shutdown = true;
    }

    clearQueue();
    while (true)
    {
      if (this.thread.isAlive())
        try {
          Thread.sleep(25L);
        } catch (InterruptedException ex) {
          Logger.getLogger(MessageQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  }

  boolean isShutdown()
  {
    synchronized (this.shutdownLock) {
      return this.shutdown;
    }
  }

  void waitUntilQueueEmpty()
  {
    int size = 0;
    synchronized (this.messageLock) {
      size = this.queue.size();

      while (size > 0)
      {
        try
        {
          this.messageLock.wait(250L);
        } catch (InterruptedException ex) {
          Logger.getLogger(MessageQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
        size = this.queue.size();
      }
    }
  }

  public void run()
  {
    while (!this.shutdown)
      try
      {
        Message message = null;
        synchronized (this.messageLock) {
          synchronized (this.queueLock)
          {
            while ((message == null) && (!this.shutdown)) {
              if (this.queue.size() > 0)
              {
                message = (Message)this.queue.pop(); continue;
              }

              this.queueLock.wait(250L);

              this.messageLock.wait(1L);
            }

          }

          if (this.shutdown)
          {
            continue;
          }

          message.sendMessage(this.gatewayIP);

          message.notifyListener();
        }
      }
      catch (InterruptedException ex)
      {
      }
  }
}

/* Location:           /Users/alecgorge/Downloads/jNAT-PMPlib 0.1 (1)/jNAT-PMPlib.jar
 * Qualified Name:     com.hoodcomputing.natpmp.MessageQueue
 * JD-Core Version:    0.6.0
 */