package io.vertx.demo;

import io.vertx.core.AbstractVerticle;

public class HelloVerticle extends AbstractVerticle {
    @Override
    public void start(){
        vertx.eventBus().consumer("hello.vertx.addr",msg -> {
            String name = (String) msg.body();
            msg.reply("Hello Verticle " + name);
        });
    }
    
}
