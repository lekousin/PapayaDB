package fr.upem.server;

import java.util.Objects;

public class Query {
	
	private final String nameRequete;
	private final String value;

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
}
