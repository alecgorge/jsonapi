package org.json.simpleForBukkit.parser;

import java.util.List;
import java.util.Map;

/**
 * Container factory for creating containers for JSON object and JSON array.
 * 
 * @see org.json.simpleForBukkit.parser.JSONParser#parse(java.io.Reader, ContainerFactory)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface ContainerFactory {
	/**
	 * @return A Map instance to store JSON object, or null if you want to use org.json.simpleForBukkit.JSONObject.
	 */
	Map<Object, Object> createObjectContainer();
	
	/**
	 * @return A List instance to store JSON array, or null if you want to use org.json.simpleForBukkit.JSONArray. 
	 */
	List<Object> creatArrayContainer();
}
