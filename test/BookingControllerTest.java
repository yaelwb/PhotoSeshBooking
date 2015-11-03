import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.State;
import models.Booking;
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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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

        IntStream.rangeClosed(1, 24).forEach(i -> {
            Long customerId = (i%4 == 0) ? customerId1 : customerId2;
            WSResponse createResponse = GenerateBookingRequest.createBooking(customerId);
            assertEquals(200, createResponse.getStatus());

            Long id = createResponse.asJson().findValue("id").asLong();
            java.util.Date date= new java.util.Date();
            Timestamp eventDate = new Timestamp(date.getTime());
            String[] locations = new String[] {"Palace of the Fine Arts", "Joshua Tree", "Cliff House"};
            String[] eventTypes = new String[] {"Underwater", "Modeling", "Nature", "Wedding", "Portrait"};
            BigDecimal price = new BigDecimal(Double.toString(251.3 + (-5)*i));

            createResponse = GenerateBookingRequest.updateBooked(id, customerId, eventDate,
                    locations[i%3], eventTypes[i%5], null, price, null, null);
            assertEquals(200, createResponse.getStatus());

            if(i%7 == 1) {
                createResponse = GenerateBookingRequest.updateCancel(id, customerId, null);
                assertEquals(200, createResponse.getStatus());
            }
            else {
                if (i % 2 == 0) {
                    createResponse = GenerateBookingRequest.updateDownpayment(id, customerId,
                            null, null, null, null, null, new BigDecimal("25"), null, null);
                    assertEquals(200, createResponse.getStatus());
                }
                if (i % 4 == 0) {
                    createResponse = GenerateBookingRequest.updatePreparation(id, customerId,
                            null, null, null, null, null, null, null);
                    assertEquals(200, createResponse.getStatus());

                    createResponse = GenerateBookingRequest.updatePhotoshoot(id, customerId,
                            null, null, null, null, null, null, 700);
                    assertEquals(200, createResponse.getStatus());
                }
            }
        });

        WSResponse response = GenerateBookingRequest.getAllBookings(null);
        JsonNode node = response.asJson();
        assertEquals(24, node.size());
        //Logger.info("BookingControllerTest.createBooking - all 24 bookings:\n" + node);


        Map<String, String[]> params = new HashMap<>();
        params.put("page", new String[]{"1"});
        response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        assertEquals(10, response.asJson().size());

        params.put("maxItems", new String[]{"7"});
        response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        assertEquals(7, response.asJson().size());


        //create several, update some, get all, filter by status, date from-to, price range
        //check pagination with/ without filters - add variation on event dates
        //delete all and get all - verify no bookings
        //implement criteria of customer id

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
        JsonNode node = response.asJson();
        assertEquals(1, node.size());
        assertEquals(id1.longValue(), node.findValue("id").asLong());

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

    private List<Booking> extractBookingList(WSResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Booking> bookings = new LinkedList<>();
        try {
            bookings = objectMapper
                    .reader()
                    .forType(new TypeReference<List<Booking>>() {})
                    .readValue(response.asJson());
        } catch(Exception ex) {
            fail("Could not parse response : " + ex.getMessage());
        }
        return bookings;
    }
}