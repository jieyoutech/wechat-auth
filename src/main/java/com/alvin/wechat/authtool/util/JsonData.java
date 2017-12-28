package com.alvin.wechat.authtool.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonData {
	private static final Pattern ARRAY_PATTERN = Pattern.compile("(.+)\\[(\\d+)\\]");
	private static final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private final JSONObject json;

	public JsonData(String jsonString) {
		json = new JSONObject(jsonString);
	}

	public static JsonData parseJson(String jsonString) {
		try {
			return new JsonData(jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get Integer from JSON by path
	 * DO NOT use in loop to get data from deep level recursively
	 * 
	 * @param path
	 *            use / as splitter, [x] as JSONArray index. Example: tags[6]/id
	 * @return Integer data, if data is not Integer type, return null.
	 */
	public Integer getInteger(String path) {
		try{
			return (Integer)getObjectFromJSON(path);
		}catch(ClassCastException | JSONException e){
			return null;
		}
	}

	/**
	 * Get Integer from JSON by path
	 * DO NOT use in loop to get data from deep level recursively
	 * 
	 * @param path
	 *            : use / as splitter, [x] as JSONArray index. Example:
	 *            tags[6]/id
	 * @return String data, if data is not String type, return null.
	 */
	public String getString(String path) {
		try{
			return (String)getObjectFromJSON(path);
		}catch(ClassCastException | JSONException e){
			return null;
		}
	}

	/**
	 * DO NOT use in loop to get data from deep level recursively
	 * @param path
	 * @return
	 * @throws JSONException
	 */
	Object getObjectFromJSON(String path) throws JSONException {
		if (json == null || path == null || path.equals("/")) {
			return json;
		}
		Object currentObj = json;
		try {
			String[] sPath = path.split("/");
			for (String key : sPath) {
				Matcher arrayMatcher = ARRAY_PATTERN.matcher(key);
				if (arrayMatcher.find()) {
					currentObj = ((JSONObject) currentObj).getJSONArray(arrayMatcher.group(1)).get(Integer.parseInt(arrayMatcher.group(2)));
				} else {
					currentObj = ((JSONObject) currentObj).get(key);
				}
			}
		} catch (Exception e) {
			throw new JSONException("Cannot get object from JSON path " + path + ", error message:" + e.getMessage());
		}

		return currentObj;
	}

	public <T> T getBean(Class<T> clazz) {
		if (json != null) {
			try {
				return mapper.readValue(json.toString(), clazz);
			} catch (IOException e) {
				// eat it
			}
		}
		return null;
	}
}
