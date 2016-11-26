package fr.upem.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import fr.upem.decoder.Decoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * @author kristof This class describes the implementation of a custom vertx
 *         server
 *
 */
public class Server extends AbstractVerticle {

	private int portHTTPS;

	/**
	 * 
	 * @param port
	 */
	public Server(int port) {
		this.portHTTPS = port;
	}

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		manageRouter(router);
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer(createHttpSServerOptions()).requestHandler(router::accept).listen(portHTTPS);
	}

	private HttpServerOptions createHttpSServerOptions() {
		return new HttpServerOptions().setSsl(true)
				.setKeyStoreOptions(new JksOptions().setPath("keystore.jks").setPassword("direct11"));
	}

	private void manageRouter(Router router) {
		Route postDbMethod = router.route(HttpMethod.POST, "/api/json/db");
		Route insertDocMethod = router.route(HttpMethod.POST, "/api/json/document");

		postDbMethod.handler(this::manageDataBaseRoutingContext);
		insertDocMethod.handler(this::manageInsertRoutingContext);

	}

	private void manageInsertRoutingContext(RoutingContext routingContext) {
		HttpServerRequest request = routingContext.request();
		System.out.println(request.absoluteURI());
		System.out.println(request.path());

		manageQueryFromHttpServer(routingContext);
	}

	private boolean isAuthentified(HttpServerRequest request) {
		String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.substring(0, 6).equals("Basic ")) {
			String identifiant = authorization.substring(6);
			System.out.println(Decoder.decode(identifiant));
			return true;
		}
		return false;
	}

	private void manageDataBaseRoutingContext(RoutingContext routingContext) {
		HttpServerRequest request = routingContext.request();
		if (isAuthentified(request)) {
			manageQueryFromHttpServer(routingContext);
		} else {
			ServerResponse.authentificationError(routingContext);
			throw new IllegalStateException("No authentified.");
		}
	}

	private void manageQueryFromHttpServer(RoutingContext routingContext) {
		try {
			Query query = Query.detectParameters(routingContext.request());
			String response = execQuery(query);
			ServerResponse.jsonResponse(routingContext, Json.encodePrettily(response));
		} catch (Exception e) {
			ServerResponse.queryError(routingContext);
		}
	}

	private String execQuery(Query query) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<Query> q = Query.class;
		Method gs1Method = q.getMethod(query.getNameRequete(), new Class[] { Query.class });
		String response = (String) gs1Method.invoke(q, new Object[] { query });
		return response;
	}

}
