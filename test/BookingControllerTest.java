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
    public void testCreateBooking() throws Exception {
        Logger.info("BookingControllerTest.testCreateBooking");

        WSResponse response = GenerateBookingRequest.createBooking(81L);
        assertEquals(400, response.getStatus());
        assertEquals("A valid customer id is a mandatory field. Received: 81", response.getBody());

        response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        JsonNode node = response.asJson();
        Long id = node.findValue("id").asLong();

        response = GenerateBookingRequest.createBooking(id);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.CREATED.toString(), node.findValue("status").asText());
        assertEquals(new BigDecimal("0"), node.findValue("price").decimalValue());
    }

}