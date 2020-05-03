package com.alecgorge.minecraft.jsonapi.dynamic;

import java.util.ArrayList;
import java.util.List;

public class ArgumentList extends ArrayList<Argument> implements List<Argument> {
	public Class<?>[] getTypes () {
		Class<?>[] c = new Class<?>[size()];
		ArrayList<Class<?>> cc = new ArrayList<Class<?>>();
		
		for(Argument a : this) {
			cc.add(a.getType());
		}
		
		cc.toArray(c);
		
		return c;
	}
	
	public Object[] getValues () {
		Object[] c = new Object[size()];
		ArrayList<Object> cc = new ArrayList<Object>();
		
		for(Argument a : this) {
			cc.add(a.getValue());
		}
		
		cc.toArray(c);
		
		return c;		
	}

	private static final long serialVersionUID = 1469704113645836925L;
}
