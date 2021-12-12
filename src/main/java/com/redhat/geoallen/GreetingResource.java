package com.redhat.geoallen;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

  @ConfigProperty(name = "kafka.tadhack.topic")
    String tadhack_topic;

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
        LOG.info("SMS Messsage Body: " + requestBody);
        sendMessageToKafka(message);

        
        String avayaResponse = "<Response> <Sms>Welcome to Avaya! Let us know if we can help you in any way during your development.</Sms> </Response>";
        return avayaResponse;
    }

    public void sendMessageToKafka(AvayaMessage smsMessage) {
      
        try {
           //String jsonMessage = objectMapper.writeValueAsString(smsMessage);
           //{"time":"2021-12-12T00:20:58.289Z","sender":"Daniel Schimpfoessl","msg":"<p>save</p>","method":"spaces"}
           //LocalDate date = LocalDate.now();
           //String currentTime = date.format(DateTimeFormatter.ISO_INSTANT);
           String currentTime = "2021-12-12T00:20:58.289Z";
           Map<String, String> elements = new HashMap();
           elements.put("time", currentTime);
           elements.put("sender", "Geoff");
           elements.put("msg", smsMessage.getBody());
           elements.put("method", "sms");

           String jsonKey = objectMapper.writeValueAsString("sms-message");
           String jsonMessage = objectMapper.writeValueAsString(elements);
           System.out.println("Message:" + jsonMessage);
           System.out.println("Key: " + jsonKey);
            KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(tadhack_topic,jsonKey, jsonMessage);
            producer.write(record, done -> System.out.println("Kafka message sent: twilio message - " + smsMessage.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    


}