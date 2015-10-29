package enums;

/**
 * Created by yael on 10/10/15.
 */
public enum State {
    CREATED("Created"),
    BOOKED("Booked"),
    DOWNPAYMENT("DownPayment"),
    PREPARATION("Preparation"),
    PHOTOSHOOT("Photoshoot"),
    PAYMENT("Payment"),
    SELECTIONS("Selections"),
    EDITING("Editing"),
    REVIEW("Review"),
    COMPLETE("Complete"),
    CANCELED("Canceled"),
    POSTPONED("Postponed");

    private String name;
    State(String name) {
        this.name = name;
    }
}
