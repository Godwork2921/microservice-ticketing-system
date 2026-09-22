package com.yeab.ticketing.payment.provider;

import com.yeab.ticketing.payment.exception.ChapaIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProviderFactory {

    private final Map<String, PaymentProvider> providersByName;
    private final String activeProvider;

    public PaymentProviderFactory(Map<String, PaymentProvider> providersByName,
                                  @Value("${app.payment.provider:mock}") String activeProvider) {
        this.providersByName = providersByName.values().stream()
                .collect(Collectors.toMap(PaymentProvider::name, Function.identity()));
        this.activeProvider = activeProvider;
    }

    public PaymentProvider get() {
        PaymentProvider provider = providersByName.get(activeProvider);
        if (provider == null) {
            throw new ChapaIntegrationException(
                    "No payment provider registered for configured value '" + activeProvider + "'");
        }
        return provider;
    }

    public String activeName() {
        return activeProvider;
    }
}