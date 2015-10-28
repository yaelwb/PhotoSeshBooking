package services;

import com.google.inject.ImplementedBy;
import models.Booking;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
@Service
@ImplementedBy(BookingServiceImpl.class)
public interface BookingService {
    Booking create(Booking inputBooking, CustomerService customerService);
    String update(Booking from, Booking to);
    int delete(Long id);

    List<Booking> getAll();
    Booking get(Long id);
}
