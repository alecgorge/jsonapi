package com.alecgorge.minecraft.jsonapi.packets;

public interface Lambda<T,Q> {
	public T execute(Q x);
}
