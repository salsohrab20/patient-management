package com.pm.stack.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createNewBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver){

        log.info("createNewBillingRequest received : {}",billingRequest.toString());

        //Business Logic, saveToDB and perform calculations

        BillingResponse billingResponse = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(billingResponse);

        responseObserver.onCompleted();

    }
}
