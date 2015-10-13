package utilities;

import models.Booking;
import models.Customer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.function.Predicate;

/**
 * Created by yael on 10/11/15.
 */
public class Predicates {

    //Customer

    public static Predicate<Customer> payMethod(String val) {
        if (val == null)
            return customer -> true;
        return customer -> customer.getPayMethod().toUpperCase().equals(val.toUpperCase());
    }

    public static Predicate<Customer> balanceGreaterOrEqual(BigDecimal val) {
        if (val == null)
            return customer -> true;
        return customer -> customer.getBalance().compareTo(val) != -1;
    }

    public static Predicate<Customer> balanceLessOrEqual(BigDecimal val) {
        if (val == null)
            return customer -> true;
        return customer -> customer.getBalance().compareTo(val) != 1;
    }

    //Booking

    public static Predicate<Booking> status(String val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getStatus().equals(val.toUpperCase());
    }

    public static Predicate<Booking> eventType(String val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getEventType().toUpperCase().equals(val.toUpperCase());
    }

    public static Predicate<Booking> eventDateBeforeOrEqual(Timestamp val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getEventDate().before(val) || booking.getEventDate().equals(val);
    }

    public static Predicate<Booking> eventDateAfterOrEqual(Timestamp val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getEventDate().after(val) || booking.getEventDate().equals(val);
    }

    public static Predicate<Booking> priceGreaterOrEqual(BigDecimal val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getPrice().compareTo(val) != -1;
    }

    public static Predicate<Booking> priceLessOrEqual(BigDecimal val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getPrice().compareTo(val) != 1;
    }

    //If selections were not made at all, both should return false
    //The correct filter will be filtering by Status == SELECTIONS, meaning numSelected
    //is not final yet, and hence pictures left to process can not yet be determined
    public static Predicate<Booking> imagesToProcessGreaterOrEqual(Integer val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getNumSelected() > 0 &&
                booking.getNumSelected() - booking.getNumProcessed() >= val;
    }

    public static Predicate<Booking> imagesToProcessLessOrEqual(Integer val) {
        if (val == null)
            return booking -> true;
        return booking -> booking.getNumSelected() > 0 &&
                booking.getNumSelected() - booking.getNumProcessed() <= val;
    }
}
