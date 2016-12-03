package fr.upem.papayadb.database;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class OldDocument {
	private final String filename;
	private HashMap<String, Object> data;
	private JsonReader reader;
	private BufferedWriter writer;
	
	private void fillDataArray(JsonArray obj, ArrayList<Object> array){
		obj.forEach(v ->{
			switch(v.getValueType()){
			case ARRAY:
				JsonArray jsonArray = (JsonArray)v;
				ArrayList<Object> innerArray = new ArrayList<Object>();
				fillDataArray(jsonArray, innerArray);
				array.add(innerArray);
				break;
			case OBJECT:
				JsonObject innerObj = (JsonObject)v;
				HashMap<String, Object> objData = new HashMap<String, Object>();
				fillDataObject(innerObj, objData);
				array.add(objData);
				break;
			case STRING:
				array.add(v.toString());
				break;
			case NUMBER:
				array.add(v.toString());
				break;
			case FALSE:
				array.add(Boolean.FALSE);
				break;
			case TRUE:
				array.add(Boolean.TRUE);
				break;
			case NULL:
				array.add(null);
				break;
			default:
				array.add(v);
				break;
		}
		});
	}
	
	private void fillDataObject(JsonObject obj, HashMap<String, Object> map){
		if (obj == null){
			return;
		}
		obj.forEach((k, v) -> {
			switch(v.getValueType()){
				case ARRAY:
					JsonArray jsonArray = (JsonArray)v;
					ArrayList<Object> array = new ArrayList<Object>();
					fillDataArray(jsonArray, array);
					map.put(k, array);
					break;
				case OBJECT:
					JsonObject innerObj = (JsonObject)v;
					HashMap<String, Object> objData = new HashMap<String, Object>();
					fillDataObject(innerObj, objData);
					map.put(k, objData);
					break;
				case STRING:
					map.put(k, v.toString());
					break;
				case NUMBER:
					map.put(k, v.toString());
					break;
				case FALSE:
					map.put(k, Boolean.FALSE);
					break;
				case TRUE:
					map.put(k, Boolean.TRUE);
					break;
				case NULL:
					map.put(k, null);
					break;
				default:
					map.put(k, v);
					break;
			}
		});
	}
	
	public OldDocument(String filename) throws FileNotFoundException{
		this.filename = filename;
		data = new HashMap<String, Object>();
		reader = Json.createReader(new FileInputStream(filename));
		JsonObject obj = reader.readObject();
		fillDataObject(obj, data);
	}
	
	public Object getData(String propertyName){
		return data.get(propertyName);
	}
	
	public String toString(){
		return data.toString();
	}
	
	public void update(String key, Object value){
		data.put(key, value);
	}
	
	public void update(HashMap<String, Object> changes){
		data.putAll(changes);
	}
	
	public void print(){
		StringBuilder sb = new StringBuilder();
		buildJson(sb, data, 0);
		System.out.println(sb);
	}
	
	@SuppressWarnings("unchecked")
	private void buildJson(StringBuilder builder, Object data, int level){
		if (data == null){
			builder.append("null");
		}
		if (data instanceof HashMap){
			builder.append("{\n");
			Entry<String, Object>[] entries = ((HashMap<String, Object>) data).entrySet().toArray((Entry<String, Object>[])new Entry[0]);
			for (int i = 0; i < entries.length - 1; i++){
				for (int tab = 0; tab < level; tab++){
					builder.append("\t");
				}
				builder.append(entries[i].getKey());
				builder.append(": ");
				buildJson(builder, entries[i].getValue(), level + 1);
				builder.append(",\n");
			}
			for (int tab = 0; tab < level; tab++){
				builder.append("\t");
			}
			builder.append(entries[entries.length - 1].getKey());
			builder.append(": ");
			buildJson(builder, entries[entries.length - 1].getValue(), level + 1);
			builder.append("\n");
			builder.append("}\n");
		}
		else if (data instanceof List){
			builder.append("[\n");
			int size = ((List<Object>) data).size();
			Object[] objs = ((List<Object>) data).toArray();
			for (int i = 0; i < size - 1; i++){
				for (int tab = 0; tab < level; tab++){
					builder.append("\t");
				}
				buildJson(builder, objs[i], level + 1);
				builder.append(",\n");
			}
			for (int tab = 0; tab < level; tab++){
				builder.append("\t");
			}
			buildJson(builder, objs[size - 1], level + 1);
			builder.append("\n");
			builder.append("]\n");
		}
		else{
			builder.append(data);
		}
	}
	
	public void applyChanges() throws IOException{
		if (writer != null){
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
		}
		StringBuilder builder = new StringBuilder();
		buildJson(builder, data, 1);
		writer.append(builder);
	}
}
