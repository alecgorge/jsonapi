package com.ramblingwood.minecraft.jsonapi.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JSONAPIStream {
	protected List<JSONAPIStreamMessage> stack = Collections.synchronizedList(new ArrayList<JSONAPIStreamMessage>());
	
	public List<JSONAPIStreamMessage> getStack() {
		return this.stack;
	}
}
