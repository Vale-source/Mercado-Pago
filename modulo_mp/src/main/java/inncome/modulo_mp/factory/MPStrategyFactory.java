package inncome.modulo_mp.factory;

import inncome.modulo_mp.service.serviceImpl.PaymentAccountMoneyStrategy;
import inncome.modulo_mp.service.serviceImpl.PaymentCardStrategy;
import inncome.modulo_mp.strategy.PaymentGenerationStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class MPStrategyFactory {

    private final ApplicationContext applicationContext;

    public MPStrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public PaymentGenerationStrategy getStrategy(String paymentType) {
        switch (paymentType.toLowerCase()) {
            case "credit_card":
            case "debit_card":
                return applicationContext.getBean(PaymentCardStrategy.class);
            case "account_money":
                return applicationContext.getBean(PaymentAccountMoneyStrategy.class);
            default:
                throw new IllegalArgumentException("Tipo de pago no soportado: " + paymentType);
        }
    }
}