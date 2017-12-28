package com.alvin.wechat.authtool.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonDB {
	private static final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.enable(SerializationFeature.INDENT_OUTPUT);
	private static final Map<String, Object> dataMap = new ConcurrentHashMap<String, Object>();
	private static String jsonFilePath;
	private static JsonDB jsonDB = null;
	private static final BlockingQueue<String> eventQueue = new LinkedBlockingQueue<String>();
	private static final String WRITE_EVENT = "WRITE";
	private static volatile Thread writerThread;
	private static AtomicBoolean inited = new AtomicBoolean(false);

	public static void initDB(String path)
			throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		if (!inited.compareAndSet(false, true)) {
			return;
		}
		jsonDB = new JsonDB(path);
		if (writerThread == null) {
			synchronized (JsonDB.class) {
				if (writerThread == null) {
					// Event-based write, thread safe may lost data
					writerThread = new Thread() {
						@Override
						public void run() {
							while (true) {
								try {
									String event = eventQueue.take();
									if (WRITE_EVENT.equals(event)) {
										FileWriter fw = new FileWriter(new File(jsonFilePath));
										fw.write(jsonDB.getDataMapJson());
										fw.close();
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					};
					writerThread.setDaemon(true);
					writerThread.start();
				}
			}
		}

	}

	private JsonDB(String jsonFilePath)
			throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		this.jsonFilePath = jsonFilePath;
		initDataMap(jsonFilePath);
	}

	public static Set<String> keySet() {
		return dataMap.keySet();
	}

	public static Set<Map.Entry<String, Object>> entrySet() {
		return dataMap.entrySet();
	}

	public static <T> T get(String key) {
		return (T) dataMap.get(key);
	}

	public static Object remove(String key) {
		Object removedObj = dataMap.remove(key);
		if (removedObj != null) {
			try {
				eventQueue.put(WRITE_EVENT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return removedObj;
	}

	public static void put(String key, Object value) {
		dataMap.put(key, value);
		try {
			eventQueue.put(WRITE_EVENT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void initDataMap(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String jsonStr = new String(data, "utf-8");
		if (jsonStr == null || jsonStr.length() == 0) {
			return;
		}
		JSONObject jo = new JSONObject(jsonStr);
		try {
			for (String key : jo.keySet()) {
				Object o = jo.get(key);
				if (o instanceof JSONObject) {
					String type = ((JSONObject) o).getString("__type__");
					JSONObject joo = (JSONObject) o;
					if (joo.has("__array__")) {
						JSONArray ja = joo.getJSONArray("__array__");
						List l = new ArrayList();
						Class typeCls = Class.forName(type);
						if (typeCls.equals(String.class)) {
							for (int i = 0; i < ja.length(); i++) {
								l.add(ja.getString(i));
							}
						} else if (typeCls.equals(Integer.class)) {
							for (int i = 0; i < ja.length(); i++) {
								l.add(ja.getInt(i));
							}
						} else if (typeCls.equals(Long.class)) {
							for (int i = 0; i < ja.length(); i++) {
								l.add(ja.getLong(i));
							}
						} else {
							for (int i = 0; i < ja.length(); i++) {
								l.add(mapper.readValue(ja.getString(i), typeCls));
							}
						}
						dataMap.put(key, l);
					} else {
						dataMap.put(key, mapper.readValue(o.toString(), Class.forName(type)));
					}
				} else {
					dataMap.put(key, o);
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getDataMapJson() {
		JSONObject jo = new JSONObject();
		try {
			for (Map.Entry<String, Object> ent : dataMap.entrySet()) {
				if (ent.getValue() instanceof String || ent.getValue() instanceof Integer
						|| ent.getValue() instanceof Long) {
					jo.put(ent.getKey(), ent.getValue());
				} else if (ent.getValue() instanceof List) {
					JSONArray ja = new JSONArray();
					String className = null;
					for (Object ol : (List) ent.getValue()) {
						className = ol.getClass().getName();
						if (ol instanceof String || ol instanceof Integer || ol instanceof Long) {
							ja.put(ol);
						} else {
							ja.put(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ol));
						}
					}
					JSONObject joo = new JSONObject();
					joo.put("__type__", className);
					joo.put("__array__", ja);
					jo.put(ent.getKey(), joo);
				} else {
					JSONObject joo = new JSONObject(
							mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ent.getValue()));
					joo.put("__type__", ent.getValue().getClass().getName());
					jo.put(ent.getKey(), joo);
				}
			}

		} catch (JSONException | JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo.toString();
	}
}
