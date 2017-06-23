package com.alecgorge.minecraft.jsonapi.util;

import java.util.ArrayList;

public class FixedSizeArrayList<T> extends ArrayList<T> {
	private static final long serialVersionUID = -1129164009717580733L;
	private int limit;

	public FixedSizeArrayList(int limit) {
		this.limit = limit;
	}

	@Override
	public boolean add(T item) {
		trim(0);
		return super.add(item);
	}
	
	@Override
	public void add(int index, T item) {
		trim();
		super.add(index, item);
	}
	
	private void trim() {
		trim(limit - 1);
	}
	
	private void trim(int index) {
		if(size() >= limit) {
			super.remove(0);
		}
	}
}
