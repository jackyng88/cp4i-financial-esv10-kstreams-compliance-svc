package com.ibm.garage.cpat.domain;

import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.kafka.client.serialization.JsonbSerializer;


@ApplicationScoped
public class ComplianceTopology {

    //private static final String KTABLE_TOPIC = "kstreams-ktable-topic";

    @ConfigProperty(name = "START_TOPIC_NAME")
    private String INCOMING_TOPIC;

    @ConfigProperty(name = "TARGET_TOPIC_NAME")
    private String OUTGOING_TOPIC;


    @Produces
    public Topology buildTopology() {

        StreamsBuilder builder = new StreamsBuilder();

        JsonbSerde<FinancialMessage> financialMessageSerde = new JsonbSerde<>(FinancialMessage.class);

        // A stream processor (node) within the topology (graph of nodes). Here, initially
        // the stream is provided with an "incoming topic" to consume from. This incoming stream
        // has it's messages deserialized with financialMessageSerde and then filtered by calling
        // checkCompliance. If this returns true we call a mapValues with that message to 
        // change the necessary flag to false to indicate the check is complete. Finally we then 
        // send it back to the topic and use the same serde to serialize it into JSON.
        builder.stream(
            INCOMING_TOPIC,
            Consumed.with(Serdes.String(), financialMessageSerde)
        )
        .filter(
            (key, message) -> checkCompliance(message)
        )
        // Below mapping is necessary if incoming record has no Key value.
        // .map(
        //     (key, message) -> new KeyValue<>(message.user_id, performComplianceCheck(message))
        // )
        .mapValues (
            checkedMessage -> performComplianceCheck(checkedMessage)
        )
        .to (
            INCOMING_TOPIC,
            Produced.with(Serdes.String(), financialMessageSerde)
        );  
        
        return builder.build();
    }

    public boolean checkCompliance (FinancialMessage rawMessage) {
        // Returns a boolean based on the compliance_services flag.
        return (rawMessage.compliance_services);
    }

    public FinancialMessage performComplianceCheck(FinancialMessage checkedMessage) {
        // Perform the "check" and then return the transformed object.
        checkedMessage.compliance_services = false;

        if (!checkedMessage.technical_validation) {
            checkedMessage.technical_validation = !checkedMessage.technical_validation;
        }

        return checkedMessage;
    }
}