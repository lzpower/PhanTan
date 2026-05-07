package entity;

public enum PaymentMethod {
    TIENMAT("Tiền mặt"),
    CHUYENKHOAN("Chuyển khoản");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return TIENMAT;
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.displayName.equalsIgnoreCase(displayName.trim())) {
                return method;
            }
        }
        return TIENMAT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
