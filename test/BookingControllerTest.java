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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
        Logger.info("BookingControllerTest.getAllBookings");
        Long customerId1 = createCustomer1();
        Long customerId2 = createCustomer2();

        Map<String, String[]> params = new HashMap<>();
        WSResponse response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        assertEquals("No such bookings currently in the system.", response.getBody());

        //generate 24 bookings with different customer ids, prices,
        //event types and locations, and update some to different statuses
        generateBookings(customerId1, customerId2);

        response = GenerateBookingRequest.getAllBookings(null);
        List<Booking> bookings = extractBookingList(response);
        assertEquals(24, bookings.size());

        //check pagination
        params.put("page", new String[]{"1"});
        response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        assertEquals(10, response.asJson().size());

        params.put("maxItems", new String[]{"7"});
        params.remove("page");
        params.put("page", new String[]{"4"});
        response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        assertEquals(3, response.asJson().size());

        //check sorting by price
        params.remove("page");
        params.put("page", new String[]{"1"});
        params.put("orderBy", new String[]{"price"});
        response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        bookings = extractBookingList(response);
        assertEquals(7, bookings.size());
        assertEquals(new BigDecimal("131.3"),bookings.get(0).getPrice());
        assertEquals(new BigDecimal("156.3"), bookings.get(5).getPrice());
        assertEquals(new BigDecimal("161.3"), bookings.get(6).getPrice());

        //check filter by customer id
        params.remove("page");
        params.put("customerId", new String[]{customerId2.toString()});
        response = GenerateBookingRequest.getAllBookings(params);
        bookings = extractBookingList(response);
        assertEquals(200, response.getStatus());
        assertEquals(18, bookings.size());

        params.put("fromPrice", new String[]{"150"});
        params.put("toPrice", new String[]{"200"});
        response = GenerateBookingRequest.getAllBookings(params);
        bookings = extractBookingList(response);
        assertEquals(200, response.getStatus());
        assertEquals(7, bookings.size());
        assertEquals(new BigDecimal("156.3"), bookings.get(0).getPrice());
        assertEquals(new BigDecimal("196.3"), bookings.get(6).getPrice());

        params.put("eventType", new String[]{"Portrait", "Wedding"});
        response = GenerateBookingRequest.getAllBookings(params);
        bookings = extractBookingList(response);
        assertEquals(200, response.getStatus());
        assertEquals(4, bookings.size());

        params.clear();
        params.put("status", new String[]{"DOWNPAYMENT", "CANCELED"});
        response = GenerateBookingRequest.getAllBookings(params);
        bookings = extractBookingList(response);
        assertEquals(200, response.getStatus());
        assertEquals(9, bookings.size());

        params.clear();
        params.put("customerId", new String[]{"8989"});
        response = GenerateBookingRequest.getAllBookings(params);
        assertEquals(200, response.getStatus());
        assertEquals("No such bookings currently in the system.", response.getBody());
        params.remove("customerId");

        //filter and sort by event dates
        params.put("eventDateFrom", new String[]{"02-12-2015%2010:35:42"});
        params.put("eventDateTo", new String[]{"02-04-2016%2018:40:42"});
        params.put("orderBy", new String[]{"eventDate"});
        response = GenerateBookingRequest.getAllBookings(params);
        bookings = extractBookingList(response);
        assertEquals(200, response.getStatus());
        assertEquals(12, bookings.size());
        assertEquals("2015-12-02 10:35:42.0", bookings.get(0).getEventDate().toString());
        assertEquals("2016-04-02 18:35:42.0", bookings.get(11).getEventDate().toString());

        //delete all and get all - verify no bookings
        response = GenerateBookingRequest.deleteAllBookings();
        assertEquals(200, response.getStatus());
        response = GenerateBookingRequest.getAllBookings(null);
        assertEquals(200, response.getStatus());
        assertEquals("No such bookings currently in the system.", response.getBody());
    }

    @Test
    @play.db.jpa.Transactional
    public void getBooking() throws Exception {
        Logger.info("BookingControllerTest.getBooking");
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
        Logger.info("BookingControllerTest.deleteBooking");
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
        Logger.info("BookingControllerTest.updateBooking");
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

    //create booking with different data for sorting and filtering checks
    //updates ned to take place in order to be able to check status filtering
    private void generateBookings(Long customerId1, Long customerId2) {
        IntStream.rangeClosed(1, 24).forEach(i -> {
            Long customerId = (i%4 == 0) ? customerId1 : customerId2;
            WSResponse createResponse = GenerateBookingRequest.createBooking(customerId);
            assertEquals(200, createResponse.getStatus());

            Long id = createResponse.asJson().findValue("id").asLong();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

            String[] dateStrings = new String[] {"02-12-2015 10:35:42", "02-11-2015 18:35:42",
                    "02-12-2015 18:00:42", "02-11-2015 18:00:42","02-04-2016 18:35:42", "03-04-2016 18:35:42",
                    "02-12-2015 10:35:42", "02-11-2015 8:35:42","02-04-2016 9:35:42", "03-04-2016 18:00:42"};
            Timestamp eventDate = null;

            try {
                String str = dateStrings[i%10];
                Date date = dateFormat.parse(str);
                eventDate = new Timestamp(date.getTime());
            } catch (ParseException e) {
                eventDate = new Timestamp(new java.util.Date().getTime());
            }

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