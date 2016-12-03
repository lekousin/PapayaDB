package fr.upem.papayadb.database;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;

public class Database {
	private final FileChannel dbFileChannel;
	private long fileLength;
	private ArrayDeque<Pair<Integer, Document>> cache;
	private final int cacheCapacity;
	
	public Database(Path filepath, int cacheCapacity) throws IOException{
		dbFileChannel = FileChannel.open(filepath, StandardOpenOption.CREATE
				, StandardOpenOption.READ, StandardOpenOption.WRITE);
		cache = new ArrayDeque<>();
		fileLength = new File(filepath.toString()).length();
		this.cacheCapacity = cacheCapacity;
	}
	
	public Database(String filename) throws IOException{
		this(Paths.get(filename), 32);
	}
	
	private void cache(String path, Document doc) throws IOException{
		if (cache.size() == cacheCapacity){
			cache.removeFirst().getV2().close();
		}
		cache.addLast(new Pair<>(path.hashCode(), doc));
	}
	
	private Document getDocument(String path) throws IOException{
		Document doc = null;
		int hash = path.hashCode();
		for (Pair<Integer, Document> pair : cache){
			if (pair.getV1() == hash){
				doc = pair.getV2();
				break;
			}
		}
		if (doc == null){
			doc = Document.openDocument(path);
			cache(path, doc);
		}
		return doc;
	}
	
	private List<String> getDocumentPaths() throws IOException{
		List<String> paths = new ArrayList<String>();
		MappedByteBuffer charBuffer = dbFileChannel.map(MapMode.READ_WRITE, 0, fileLength);
		charBuffer.load();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fileLength; i++){
			char c = (char)charBuffer.get();
			if (c == '\n'){
				String[] args = sb.toString().split("[ \0]");
				if (args[1].trim().equals("0")){
					sb.delete(0, sb.length());
					continue;
				}
				paths.add(args[0].trim());
				sb.delete(0, sb.length());
				continue;
			}
			sb.append(c);
		}
		if (sb.length() > 0){
			String[] args = sb.toString().split("[ \0]");
			if (args[1].trim().equals("0")){
				return paths;
			}
			paths.add(args[0].trim());
		}
		return paths;
	}
	
	public List<Map<String, String>> select(JsonObject request) throws IOException{
		List<Map<String, String>> results = new ArrayList<>();
		List<String> paths = getDocumentPaths();
		for (String path : paths){
			Document doc = getDocument(path);
			Map<String, String> row = doc.select(request);
			if (row != null){
				results.add(row);
			}
		}
		return results;
	}
	
	public void insert(String filepath, JsonObject object) throws IOException{
		synchronized(dbFileChannel){
			Document doc = Document.createDocument(filepath, object);
			int length = filepath.length();
			MappedByteBuffer map = dbFileChannel.map(MapMode.READ_WRITE, fileLength, length + 2);
			map.put(filepath.getBytes());
			map.putChar(' ');
			map.putInt(1);
			map.force();
			cache(filepath, doc);
		}
	}
	
	public void delete(JsonObject request) throws Exception{
		synchronized(dbFileChannel){
			JsonArray docsJson = request.getJsonArray("documents");
			JsonObject where = request.getJsonObject("where");
			List<String> docs = new ArrayList<>();
			if (docsJson == null && where == null){
				throw new JsonException("Could not find documents nor where field");
			}
			docsJson.forEach(d -> {
				docs.add(d.toString().substring(1, d.toString().length() - 1));
			});
			MappedByteBuffer charBuffer = dbFileChannel.map(MapMode.READ_WRITE, 0, fileLength);
			charBuffer.load();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < fileLength; i++){
				char c = (char)charBuffer.get();
				if (c == '\n'){
					String[] args = sb.toString().split(" ");
					if (docs.contains(args[0].trim()) && args[1].trim().equals("1")){
						charBuffer.position(charBuffer.position() - 2).putChar('0').getChar();
					}
					sb.delete(0, sb.length());
				}
				else{
					sb.append(c);
				}
			}
			if (sb.length() > 0){
				String[] args = sb.toString().split(" ");
				if (docs.contains(args[0].trim()) && args[1].trim().equals("1")){
					charBuffer.position(charBuffer.position() - 2);
					charBuffer.putChar('0');
					charBuffer.getChar();
				}
			}
		}
	}
	
	public void update(JsonObject request){
		synchronized(dbFileChannel){
			List<OldDocument> toUpdate = new ArrayList<>();
			HashMap<String, Object> changes = new HashMap<>();
			for (OldDocument document : toUpdate){
				document.update(changes);
			}
		}
	}
	
	public void close() throws IOException{
		dbFileChannel.close();
		while (cache.isEmpty() == false){
			cache.pop().getV2().close();
		}
	}
}
