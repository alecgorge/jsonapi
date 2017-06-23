package com.alecgorge.minecraft.jsonapi.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

/**
 * This class represents a stream. You most likely never have to overload any of
 * these methods.
 * 
 * @author alecgorge
 */
public abstract class JSONAPIStream {
	protected List<JSONAPIStreamListener> listeners = Collections.synchronizedList(new ArrayList<JSONAPIStreamListener>());
	protected LinkedBlockingQueue<JSONAPIStreamMessage> messages = new LinkedBlockingQueue<JSONAPIStreamMessage>();
	protected List<JSONAPIStreamMessage> last50 = Collections.synchronizedList(new ArrayList<JSONAPIStreamMessage>(150));

	protected String name;

	public JSONAPIStream(String name) {
		this.name = name;
	}

	public JSONAPIStream() {
		Logger.getLogger("Minecraft").warning("You just created a JSONAPIStream without a name! You shouldn't be doing that! Location:");
		Thread.dumpStack();
		Logger.getLogger("Minecraft").warning("End JSONAPIStream invalid constructor location.");
	}

	public String getName() {
		return name;
	}

	/**
	 * Register to have messages pushed to the listener.
	 * 
	 * @param l
	 *            The listener to push to.
	 * @param feedOld
	 *            Do you want to receive up to 150 previous messages immediately
	 *            after registering?
	 */
	public void registerListener(JSONAPIStreamListener l, boolean feedOld) {
		if (feedOld && JSONAPI.instance.allowSendingOldStreamMessages) {
			synchronized (last50) {
				for (JSONAPIStreamMessage m : last50) {
					l.onMessage(m, this);
				}				
			}
		}

		listeners.add(l);
	}

	/**
	 * Returns up to the last 150 messages in syncronized ArrayList.
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
	
	private int drainTask = -1;

	/**
	 * Push out a message to all subscribers.
	 * 
	 * @param m
	 *            A message to be pushed to all subscribers.
	 */
	public void pushMessage(final JSONAPIStreamMessage m) {
		final JSONAPIStream that = this;
		messages.offer(m);
		last50.add(m);

		if (last50.size() > 150) {
			last50.remove(0);
		}
		
		if(drainTask > 0) {
			JSONAPI.instance.getServer().getScheduler().cancelTask(drainTask);
		}
		
		(new BukkitRunnable() {

			@Override
			public void run() {
				ArrayList<JSONAPIStreamMessage> stack = new ArrayList<JSONAPIStreamMessage>();
				messages.drainTo(stack);

				synchronized (listeners) {
					for (JSONAPIStreamListener l : listeners) {
						for (JSONAPIStreamMessage mm : stack) {
							l.onMessage(mm, that);
						}
					}
				}
			}
		}).runTaskLaterAsynchronously(JSONAPI.instance, 5);
	}
}
