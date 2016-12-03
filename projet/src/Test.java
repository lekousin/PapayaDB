import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.json.Json;

import fr.upem.papayadb.database.Database;
import fr.upem.papayadb.database.Document;

public class Test {
	public static void main(String[] args) throws Exception {
		/*Document doc = Document.openDocument("test.json");
		Map<String, String> select = doc.select(Json.createObjectBuilder()
				.add("fields", Json.createArrayBuilder()
						.add("name")
						.add("a"))
				.build());
		select.forEach((k, v) -> {
			System.out.println(k + ": " + v);
		});*/
		Database db = new Database("db.txt");
		db.delete(Json.createObjectBuilder()
				.add("documents", Json.createArrayBuilder()
						.add("test2"))
				.build());
		List<Map<String, String>> select = db.select(Json.createObjectBuilder()
				.add("fields", Json.createArrayBuilder()
						.add("name")
						.add("a"))
				.build());
		select.forEach(r -> {
			System.out.println("Row data:");
			r.forEach((k, v) -> {
				System.out.println("\t" + k + ": " + v);
			});
		});
	}
}
