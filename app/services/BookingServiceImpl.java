package services;

import models.Booking;
import play.Logger;
import play.db.jpa.JPA;
import utilities.RequestUtil;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
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

    //TODO
    @Override
    public void update(Booking from, Booking to) {

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
        String queryString = "from Booking";
        TypedQuery<Booking> query = JPA.em().createQuery(queryString, Booking.class);
        RequestUtil.paginate(query);
        List<Booking> l = query.getResultList();

        if (l == null || l.isEmpty())
            Logger.info("services.BookingService.getAll(): No bookings to show");
        else
            Logger.info("services.BookingService.getAll(): returned " + l.size() + " bookings.");
        return l;
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
}
