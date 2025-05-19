package org.pm.patientservice.aspects;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PatientServiceMetrics {

    private final MeterRegistry meterRegistry;

    public PatientServiceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    @Around("execution(* org.pm.patientservice.service.PatientService.getAllPatients(..))")
    public Object monitorGetPatients(ProceedingJoinPoint joinPoint) throws Throwable {
        meterRegistry.counter("custom.redis.cache.miss","cache","patients").increment();
        return joinPoint.proceed();
    }


}
