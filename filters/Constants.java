package filters;


public class Constants {
    private static PayloadStrategy PAYLOAD_STRATEGY = PayloadStrategy.NEIGHBORING;

    public static PayloadStrategy getPayloadStrategy() {
        return PAYLOAD_STRATEGY;
    }

    public static void setPayloadStrategy(PayloadStrategy newStrategy) {
        PAYLOAD_STRATEGY = newStrategy;
    }
}
