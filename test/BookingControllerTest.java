import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import play.Logger;
import play.libs.ws.WSResponse;
import play.test.WithServer;
import static org.junit.Assert.*;


/**
 * Created by yael on 10/27/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class BookingControllerTest extends WithServer {

    @Before
    public void setUp() {
        GenerateRequest.deleteAllBookings();
        GenerateRequest.deleteAllCustomers();
    }

    @After
    public void tearDown() {
        GenerateRequest.deleteAllBookings();
        GenerateRequest.deleteAllCustomers();
    }

    @Test
    @play.db.jpa.Transactional
    public void testCreateUpdateBooking() throws Exception {
        Logger.info("BookingControllerTest.testCreateUpdateBooking");

        WSResponse response = GenerateRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        JsonNode node = response.asJson();
        Logger.info("testCreateUpdateBooking created customer: " + node.toString());

        Long id = node.findValue("id").asLong();
        response = GenerateRequest.createBooking(id);
        node = response.asJson();
        Logger.info("testCreateUpdateBooking created: " + node.toString());
//        System.out.println("testCreateUpdateBooking created booking: " + response.getBody());
    }
}