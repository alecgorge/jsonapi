package com.alecgorge.minecraft.jsonapi.packets;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class LostConnectionFilter implements Filter {
	Filter oldFilter;
	
	public LostConnectionFilter() {
		oldFilter = null;
	}
	
	public LostConnectionFilter(Filter old) {
		oldFilter = old;
	}
	
	@Override
	public boolean isLoggable(LogRecord record) {
		if(!(record == null || record.getMessage() == null)) {
			if(record.getMessage().equals("Connection reset"))
				return false;
			
			if(record.getMessage().matches("/[0-9.]+:[0-9]+ lost connection"))
				return false;			
		}
		
		if(oldFilter == null) return true;
		
		return oldFilter.isLoggable(record);
	}

}
