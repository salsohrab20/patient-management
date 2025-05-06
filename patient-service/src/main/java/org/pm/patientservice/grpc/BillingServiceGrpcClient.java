package org.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.pm.patientservice.kafka.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final KafkaProducer kafkaProducer;
    private BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.server.grpc.port:9001}") int serverPort,
            KafkaProducer kafkaProducer) {

        log.info("Connecting to billing GRPC service at {} : {}", serverAddress, serverPort);

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(
                serverAddress, serverPort
        ).usePlaintext().build();

        blockingStub = BillingServiceGrpc.newBlockingStub(managedChannel);
        this.kafkaProducer = kafkaProducer;
    }

    /*
    * name - name of circuit breaker instance
    * fallbackMethod - what method to use if the request fails
    * retry - specifies to attempt the request 3 times before triggering circuit breaker
    * */
    @CircuitBreaker(name="billingService" , fallbackMethod = "billingFallback")
    @Retry(name ="billingRetry")
    public BillingResponse createNewBillingAccount(String patientId, String name, String email) {

        BillingRequest billingRequest = BillingRequest
                .newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();

        BillingResponse billingResponse = blockingStub.createNewBillingAccount(billingRequest);

        log.info("Received response from billing service : {}", billingResponse);
        return billingResponse;

    }

    /*
    * same signature as createBillingAccount
    * t -> exception that cause the circuit to open
    * */
    public BillingResponse billingFallback(String patientId, String name, String email, Throwable t) {

        log.warn("[CIRCUIT BREAKER]: Billing service is unavailable. Triggered " +
                "fallback  :{}", t.getMessage());

      kafkaProducer.sendBillingAccountEvent(patientId, name, email);

        return BillingResponse.newBuilder()
                .setAccountId("")
                .setStatus("PENDING")
                .build();

    }
}
