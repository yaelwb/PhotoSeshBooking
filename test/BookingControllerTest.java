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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

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
    public void updateBookingSuccessfully() throws Exception {
        Logger.info("BookingControllerTest.updateBookingSuccessfully");
        Long customerId = createCustomer1();
        assertCustomerBalance(customerId, "55.79");

        //create booking
        WSResponse response = GenerateBookingRequest.createBooking(customerId);
        assertEquals(200, response.getStatus());
        Long id = response.asJson().findValue("id").asLong();

        //can't move to payment state
        response = GenerateBookingRequest.updatePayment(id, customerId, new BigDecimal("200"));
        assertEquals(400, response.getStatus());
        response = GenerateBookingRequest.updatePhotoshoot(id, customerId, null, null, null, null, null, 0);
        assertEquals(400, response.getStatus());


        //update to booked
        Timestamp eventDate = new Timestamp(dateFormat.parse("02-12-2015 10:35:42").getTime());
        response = GenerateBookingRequest.updateBooked(id, customerId, eventDate,
                "Palace of the Fine Arts", "Modeling", null, new BigDecimal("500"),
                null, "Tango dress for retail photoshoot - " +
                        "make sure to capture each dress from front, back, side, and during movement");
        assertEquals(200, response.getStatus());
        assertCustomerBalance(customerId, "555.79");

        //update details
        response = GenerateBookingRequest.updateBooked(id, customerId, null,
                null, null, new BigDecimal("3"), null, "Svetlana, Denise, Bei, Fernanda", null);
        assertEquals(200, response.getStatus());
        assertEquals("Svetlana, Denise, Bei, Fernanda", response.asJson().findValue("keyAttendees").asText());

        //update to downpayment
        response = GenerateBookingRequest.updateDownpayment(id, customerId, null, null, null,
                null, null, new BigDecimal("50"), null, null);
        assertEquals(200, response.getStatus());
        assertEquals(State.DOWNPAYMENT.toString(), response.asJson().findValue("status").asText());
        assertCustomerBalance(customerId, "505.79");

        //pay some more
        response = GenerateBookingRequest.updateDownpayment(id, customerId, null, null, null,
                null, null, new BigDecimal("70"), null, null);
        assertEquals(200, response.getStatus());
        assertCustomerBalance(customerId, "435.79");

        //update to preparation
        response = GenerateBookingRequest.updatePreparation(id, customerId,
                new BigDecimal("6"), null, "Svetlana, Megan", null,
                "Canon EF 28-135mm lens",
                "fix saturation and white balance accordingly",
                "about 30 feet to the right from the podium");
        assertEquals(200, response.getStatus());
        JsonNode node = response.asJson();
        assertEquals("Svetlana, Megan", node.findValue("keyAttendees").asText());
        assertEquals(new BigDecimal("500.0"), node.findValue("price").decimalValue());


        response = GenerateBookingRequest.updatePreparation(id, customerId,
                null, new BigDecimal("600"), null, null,
                "Canon EF 28-135mm lens, tripod, flash, remote, strobe",
                null, null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.PREPARATION.toString(), node.findValue("status").asText());
        assertEquals("Canon EF 28-135mm lens, tripod, flash, remote, strobe", node.findValue("equipment").asText());
        assertEquals(new BigDecimal("600.0"), node.findValue("price").decimalValue());
        assertCustomerBalance(customerId, "535.79");

        //photoshoot
        response = GenerateBookingRequest.updatePhotoshoot(id, customerId,
                null, null, null, null, null, 800);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.PHOTOSHOOT.toString(), node.findValue("status").asText());
        assertEquals(800, node.findValue("numPics").asInt());

        //try cancel and postpone after the photoshoot.
        response = GenerateBookingRequest.updatePostpone(id, customerId, null);
        assertEquals(400, response.getStatus());
        response = GenerateBookingRequest.updateCancel(id, customerId, new BigDecimal("25"));
        assertEquals(400, response.getStatus());

        response = GenerateBookingRequest.updatePhotoshoot(id, customerId, null, null,
                "Tango dress for retail photoshoot - make sure to capture each dress from front, back, side, and during movement. Coordinate with Renee",
                "ISO=100, f stop=2.0, WB: temperature 7000, tint +1,", null, 900);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(900, node.findValue("numPics").asInt());

        //update to payment
        response = GenerateBookingRequest.updatePayment(id, customerId, new BigDecimal("200"));
        assertEquals(200, response.getStatus());
        assertCustomerBalance(customerId, "335.79");

        response = GenerateBookingRequest.updatePayment(id, customerId, new BigDecimal("200"));
        assertEquals(200, response.getStatus());
        assertCustomerBalance(customerId, "135.79");

        //update to selection - won't move from payment state without overriding the payment, as it is not fully paid
        response = GenerateBookingRequest.updateSelections(id, customerId, 30, 0, null, null);
        assertEquals(400, response.getStatus());

        response = GenerateBookingRequest.updateSelections(id, customerId, 30, 0, null, "true");
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.SELECTIONS.toString(), node.findValue("status").asText());
        assertEquals(30, node.findValue("numSelected").asInt());

        //no need to override once we already switched into selection mode
        response = GenerateBookingRequest.updateSelections(id, customerId, 587, 0, null, null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.SELECTIONS.toString(), node.findValue("status").asText());
        assertEquals(587, node.findValue("numSelected").asInt());

        //try to review before making edits
        response = GenerateBookingRequest.updateReview(id, customerId, 587, 0, null);
        assertEquals(400, response.getStatus());

        //edit
        response = GenerateBookingRequest.updateEditing(id, customerId, 587, 28, null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.EDITING.toString(), node.findValue("status").asText());
        assertEquals(28, node.findValue("numProcessed").asInt());

        //try to review before finishing edits
        response = GenerateBookingRequest.updateReview(id, customerId, 587, 28, null);
        assertEquals(400, response.getStatus());

        //override in order to get customer's feedback before editing almost 600 images
        response = GenerateBookingRequest.updateReview(id, customerId, 587, 28, "true");
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.REVIEW.toString(), node.findValue("status").asText());
        assertEquals(28, node.findValue("numProcessed").asInt());

        //back from review with a few new images selected, and most edits approved
        response = GenerateBookingRequest.updateSelections(id, customerId, 592, 23, "add exposure to some images, try to retouch background", null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.SELECTIONS.toString(), node.findValue("status").asText());
        assertEquals("add exposure to some images, try to retouch background", node.findValue("reviewNotes").asText());

        //edit some more
        response = GenerateBookingRequest.updateEditing(id, customerId, 592, 320, null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.EDITING.toString(), node.findValue("status").asText());
        assertEquals(320, node.findValue("numProcessed").asInt());

        response = GenerateBookingRequest.updateEditing(id, customerId, 592, 592, null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.EDITING.toString(), node.findValue("status").asText());
        assertEquals(592, node.findValue("numProcessed").asInt());

        //resend for review
        response = GenerateBookingRequest.updateReview(id, customerId, 592, 592, null);
        assertEquals(200, response.getStatus());
        node = response.asJson();
        assertEquals(State.REVIEW.toString(), node.findValue("status").asText());
        assertEquals(592, node.findValue("numProcessed").asInt());

        //all good, process complete
        response = GenerateBookingRequest.updateComplete(id, customerId);
        assertEquals(200, response.getStatus());
        assertEquals(State.COMPLETE.toString(), response.asJson().findValue("status").asText());

        //try cancel and postpone after the photoshoot.
        response = GenerateBookingRequest.updatePostpone(id, customerId, null);
        assertEquals(400, response.getStatus());
        response = GenerateBookingRequest.updateCancel(id, customerId, new BigDecimal("25"));
        assertEquals(400, response.getStatus());
    }


    @Test
    @play.db.jpa.Transactional
    public void updateBookingErrors() throws Exception {
        Logger.info("updateBookingErrors.updateBooking");
        Long customerId = createCustomer1();
        assertCustomerBalance(customerId, "55.79");

        //create two bookings - to see the customer's balance keeps being updated
        WSResponse response = GenerateBookingRequest.createBooking(customerId);
        assertEquals(200, response.getStatus());
        Long id1 = response.asJson().findValue("id").asLong();

        response = GenerateBookingRequest.createBooking(customerId);
        assertEquals(200, response.getStatus());
        Long id2 = response.asJson().findValue("id").asLong();

        //update to booked
        Timestamp eventDate1 = new Timestamp(dateFormat.parse("02-12-2015 10:35:42").getTime());
        Timestamp eventDate2 = new Timestamp(dateFormat.parse("07-12-2015 10:35:42").getTime());
        Timestamp eventDate3 = new Timestamp(dateFormat.parse("14-12-2015 10:35:42").getTime());

        response = GenerateBookingRequest.updateBooked(id1, customerId, eventDate1,
                "Palace of the Fine Arts", "Modeling", null, new BigDecimal("500"), null, null);
        assertEquals(200, response.getStatus());
        assertCustomerBalance(customerId, "555.79");

        response = GenerateBookingRequest.updateBooked(id2, customerId, eventDate2,
                "Cliff House", "Portrait", null, new BigDecimal("200"), null, null);
        assertEquals(200, response.getStatus());
        assertEquals(eventDate2.getTime(), response.asJson().findValue("eventDate").asLong());
        assertCustomerBalance(customerId, "755.79");

        //postpone and resume booking #2
        response = GenerateBookingRequest.updatePostpone(id2, customerId, null);
        assertEquals(200, response.getStatus());
        JsonNode node = response.asJson();
        assertEquals(State.POSTPONED.toString(), node.findValue("status").asText());
        assertEquals("null", node.findValue("eventDate").asText());

        //illegal state change
        response = GenerateBookingRequest.updateComplete(id2, customerId);
        assertEquals(400, response.getStatus());

        //new date is missing
        response = GenerateBookingRequest.updateDownpayment(id2, customerId, null, null, null,
                null, null, null, null, null);
        assertEquals(400, response.getStatus());
        assertCustomerBalance(customerId, "755.79");

        //new date
        response = GenerateBookingRequest.updateDownpayment(id2, customerId, eventDate3, null, null,
                null, null, new BigDecimal("50"), null, null);
        assertEquals(200, response.getStatus());
        assertEquals(State.DOWNPAYMENT.toString(), response.asJson().findValue("status").asText());
        assertCustomerBalance(customerId, "705.79");

        //cancel
        response = GenerateBookingRequest.updateCancel(id2, customerId, new BigDecimal("25"));
        assertEquals(200, response.getStatus());
        assertEquals(State.CANCELED.toString(), response.asJson().findValue("status").asText());
        assertCustomerBalance(customerId, "530.79");

        //can't go back from canceled
        response = GenerateBookingRequest.updateDownpayment(id2, customerId, eventDate3, null, null,
                null, null, new BigDecimal("50"), null, null);
        assertEquals(400, response.getStatus());

        //update first booking to downpayment and then cancel without refund
        response = GenerateBookingRequest.updateDownpayment(id1, customerId, null, null, null,
                null, null, new BigDecimal("100"), null, null);
        assertEquals(200, response.getStatus());
        assertEquals(State.DOWNPAYMENT.toString(), response.asJson().findValue("status").asText());
        assertCustomerBalance(customerId, "430.79");

        response = GenerateBookingRequest.updateCancel(id1, customerId, null);
        assertEquals(200, response.getStatus());
        assertEquals(State.CANCELED.toString(), response.asJson().findValue("status").asText());
        assertCustomerBalance(customerId, "30.79");
    }

    private void assertCustomerBalance(Long customerId, String expected) {
        WSResponse response = GenerateCustomerRequest.getCustomer(customerId);
        BigDecimal balance = response.asJson().findValue("balance").decimalValue();
        assertEquals(new BigDecimal(expected), balance);
    }

    private Long createCustomer1() {
        WSResponse response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        Long id = response.asJson().findValue("id").asLong();
        return id;
    }

    private Long createCustomer2() {
        WSResponse response = GenerateCustomerRequest.createCustomer("Nicholas", "Green", "nik@testmail.com", "3473473434", null, "80");
        assertEquals(200, response.getStatus());
        Long id = response.asJson().findValue("id").asLong();
        return id;
    }

    //create booking with different data for sorting and filtering checks
    //updates ned to take place in order to be able to check status filtering
    private void generateBookings(Long customerId1, Long customerId2) {
        IntStream.rangeClosed(1, 24).forEach(i -> {
            Long customerId = (i % 4 == 0) ? customerId1 : customerId2;
            WSResponse response = GenerateBookingRequest.createBooking(customerId);
            assertEquals(200, response.getStatus());
            Long id = response.asJson().findValue("id").asLong();

            String[] dateStrings = new String[]{"02-12-2015 10:35:42", "02-11-2015 18:35:42",
                    "02-12-2015 18:00:42", "02-11-2015 18:00:42", "02-04-2016 18:35:42", "03-04-2016 18:35:42",
                    "02-12-2015 10:35:42", "02-11-2015 8:35:42", "02-04-2016 9:35:42", "03-04-2016 18:00:42"};
            Timestamp eventDate = null;

            try {
                eventDate = new Timestamp(dateFormat.parse(dateStrings[i % 10]).getTime());
            } catch (ParseException e) {
                eventDate = new Timestamp(new java.util.Date().getTime());
            }

            String[] locations = new String[]{"Palace of the Fine Arts", "Joshua Tree", "Cliff House"};
            String[] eventTypes = new String[]{"Underwater", "Modeling", "Nature", "Wedding", "Portrait"};
            BigDecimal price = new BigDecimal(Double.toString(251.3 + (-5) * i));

            response = GenerateBookingRequest.updateBooked(id, customerId, eventDate,
                    locations[i % 3], eventTypes[i % 5], null, price, null, null);
            assertEquals(200, response.getStatus());

            if (i % 7 == 1) {
                response = GenerateBookingRequest.updateCancel(id, customerId, null);
                assertEquals(200, response.getStatus());
            } else {
                if (i % 2 == 0) {
                    response = GenerateBookingRequest.updateDownpayment(id, customerId,
                            null, null, null, null, null, new BigDecimal("25"), null, null);
                    assertEquals(200, response.getStatus());
                }
                if (i % 4 == 0) {
                    response = GenerateBookingRequest.updatePreparation(id, customerId,
                            null, null, null, null, null, null, null);
                    assertEquals(200, response.getStatus());

                    response = GenerateBookingRequest.updatePhotoshoot(id, customerId,
                            null, null, null, null, null, 700);
                    assertEquals(200, response.getStatus());
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