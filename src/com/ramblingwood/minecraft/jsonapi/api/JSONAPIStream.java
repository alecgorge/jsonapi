package com.ramblingwood.minecraft.jsonapi.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class JSONAPIStream {
	protected List<JSONAPIStreamListener> listeners = Collections.synchronizedList(new ArrayList<JSONAPIStreamListener>());
	protected LinkedBlockingQueue<JSONAPIStreamMessage> messages = new LinkedBlockingQueue<JSONAPIStreamMessage>();
	protected List<JSONAPIStreamMessage> last50 = Collections.synchronizedList(new ArrayList<JSONAPIStreamMessage>(50));
	
	public void registerListener(JSONAPIStreamListener l, boolean feedOld) {
		if(feedOld) {
			for(JSONAPIStreamMessage m : last50) {
				l.onMessage(m, this);
			}
		}
		
		listeners.add(l);
	}
	
	public List<JSONAPIStreamMessage> getStack() {
		return last50;
	}
	
	public void deregisterListener(JSONAPIStreamListener l) {
		listeners.remove(l);
	}
	
	public void addMessage(JSONAPIStreamMessage m) {
		pushMessage(m);
	}
	
	public void pushMessage(JSONAPIStreamMessage m) {		
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
					l.onMessage(mm, this);
				}			
			}
		}
	}
}
