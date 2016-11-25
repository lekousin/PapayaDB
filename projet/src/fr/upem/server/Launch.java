package fr.upem.server;

import io.vertx.core.Vertx;

public class Launch {

	public static void main(String[] argv){
		System.out.println("Launch WEB Server");
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Server());
	}
}
