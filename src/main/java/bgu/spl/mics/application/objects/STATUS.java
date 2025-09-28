package bgu.spl.mics.application.objects;

/**
 * Represents the status of a system component.
 * Possible statuses:
 * - UP: The component is operational.
 * - DOWN: The component is non-operational.
 * - ERROR: The component has encountered an error.
 */
public enum STATUS {
    UP(0), DOWN(1), ERROR(2);

    private final int value;

    STATUS(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static STATUS fromValue(int value) {
        for (STATUS status : STATUS.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid value for Status: " + value);
    }
}

