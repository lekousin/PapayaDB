package fr.upem.server;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.http.HttpServerRequest;

public class Query {

	private final String nameRequete;

	public String getNameRequete() {
		return nameRequete;
	}

	public String getValue() {
		return value;
	}

	private final String value;
	private final static String checkQueryFormatRegex = "method=%27(\\S[a-zA-Z\\d]*?)%27&value=%27(\\S[a-zA-Z\\d]*?)%27$";

	public Query(String nameRequete, String value) {
		Objects.requireNonNull(nameRequete);
		Objects.requireNonNull(value);
		this.value = value;
		this.nameRequete = nameRequete;
	}

	@Override
	public String toString() {
		return nameRequete + " " + value;
	}

	public static Query detectParameters(HttpServerRequest request) {
		Objects.requireNonNull(request.query());
		Pattern pattern = Pattern.compile(checkQueryFormatRegex);
		Matcher matcher = pattern.matcher(request.query());
		if (!matcher.matches()) {
			throw new IllegalAccessError();
		}
		return new Query(matcher.group(1), matcher.group(2));
	}

	public static String createdb(Query query) {
		return query.toString();
	}

	public static String deletedb(Query query) {
		return query.toString();
	}
}
