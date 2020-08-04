// package com.ibm.garage.cpat.infrastructure;

// import javax.enterprise.context.ApplicationScoped;

// import org.eclipse.microprofile.reactive.messaging.Outgoing;

// import io.reactivex.Flowable;
// import io.smallrye.reactive.messaging.kafka.KafkaRecord;

// import java.util.concurrent.TimeUnit;
// import java.util.Random;

// import com.ibm.garage.cpat.domain.*;


// @ApplicationScoped
// public class MockProducer {

//     private Random random = new Random();
//     private String[] sampleStockSymbols = {"AXP", "BABA", "CAT"};

//     private FinancialMessage mock = new FinancialMessage("1234", "AXP", "SWISS", "bonds", "10/20/2019",
//                                                          "10/21/2019", 12, 1822.38, 21868.55, 94, 7,
//                                                          true, false, false, false, false);

//     @Outgoing("mock-messages")
//     public Flowable<KafkaRecord<String, FinancialMessage>> produceMock() {
//         return Flowable.interval(5, TimeUnit.SECONDS)
//                        .map(tick -> {
//                             return generateRandomMessage(mock);
//                         });
//     }

//     public KafkaRecord<String, FinancialMessage> generateRandomMessage(FinancialMessage mock) {
//         mock.user_id = String.valueOf(random.nextInt(100));
//         mock.stock_symbol = sampleStockSymbols[random.nextInt(3)];

//         return KafkaRecord.of(mock.user_id, mock);
//     }
// }