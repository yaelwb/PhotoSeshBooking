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
    public void testCreateUpdateBooking() throws Exception {
        Logger.info("BookingControllerTest.testCreateUpdateBooking");

        WSResponse response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        JsonNode node = response.asJson();
        Logger.info("testCreateUpdateBooking created customer: " + node.toString());

        Long id = node.findValue("id").asLong();
        response = GenerateBookingRequest.createBooking(id);
        node = response.asJson();
        Logger.info("testCreateUpdateBooking created: " + node.toString());
        assertEquals(State.CREATED.toString(), node.findValue("status").asText());
    }
}