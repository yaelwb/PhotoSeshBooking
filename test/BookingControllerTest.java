import com.fasterxml.jackson.databind.JsonNode;
import enums.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import play.Logger;
import play.libs.ws.WSResponse;
import play.test.WithServer;
import utils.GenerateBookingRequest;
import utils.GenerateCustomerRequest;

import java.math.BigDecimal;

import static org.junit.Assert.*;


/**
 * Created by yael on 10/27/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class BookingControllerTest extends WithServer {

    @Before
    public void setUp() {
        GenerateBookingRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
    }

    @After
    public void tearDown() throws Exception{
        GenerateBookingRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
    }

    @Test
    @play.db.jpa.Transactional
    public void createBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");

        WSResponse response = GenerateBookingRequest.createBooking(81L);
        assertEquals(400, response.getStatus());
        assertEquals("A valid customer id is a mandatory field. Received: 81", response.getBody());

        Long id = createCustomer1();
        response = GenerateBookingRequest.createBooking(id);
        assertEquals(200, response.getStatus());
        JsonNode node = response.asJson();
        assertEquals(State.CREATED.toString(), node.findValue("status").asText());
        assertEquals(new BigDecimal("0"), node.findValue("price").decimalValue());
    }

    @Test
    @play.db.jpa.Transactional
    public void getAllBookings() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        createCustomer1();
        createCustomer2();
        //create several, update some, get all, filter by status, date from-to, price range
        //check pagination with/ without filters
        //delete all and get all - verify no bookings

    }

    @Test
    @play.db.jpa.Transactional
    public void getBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        createCustomer1();
        createCustomer2();

        //create 2, get by id and validate
        //get by invalid id
    }

    @Test
    @play.db.jpa.Transactional
    public void deleteBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        createCustomer1();
        createCustomer2();

        //make 2 bookings
        //view - count 2
        //delete booking
        //delete invalid booking
        //view - count 1
    }

    @Test
    @play.db.jpa.Transactional
    public void updateBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        createCustomer1();
        createCustomer2();

        //make 2 bookings
        //start updating both, but postpone and resume the second
        //cancel the second with refund. check the customer balance being updated
        //check for invalid status changes
        //check that get all with filter by status works
        //try cancel and postpone after the photoshoot.
    }

    private Long createCustomer1() {
        WSResponse response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        Long id = response.asJson().findValue("id").asLong();
        return id;
    }

    private Long createCustomer2() {
        WSResponse response = GenerateCustomerRequest.createCustomer("Nicholas", "Green", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(200, response.getStatus());
        Long id = response.asJson().findValue("id").asLong();
        return id;
    }
}