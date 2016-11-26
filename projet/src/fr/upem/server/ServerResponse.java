package fr.upem.server;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

/**
 * @author kristof
 * This class show 
 *
 */
public class ServerResponse {
	
	public static void jsonResponse(RoutingContext routingContext,String json){
		routingContext.response().putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(json.length()))
		.putHeader("Content-type", "application/json")
		.write(json)
		.end();
	}
	
	public static void authentificationError(RoutingContext routingContext){
		sendErrorResponse(routingContext, "Error Authentification");
	}
	
	public static void queryError(RoutingContext routingContext){
		sendErrorResponse(routingContext, "Error Bad Query");
	}
	
	public static void requestError(RoutingContext routingContext){
		sendErrorResponse(routingContext, "Error Bad request");
	}
	
	private static void sendErrorResponse(RoutingContext routingContext,String error){
		routingContext.response().putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(error.length()))
		.putHeader("Content-type", "application/json")
		.write(error)
		.end();
	}
}
