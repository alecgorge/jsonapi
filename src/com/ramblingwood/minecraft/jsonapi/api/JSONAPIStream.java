package com.ramblingwood.minecraft.jsonapi.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;

/**
 * This class represents a stream. You most likely never have to overload any of these methods.
 * 
 * @author alecgorge
 */
public abstract class JSONAPIStream {
	protected List<JSONAPIStreamListener> listeners = Collections.synchronizedList(new ArrayList<JSONAPIStreamListener>());
	protected LinkedBlockingQueue<JSONAPIStreamMessage> messages = new LinkedBlockingQueue<JSONAPIStreamMessage>();
	protected List<JSONAPIStreamMessage> last50 = Collections.synchronizedList(new ArrayList<JSONAPIStreamMessage>(50));
	
	/**
	 * Register to have messages pushed to the listener.
	 * 
	 * @param l The listener to push to.
	 * @param feedOld Do you want to receive up to 50 previous messages immediately after registering?
	 */
	public void registerListener(JSONAPIStreamListener l, boolean feedOld) {
		if(feedOld) {
			for(JSONAPIStreamMessage m : last50) {
				l.onMessage(m, this);
			}
		}
		
		listeners.add(l);
	}
	
	/**
	 * Returns up to the last 50 messages in syncronized ArrayList.
	 * 
	 * @return
	 */
	public List<JSONAPIStreamMessage> getStack() {
		return last50;
	}
	
	
	/**
	 * Undo the effects of registerListener.
	 * 
	 * @param l
	 */
	public void deregisterListener(JSONAPIStreamListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Alias for pushMessage
	 * 
	 * @param m
	 */
	public void addMessage(JSONAPIStreamMessage m) {
		pushMessage(m);
	}
	
	/**
	 * Push out a message to all subscribers.
	 * 
	 * @param m A message to be pushed to all subscribers.
	 */
	public void pushMessage(final JSONAPIStreamMessage m) {
		final JSONAPIStream that = this;
		JSONAPI.instance.getServer().getScheduler().scheduleSyncDelayedTask(JSONAPI.instance, new Runnable() {
			public void run () {
				try {
					messages.put(m);
					last50.add(m);
					
					if(last50.size() > 50) {
						last50.remove(0);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<JSONAPIStreamMessage> stack = new ArrayList<JSONAPIStreamMessage>();
				messages.drainTo(stack);
				
				synchronized (listeners) {
					for(JSONAPIStreamListener l : listeners) {
						for(JSONAPIStreamMessage mm : stack) {
							l.onMessage(mm, that);
						}			
					}
				}
			}
		});
	}
}
