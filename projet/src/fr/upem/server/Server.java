package fr.upem.server;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.upem.decoder.Decoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class Server extends AbstractVerticle{
	
	private final int port = 8080;
	private final String checkQueryFormatRegex ="method=([a-zA-Z\\d]*?)&value=([a-zA-Z\\d]*?)$";

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		manageRouter(router);
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer(createHttpSServerOptions()).requestHandler(router::accept).listen(port);
		System.out.println("listen on port "+port);
	}
	
	private HttpServerOptions createHttpSServerOptions(){
		return new HttpServerOptions().setSsl(true).setKeyStoreOptions(
		        new JksOptions().setPath("keystore.jks").setPassword("direct11") );
	}
	
	private void manageRouter(Router router){
		Route postDbMethod = router.route(HttpMethod.POST,"/api/json/db");
		postDbMethod.handler(this::manageDataBaseRoutingContext);

	}
	

	private boolean isAuthentified(HttpServerRequest request){
		String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.substring(0,6).equals("Basic ")) {
			String identifiant = authorization.substring(6);
			System.out.println(Decoder.decode(identifiant));
			return true;
		}		
		return false;
	}
		
	private void manageDataBaseRoutingContext(RoutingContext routingContext) {
		Objects.requireNonNull(routingContext);
		HttpServerRequest request = routingContext.request();
		if(isAuthentified(request)){
			manageHttpServerRequest(request);
		}else{
			throw new IllegalStateException("No authentified.");
		}
	}
	
	private void manageHttpServerRequest(HttpServerRequest request){
		Objects.requireNonNull(request);
		Query query = detectParameters(request);
		if(query!=null)
		{
			System.out.println(query);
		}
	}
	
	private Query detectParameters(HttpServerRequest request){
		Objects.requireNonNull(request.query());	
	    Pattern pattern = Pattern.compile(checkQueryFormatRegex);
	    Matcher matcher = pattern.matcher(request.query());
	    if(!matcher.matches()){
	    	throw new IllegalAccessError("URL INVALIDE");
	    }
    	return new Query(matcher.group(1), matcher.group(2));		
	}
	
}
