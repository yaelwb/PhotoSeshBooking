package services;

import com.google.inject.Inject;
import enums.State;
import models.Booking;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import play.Logger;
import play.db.jpa.JPA;
import utilities.RequestUtil;
import utilities.StatusUtil;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
public class BookingServiceImpl implements BookingService {

    private final CustomerService customerService;

    @Inject
    public BookingServiceImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public Booking create(Booking inputBooking, CustomerService customerService) {
        Long customerId = inputBooking.getCustomerId();
        if (customerId == null || !customerService.customerIdExists(customerId)) {
            Logger.error("services.BookingService.create(): Customer Id missing/invalid");
            return null;
        }

        Booking booking = new Booking(customerId);

        JPA.em().persist(booking);
        Logger.info("services.BookingService.create(): Created booking " + booking.toString());
        return booking;
    }

    @Override
    public int delete(Long id) {
        Query query = JPA.em().createQuery("DELETE Booking WHERE id =:id ").setParameter("id", id);
        int res = query.executeUpdate();
        if (res == 0)
            Logger.info("services.BookingService.delete(): booking not found");
        else
            Logger.info("services.BookingService.delete(): deleted booking " + id);
        return res;
    }

    @Override
    public List<Booking> getAll() {
        Session session = JPA.em().unwrap(Session.class);
        Criteria cr = session.createCriteria(Booking.class);

        String[] statusList = RequestUtil.getQueryParams("status");
        if(statusList != null)
            cr.add(Restrictions.in("status", statusList));

        String eventType = RequestUtil.getQueryParam("eventType");
        if(eventType != null)
            cr.add(Restrictions.eq("eventType", eventType));

        String eventDateFrom = RequestUtil.getQueryParam("eventDateFrom");
        if(eventDateFrom != null)
            cr.add(Restrictions.ge("eventDate", Timestamp.valueOf(eventDateFrom)));

        String eventDateTo = RequestUtil.getQueryParam("eventDateTo");
        if(eventDateTo != null)
            cr.add(Restrictions.le("eventDate", Timestamp.valueOf(eventDateTo)));

        String fromPrice = RequestUtil.getQueryParam("fromPrice");
        if(fromPrice != null)
            cr.add(Restrictions.ge("price", new BigDecimal(fromPrice)));

        String toPrice = RequestUtil.getQueryParam("toPrice");
        if(toPrice != null)
            cr.add(Restrictions.le("price", new BigDecimal(toPrice)));

        RequestUtil.paginate(cr);
        List<Booking> results = cr.list();

        if (results == null || results.isEmpty())
            Logger.info("services.BookingService.getAll(): No bookings to show");
        else
            Logger.info("services.BookingService.getAll(): returned " + results.size() + " bookings.");
        return results;
    }

    @Override
    public Booking get(Long id) {
        Booking booking = JPA.em().find(Booking.class, id);
        if (booking == null)
            Logger.info("services.BookingService.get(): booking not found");
        else
            Logger.info("services.BookingService.get(): returned " + booking.toString());
        return booking;
    }

    @Override
    public String update(Booking input, Booking orig) {
        State toState = StatusUtil.getState(input.getStatus());
        switch(toState) {
            case BOOKED:
                return booked(input, orig);
            case DOWNPAYMENT:
                return downpayment(input, orig);
            case PREPARATION:
                return preparation(input, orig);
            case PHOTOSHOOT:
                return photoshoot(input, orig);
            case PAYMENT:
                return payment(input, orig);
            case SELECTIONS:
                return selections(input, orig);
            case EDITING:
                return editing(input, orig);
            case REVIEW:
                return review(input, orig);
            case COMPLETE:
                return complete(input, orig);
            case CANCELED:
                return cancel(input, orig);
            case POSTPONED:
                return postpone(input, orig);
        }
        return null;
    }

    private String booked (Booking input, Booking orig) {
        //check the required data either exists or is being set
        if(input.getEventDate() == null && orig.getEventDate() == null)
            return "You must set a date for the event to be booked.";
        if(input.getLocation() == null && orig.getLocation() == null)
            return "You must set a location for the event to be booked.";
        if(input.getEventType() == null && orig.getEventType() == null)
            return "You must set an event type for the event to be booked.";
        if(input.getPrice() == null && orig.getPrice() == null)
            return "You must set a price for the event to be booked.";

        //All good, update the db entry
        orig.setEventDate(input.getEventDate());
        orig.setLocation(input.getLocation());
        orig.setEventType(input.getEventType());
        orig.setPrice(input.getPrice());
        orig.setAmountPaid(new BigDecimal(0.0));
        customerService.addToBalance(input.getCustomerId(), input.getPrice());
        orig.setStatus(State.BOOKED.toString());
        return null;
    }

    //TODO
    private String downpayment (Booking input, Booking orig) {
        return null;
    }

    //TODO
    private String preparation (Booking input, Booking orig) {
        return null;
    }

    //TODO
    private String photoshoot (Booking input, Booking orig) {
        return null;
    }

    private String payment (Booking input, Booking orig) {
        BigDecimal increment = input.getAmountPaid();
        increment.subtract(orig.getAmountPaid());
        customerService.subtractFromBalance(orig.getCustomerId(), increment);

        orig.setAmountPaid(input.getAmountPaid());
        orig.setStatus(State.PAYMENT.toString());
        return null;
    }

    private String selections (Booking input, Booking orig) {
        State fromState = StatusUtil.getState(orig.getStatus());
        switch(fromState) {
            case PAYMENT:
                //override payment - allow customer to pay a part of the balance later on
                String override = RequestUtil.getQueryParam("overridePayment");
                if(!input.getAmountPaid().equals(input.getPrice())) {
                    if (override == null || override.equals("false")) {
                        return "Amount must be paid in full before selections are made.";
                    }
                }
                orig.setNumProcessed(0);
                break;
            case SELECTIONS:
                orig.setNumSelected(input.getNumSelected());
                break;
            case REVIEW:
                orig.setNumSelected(input.getNumSelected());
                orig.setNumProcessed(input.getNumProcessed());
                orig.setReviewNotes(input.getReviewNotes());
                break;
        }

        orig.setStatus(State.SELECTIONS.toString());
        return null;
    }

    private String editing (Booking input, Booking orig) {
        State fromState = StatusUtil.getState(orig.getStatus());
        switch(fromState) {
            case SELECTIONS:
                orig.setNumProcessed(0);
                break;
            case EDITING:
                orig.setNumProcessed(input.getNumProcessed());
                break;
            case REVIEW:
                orig.setNumSelected(input.getNumSelected());
                orig.setNumProcessed(input.getNumProcessed());
                orig.setReviewNotes(input.getReviewNotes());
                break;
        }

        orig.setStatus(State.EDITING.toString());
        return null;
    }

    private String review (Booking input, Booking orig) {
        String override = RequestUtil.getQueryParam("override");
        int numTodo = input.getNumSelected() - input.getNumProcessed();
        if(numTodo > 0) {
            if (override == null || override.equals("false")) {
                return "There are " + numTodo + " unprocessed images. You must override in order to sent to partial review.";
            }
        }

        orig.setStatus(State.REVIEW.toString());
        return null;
    }

    private String complete (Booking input, Booking orig) {
        orig.setStatus(State.COMPLETE.toString());
        return null;
    }

    private String cancel (Booking input, Booking orig) {
        BigDecimal refund = RequestUtil.getQueryParamAsBigDecimal("refund");
        if (refund != null) {
            customerService.subtractFromBalance(orig.getCustomerId(), orig.getPrice());
        }

        orig.setStatus(State.CANCELED.toString());
        return null;
    }

    private String postpone (Booking input, Booking orig) {
        if(input.getEventDate() != null && input.getEventDate().after(orig.getEventDate()))
            orig.setEventDate(input.getEventDate());
        else
            orig.setEventDate(null);

        orig.setStatus(State.POSTPONED.toString());
        return null;
    }
}
