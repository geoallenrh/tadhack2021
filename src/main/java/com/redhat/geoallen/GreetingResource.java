package com.redhat.geoallen;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.jboss.resteasy.annotations.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;

import javax.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.core.Request;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;



@Path("/")
public class GreetingResource {

    @Inject
    Vertx vertx;

    @Inject
    ObjectMapper objectMapper;

    @Context
    Request request;

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaBootstrapServer;

  @ConfigProperty(name = "kafka.twilio.topic")
    String twilio_in_topic;

    private KafkaProducer<String, String> producer;

  @PostConstruct
    void initKafkaClient() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaBootstrapServer);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.out.println("bootstrapping Kafka with config: " + config);

        producer = KafkaProducer.create(vertx, config);
    }


    @Path("/hello")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }


    @POST
    @Path("/sms")
    //@Consumes(MediaType.TEXT_PLAIN)
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_XML)
    public String sms(@Form AvayaMessage message, String requestBody) {
        LOG.info(message.getBody());
        //LOG.info(request.);
        LOG.info("SMS Messsage Body: " + requestBody);

        /**AccountSid=AC400001a0ee01c9b648924b68b613b428
        &ApiVersion=v2
        &Body=Test
        &DlrStatus=
        &ErrorMessage=&
        From=%2B14173802843
        &Price=0
        &SmsSid=SMf11903b683d44843812ab6a8d9c1234e
        &SmsStatus=received
        &To=%2B16062528425
        **/
        //String response = "<Response><Message>Thank You!  We received your message:  </Message></Response>";

        String avayaResponse = "<Response> <Sms>Welcome to Avaya! Let us know if we can help you in any way during your development.</Sms> </Response>";
        return avayaResponse;
    }

    


}