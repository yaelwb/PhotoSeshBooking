package services;

import com.google.inject.ImplementedBy;
import models.Booking;

import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
@ImplementedBy(BookingServiceImpl.class)
public interface BookingService {
    Booking create(Booking inputBooking, CustomerService customerService);
    void update(Booking from, Booking to);
    int delete(Long id);

    List<Booking> getAll();
    Booking get(Long id);
}
