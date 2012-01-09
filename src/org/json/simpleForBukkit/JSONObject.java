/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.json.simpleForBukkit;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject extends HashMap<Object, Object> implements Map<Object, Object>, JSONAware, JSONStreamAware{
	private static final long serialVersionUID = -503443796854799292L;

    /**
     * Encode a map into JSON text and write it to out.
     * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
     * 
     * @see org.json.simpleForBukkit.JSONValue#writeJSONString(Object, Writer)
     * 
     * @param map
     * @param out
     */
	@SuppressWarnings("unchecked")
	public static void writeJSONString(Map map, Writer out) throws IOException {
		if(map == null){
			out.write("null");
			return;
		}
		
		boolean first = true;
		Iterator<Object> iter = map.entrySet().iterator();
		
        out.write('{');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                out.write(',');
			Map.Entry<Object, Object> entry=(Map.Entry<Object, Object>)iter.next();
            out.write('\"');
            out.write(escape(String.valueOf(entry.getKey())));
            out.write('\"');
            out.write(':');
			JSONValue.writeJSONString(entry.getValue(), out);
		}
		out.write('}');
	}

	public void writeJSONString(Writer out) throws IOException{
		writeJSONString(this, out);
	}
	
	/**
	 * Convert a map to JSON text. The result is a JSON object. 
	 * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see org.json.simpleForBukkit.JSONValue#toJSONString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toJSONString(Map<Object, Object> map){
		if(map == null)
			return "null";
		
        StringBuffer sb = new StringBuffer();
        boolean first = true;
		Iterator<Entry<Object, Object>> iter=map.entrySet().iterator();
		
        sb.append('{');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                sb.append(',');
            
			Map.Entry<Object, Object> entry=(Map.Entry<Object, Object>)iter.next();
			toJSONString(String.valueOf(entry.getKey()),entry.getValue(), sb);
		}
        sb.append('}');
		return sb.toString();
	}
	
	public String toJSONString(){
		return toJSONString(this);
	}
	
	private static String toJSONString(String key,Object value, StringBuffer sb){
		sb.append('\"');
        if(key == null)
            sb.append("null");
        else
            JSONValue.escape(key, sb);
		sb.append('\"').append(':');
		
		sb.append(JSONValue.toJSONString(value));
		
		return sb.toString();
	}
	
	public String toString(){
		return toJSONString();
	}

	public static String toString(String key,Object value){
        StringBuffer sb = new StringBuffer();
		toJSONString(key, value, sb);
        return sb.toString();
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see org.json.simpleForBukkit.JSONValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		return JSONValue.escape(s);
	}
}
