package com.salaboy.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.cloudevents.v02.CloudEventBuilder;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.cloudevents.v02.AttributesImpl;
import java.net.URI;
import java.net.URISyntaxException;


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
                                    System.out.println("Incoming Headers >>>>>>>>>>>>" + req.headers());
                                    sendCloudEvent(receivedEvent);
                                }
                            });
                    req.response().end();
                })
                .rxListen(8080).subscribe(server -> {
            System.out.println("Server running!");
        });
    }

    public void sendCloudEvent(CloudEvent cloudEvent) throws URISyntaxException {
        final HttpClientRequest request = vertx.createHttpClient().post(80, "http://cloudevents-go.default.svc.cluster.local", "/");
        final CloudEvent<AttributesImpl, String>  myCloudEvent = CloudEventBuilder.<String>builder()

                .withId("1234-abcd")
                .withType("java-event")
                .withSource(URI.create("cloudevents-java.default.svc.cluster.local"))
                .withData("{\"name\" : \"Salaboy From Java Cloud Event\" }")
                .withContenttype("application/json")
                .build();
// add a client response handler
        request.handler(resp -> {
            // react on the server response
            System.out.println("Event posted: " +resp.statusCode() + "-> " + resp.statusMessage());

        });
//        request.headers().set("Host", "cloudevents-go.default.34.65.101.52.xip.io");
// write the CloudEvent to the given HTTP Post request object
        VertxCloudEvents.create().writeToHttpClientRequest(myCloudEvent, request);
        System.out.println("New >>>>>>>>>>>>" + request.headers());
        request.end();
    }
}

