package services;

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

    //TODO
    @Override
    public void update(Booking from, Booking to) {
        State toState = StatusUtil.getState(to.getStatus());
        switch(toState) {
            case BOOKED:
                booked(from, to);
                break;
            case DOWNPAYMENT:
                downpayment(from, to);
                break;
            case PREPARATION:
                preparation(from, to);
                break;
            case PHOTOSHOOT:
                photoshoot(from, to);
                break;
            case PAYMENT:
                payment(from, to);
                break;
            case SELECTIONS:
                selections(from, to);
                break;
            case EDITING:
                editing(from, to);
                break;
            case REVIEW:
                review(from, to);
                break;
            //no check or field update needed except for status update
            case COMPLETE:
                from.setStatus(State.COMPLETE.toString());
                break;
            case CANCELED:
                cancel(from, to);
                break;
            case POSTPONED:
                postpone(from, to);
                break;
        }
    }

    private void booked (Booking from, Booking to) {

    }

    private void downpayment (Booking from, Booking to) {

    }

    private void preparation (Booking from, Booking to) {

    }

    private void photoshoot (Booking from, Booking to) {

    }

    private void payment (Booking from, Booking to) {

    }

    private void selections (Booking from, Booking to) {

    }

    private void editing (Booking from, Booking to) {

    }

    private void review (Booking from, Booking to) {

    }

    private void cancel (Booking from, Booking to) {

    }

    private void postpone (Booking from, Booking to) {

    }
}
