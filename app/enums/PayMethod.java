package enums;

/**
 * Created by yael on 10/10/15.
 */
public enum PayMethod {
    CASH("Cash"),
    CHECK("Check"),
    PAYPAL("PayPal"),
    SQUARE("Square");

    private String name;
    PayMethod(String name) {
        this.name = name;
    }
}
