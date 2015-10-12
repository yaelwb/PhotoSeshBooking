package utilities;

import models.Booking;

import java.util.function.Predicate;

/**
 * Created by yael on 10/11/15.
 */
public class Predicates {

    public static Predicate<Booking> status(String val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getStatus().equals(val.toUpperCase());
    }
}
