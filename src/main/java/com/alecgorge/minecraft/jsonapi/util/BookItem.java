package com.alecgorge.minecraft.jsonapi.util;

import org.bukkit.inventory.meta.BookMeta;

public class BookItem {
	private BookMeta stack = null;
	
	public BookItem(org.bukkit.inventory.ItemStack item) {
		this.stack = (BookMeta) item.getItemMeta();
	}
	
	public String[] getPages() {
		return stack.getPages().toArray(new String[] {});
	}
	
	public String getAuthor() {
		return stack.getAuthor();
	}
	
	public String getTitle() {
		return stack.getTitle();
	}
}