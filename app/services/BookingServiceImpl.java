package services;

import com.google.inject.Inject;
import enums.State;
import models.Booking;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import play.Logger;
import play.db.jpa.JPA;
import utilities.RequestUtil;
import utilities.StatusUtil;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
@Service
public class BookingServiceImpl implements BookingService {

    private final CustomerService customerService;
    private MathContext mc = new MathContext(2); // 2 precision

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
        booking.setPrice(new BigDecimal("0"));
        booking.setAmountPaid(new BigDecimal("0"));
        booking.setStatusId(StatusUtil.getStatusId(State.CREATED.name()));

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
    public int deleteAll() {
        Query query = JPA.em().createQuery("DELETE Booking");
        int res = query.executeUpdate();
        if (res == 0)
            Logger.info("services.BookingService.deleteAll(): no bookings found");
        else
            Logger.info("services.BookingService.deleteAll(): deleted all " + res + " Bookings");
        return res;
    }

    @Override
    public List<Booking> getAll() {
        Session session = JPA.em().unwrap(Session.class);
        Criteria cr = session.createCriteria(Booking.class);

        //allow filtering by several statuses
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
        Booking booking = null;
        if(id != null)
            booking = JPA.em().find(Booking.class, id);
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

    //date, location, type - might be updated after initially set.
    private void updateBasicInfo(Booking input, Booking orig) {
        if(input.getEventDate() != null && input.getEventDate() != orig.getEventDate())
            orig.setEventDate(input.getEventDate());

        if(input.getLocation() != null && !input.getLocation().equals(orig.getLocation()))
            orig.setLocation(input.getLocation());

        if(input.getEventType() != null && !input.getEventType().equals(orig.getEventType()))
            orig.setEventType(input.getEventType());
    }

    //customer additional info - duration, key attendees, requirement. can be updated multiple times
    private void updateAdditionalInfo(Booking input, Booking orig) {
        if(input.getDuration() != null && !input.getDuration().equals(orig.getDuration()))
            orig.setDuration(input.getDuration());

        if(input.getKeyAttendees() != null && !input.getKeyAttendees().equals(orig.getKeyAttendees()))
            orig.setKeyAttendees(input.getKeyAttendees());

        if(input.getRequirements() != null && !input.getRequirements().equals(orig.getRequirements()))
            orig.setRequirements(input.getRequirements());
    }

    private String booked (Booking input, Booking orig) {
        State fromState = StatusUtil.getState(orig.getStatus());

        //check the required data either exists or is being set
        if(fromState == State.CREATED) {
            if (input.getEventDate() == null && orig.getEventDate() == null)
                return "You must set a date for the event to be booked.";
            if (input.getLocation() == null && orig.getLocation() == null)
                return "You must set a location for the event to be booked.";
            if (input.getEventType() == null && orig.getEventType() == null)
                return "You must set an event type for the event to be booked.";
            if (input.getPrice() == null && orig.getPrice() == null)
                return "You must set a price for the event to be booked.";
            orig.setAmountPaid(new BigDecimal(0.0));
        }

        //All good, update the db entry
        updateBasicInfo(input, orig);

        //price is set. paid amount is not modified - we have downpayment state for that
        orig.setPrice(input.getPrice());
        customerService.addToBalance(orig.getCustomerId(), input.getPrice());

        //additional info might be updated
        updateAdditionalInfo(input, orig);

        orig.setStatus(State.BOOKED.toString());
        return null;
    }

    private String downpayment (Booking input, Booking orig) {
        BigDecimal paid = input.getAmountPaid().subtract(orig.getAmountPaid(), mc);
        customerService.subtractFromBalance(orig.getCustomerId(), paid);
        orig.setAmountPaid(input.getAmountPaid());

        //some info might be updated
        updateBasicInfo(input, orig);
        updateAdditionalInfo(input, orig);

        //price still might be negotiated and changed during downpayment and preparation states
        if(input.getPrice() != null && !input.getPrice().equals(orig.getPrice())) {
            BigDecimal diff = input.getPrice().subtract(orig.getPrice(), mc);
            customerService.addToBalance(orig.getCustomerId(), diff);
            orig.setPrice(input.getPrice());
        }

        orig.setStatus(State.DOWNPAYMENT.toString());
        return null;
    }

    private String preparation (Booking input, Booking orig) {
        //basic info can no longer be updated. however additional info such as requirements can be added
        updateAdditionalInfo(input, orig);

        //photographer's info is added
        if(input.getEquipment() != null)
            orig.setEquipment(input.getEquipment());
        if(input.getCameraSettings() != null)
            orig.setCameraSettings(input.getCameraSettings());
        if(input.getOptimalLightingSpots() != null)
            orig.setOptimalLightingSpots(input.getOptimalLightingSpots());

        //last chance for price to be negotiated and changed
        if(input.getPrice() != null && !input.getPrice().equals(orig.getPrice())) {
            BigDecimal diff = input.getPrice().subtract(orig.getPrice(), mc);
            customerService.addToBalance(orig.getCustomerId(), diff);
            orig.setPrice(input.getPrice());
        }

        orig.setStatus(State.PREPARATION.toString());
        return null;
    }

    private String photoshoot (Booking input, Booking orig) {
        //last minute additional info such as key attendees can be added
        updateAdditionalInfo(input, orig);

        //camera settings might be tweaked. register new optimal lighting spots fro future reference
        if(input.getCameraSettings() != null)
            orig.setCameraSettings(input.getCameraSettings());
        if(input.getOptimalLightingSpots() != null)
            orig.setOptimalLightingSpots(input.getOptimalLightingSpots());

        orig.setNumPics(input.getNumPics());

        orig.setStatus(State.PHOTOSHOOT.toString());
        return null;
    }

    private String payment (Booking input, Booking orig) {
        BigDecimal paid = input.getAmountPaid().subtract(orig.getAmountPaid(), mc);
        customerService.subtractFromBalance(orig.getCustomerId(), paid);
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
                        return "Amount must be paid in full before selections are made. Override option is available";
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
        orig.setNumSelected(input.getNumSelected());
        orig.setNumProcessed(input.getNumProcessed());

        String override = RequestUtil.getQueryParam("override");
        int numTodo = input.getNumSelected() - input.getNumProcessed();
        if(numTodo > 0) {
            if (override == null || override.equals("false")) {
                return "There are " + numTodo + " unprocessed images. You must override in order to send to partial review.";
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
            customerService.subtractFromBalance(orig.getCustomerId(), refund);
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
