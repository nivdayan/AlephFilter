package filters;

public enum PayloadStrategy {
    MIRRORING("mirroring"),
    NEIGHBORING("neighboring");
    private final String value;

    PayloadStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

