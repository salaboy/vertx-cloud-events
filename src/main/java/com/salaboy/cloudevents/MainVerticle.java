package com.salaboy.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpClientRequest;


public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        vertx
                .createHttpServer()
                .requestHandler(req -> {
                    VertxCloudEvents.create().rxReadFromRequest(req)
                            .subscribe((receivedEvent, throwable) -> {
                                if (receivedEvent != null) {
                                    // I got a CloudEvent object:
                                    System.out.println("The event type: " + receivedEvent.getAttributes().getType());
                                    sendCloudEvent(receivedEvent);
                                }
                            });
                    req.response().end();
                })
                .rxListen(8080).subscribe(server -> {
            System.out.println("Server running!");
        });
    }

    public void sendCloudEvent(CloudEvent cloudEvent){
        final HttpClientRequest request = vertx.createHttpClient().post(8080, "localhost", "/");

// add a client response handler
        request.handler(resp -> {
            // react on the server response
            System.out.println("Event posted: " +cloudEvent);
        });

// write the CloudEvent to the given HTTP Post request object
        VertxCloudEvents.create().writeToHttpClientRequest(cloudEvent, request);
        request.end();
    }
}

