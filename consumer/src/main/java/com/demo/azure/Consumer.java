package com.demo.azure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redislabs.redistimeseries.Aggregation;
import com.redislabs.redistimeseries.RedisTimeSeries;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class Consumer {

    private RedisTimeSeries rts;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.password}")
    private String redisPassword;

    @PostConstruct
    public void init() {
        System.out.println("Connecting to Redis");

        int timeout = 2000;
        boolean ssl = true;

        GenericObjectPoolConfig<Jedis> jedisPoolConfig = new GenericObjectPoolConfig<>();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout, redisPassword, ssl);

        this.rts = new RedisTimeSeries(jedisPool);
    }

    final static String TEMP_METRIC = "temp";
    final static String PRESSURE_METRIC = "pressure";

    @KafkaListener(topics = "${topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        
        System.out.println("Record - " + record.value());

        // sample records format: location,device,temp,pressure
        // sample reading: 1,2,40,50
        String[] data = record.value().split(",");
        String location = data[0]; // 1
        String device = data[1]; // 2
        String temp = data[2];
        String pressure = data[3];

        long tstamp = record.timestamp(); // ideally, this should come from source system

        Map<String, String> labels = new HashMap<>();
        labels.put("location", location);
        labels.put("device", device);

        // add temperature reading first
        labels.put("metric", TEMP_METRIC);
        String timeSeriesKey = TEMP_METRIC + ":" + location + ":" + device;
        rts.add(timeSeriesKey, tstamp, Double.parseDouble(temp), labels);

        System.out.println("adding " + TEMP_METRIC + " to time series " + timeSeriesKey + " for device " + device
                + " with labels " + labels);

        // add pressure reading
        labels.put("metric", PRESSURE_METRIC);
        timeSeriesKey = PRESSURE_METRIC + ":" + location + ":" + device;
        rts.add(timeSeriesKey, tstamp, Double.parseDouble(pressure), labels);

        System.out.println("adding " + PRESSURE_METRIC + " to time series " + timeSeriesKey + " for device " + device
                + " with labels " + labels);
    }

    @Bean
    ErrorHandler errorHandler() {
        return new LoggingErrorHandler();
    }
}
