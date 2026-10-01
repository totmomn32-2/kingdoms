package dev.kingdoms;

public enum KingdomType {
    RED("Red Kingdom"),
    BLUE("Blue Kingdom"),
    GREEN("Green Kingdom");

    private final String displayName;

    KingdomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KingdomType fromString(String text) {
        for (KingdomType type : values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
