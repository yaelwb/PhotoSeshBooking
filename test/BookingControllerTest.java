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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.DriverManager;
import java.sql.SQLNonTransientConnectionException;

import static org.junit.Assert.*;


/**
 * Created by yael on 10/27/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class BookingControllerTest extends WithServer {

//    private EntityManagerFactory emFactory;
//    private EntityManager em;


    @Before
    public void setUp() {
        GenerateBookingRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
//        try {
//            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
//            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;create=true").close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            fail("Exception during database startup.");
//        }
//        try {
//            emFactory = Persistence.createEntityManagerFactory("testPU");
//            em = emFactory.createEntityManager();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            fail("Exception during JPA EntityManager instanciation.");
//        }
    }

    @After
    public void tearDown() throws Exception{
        GenerateBookingRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
//        if (em != null) {
//            em.close();
//        }
//        if (emFactory != null) {
//            emFactory.close();
//        }
//        try {
//            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;shutdown=true").close();
//        } catch (SQLNonTransientConnectionException ex) {
//            if (ex.getErrorCode() != 45000) {
//                throw ex;
//            }
//        }
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