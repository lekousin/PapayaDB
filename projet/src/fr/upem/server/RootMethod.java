/**
 * 
 */
package fr.upem.server;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;

/**
 * @author master
 *
 */
public class RootMethod {
	private final String nameRequete;

	public String getNameRequete() {
		return nameRequete;
	}

	public String getValue() {
		return value;
	}

	private final String value;
	public final static String REGEX_DB = "method=%27(\\S[a-zA-Z\\d]*?)%27&value=%27(\\S[a-zA-Z\\d]*?)%27$";

	public RootMethod(String nameRequete, String value) {
		Objects.requireNonNull(nameRequete);
		Objects.requireNonNull(value);
		this.value = value;
		this.nameRequete = nameRequete;
	}

	public RootMethod(String nameRequete) {
		Objects.requireNonNull(nameRequete);
		this.value = null;
		this.nameRequete = nameRequete;
	}

	@Override
	public String toString() {
		return nameRequete + " " + value;
	}

	public static RootMethod builtQueryWithParameters(HttpServerRequest request, String checkQueryFormatRegex) {
		Objects.requireNonNull(request.query());
		Matcher matcher = checkRegex(request.query(), checkQueryFormatRegex);
		return new RootMethod(matcher.group(1), matcher.group(2));
	}

	/**
	 * @param request
	 * @param checkQueryFormatRegex
	 * @return
	 * @throws IllegalAccessError
	 */
	private static Matcher checkRegex(String query, String checkQueryFormatRegex) throws IllegalAccessError {
		Pattern pattern = Pattern.compile(checkQueryFormatRegex);
		Matcher matcher = pattern.matcher(query);
		if (!matcher.matches()) {
			throw new IllegalAccessError();
		}
		return matcher;
	}

	public static String listdb(RootMethod query) {
		return Json.encodePrettily(query);
	}
}
