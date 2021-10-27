package io.vertx.demo;

import java.security.AuthProvider;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> start) {

    //DeploymentOptions opts = new DeploymentOptions().setWorker(true).setInstances(8);
            
    //vertx.deployVerticle("io.vertx.demo.HelloVerticle",opts);

    vertx.deployVerticle(new HelloVerticle());

    Router baseRouter = Router.router(vertx);

    baseRouter.route("/test-api/").method(HttpMethod.GET).consumes("application/json").handler(this::testApiHandler);
     
    baseRouter.get("/hello/").handler(this::helloHandler);

    //baseRouter.get("/hello/:name").handler(this::nameHandler);

    baseRouter.get("/hello/:name").handler(this::verticleHandler);

    basicConfig(start, baseRouter);

  }

  private void basicConfig(Promise<Void> start, Router router){
    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
        .setType("file")
        .setFormat("json")
        .setConfig(new JsonObject().put("path", "config.json"));

    ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
        .addStore(defaultConfig);

    Handler<AsyncResult<JsonObject>> handler = asyncResult-> this.handleConfigResults(start, router, asyncResult);

    ConfigRetriever configRetriever = ConfigRetriever.create(vertx, opts);
    configRetriever.getConfig(handler);
  }

  private void handleConfigResults(Promise<Void> start, Router router, AsyncResult<JsonObject> asyncResult){
    if(asyncResult.succeeded()){
      JsonObject config = asyncResult.result();
      JsonObject http = config.getJsonObject("http");
      int httpPort = http.getInteger("port");
      vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(httpPort);
      start.complete();
    }else{
      start.fail("Unable to load configuration ");
    }
  }

  //Router handlers

  private void testApiHandler(RoutingContext routingContext){
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Test API");
  }

  private void helloHandler(RoutingContext routingContext){
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello API");
  }

  private void nameHandler(RoutingContext routingContext){
    String name = routingContext.pathParam("name");
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello "+name);
  }

  private void verticleHandler(RoutingContext routingContext){
    String name = routingContext.pathParam("name");
    vertx.eventBus().request("hello.vertx.addr", name,reply -> {
      routingContext.response().end((String)reply.result().body());
    });
  }

}
