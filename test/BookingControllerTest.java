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

        Long customerId = createCustomer1();
        response = GenerateBookingRequest.createBooking(customerId);
        assertEquals(200, response.getStatus());
        JsonNode node = response.asJson();
        assertEquals(State.CREATED.toString(), node.findValue("status").asText());
        assertEquals(new BigDecimal("0"), node.findValue("price").decimalValue());
    }

    @Test
    @play.db.jpa.Transactional
    public void getAllBookings() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        Long customerId1 = createCustomer1();
        Long customerId2 = createCustomer2();
        //create several, update some, get all, filter by status, date from-to, price range
        //check pagination with/ without filters
        //delete all and get all - verify no bookings

    }

    @Test
    @play.db.jpa.Transactional
    public void getBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        Long customerId1 = createCustomer1();
        Long customerId2 = createCustomer2();

        WSResponse response = GenerateBookingRequest.createBooking(customerId1);
        assertEquals(200, response.getStatus());
        Long id1 = response.asJson().findValue("id").asLong();

        response = GenerateBookingRequest.createBooking(customerId2);
        assertEquals(200, response.getStatus());
        Long id2 = response.asJson().findValue("id").asLong();

        response = GenerateBookingRequest.updateCancel(id2, customerId2, null);
        assertEquals(200, response.getStatus());

        response = GenerateBookingRequest.getBooking(id1);
        assertEquals(200, response.getStatus());
        assertEquals(State.CREATED.toString(), response.asJson().findValue("status").asText());

        response = GenerateBookingRequest.getBooking(id2);
        assertEquals(200, response.getStatus());
        assertEquals(State.CANCELED.toString(), response.asJson().findValue("status").asText());

        response = GenerateBookingRequest.getBooking(12L);
        assertEquals(200, response.getStatus());
        assertEquals("No such bookings currently in the system.", response.getBody());
    }

    @Test
    @play.db.jpa.Transactional
    public void deleteBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        Long customerId1 = createCustomer1();
        Long customerId2 = createCustomer2();

        WSResponse response = GenerateBookingRequest.createBooking(customerId1);
        assertEquals(200, response.getStatus());
        Long id1 = response.asJson().findValue("id").asLong();

        response = GenerateBookingRequest.createBooking(customerId2);
        assertEquals(200, response.getStatus());
        Long id2 = response.asJson().findValue("id").asLong();

        response = GenerateBookingRequest.getAllBookings(null);
        assertEquals(200, response.getStatus());
        assertEquals(2, response.asJson().size());

        response = GenerateBookingRequest.deleteBooking(id2);
        assertEquals(200, response.getStatus());

        response = GenerateBookingRequest.getAllBookings(null);
        assertEquals(200, response.getStatus());
        assertEquals(1, response.asJson().size());

        response = GenerateBookingRequest.deleteBooking(12L);
        assertEquals(400, response.getStatus());

        response = GenerateBookingRequest.deleteBooking(id2);
        assertEquals(400, response.getStatus());
    }

    @Test
    @play.db.jpa.Transactional
    public void updateBooking() throws Exception {
        Logger.info("BookingControllerTest.createBooking");
        Long customerId1 = createCustomer1();
        Long customerId2 = createCustomer2();

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