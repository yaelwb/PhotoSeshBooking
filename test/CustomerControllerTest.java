import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import play.test.*;
import play.libs.ws.*;
import play.Logger;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import services.CustomerService;
import utils.GenerateBookingRequest;
import utils.GenerateCustomerRequest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * Created by yael on 10/14/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerControllerTest extends WithServer {

//    private EntityManagerFactory emFactory;
//    private EntityManager em;
//
//    @Inject
//    CustomerService customerService;

    @Before
    public void setUp() {
        GenerateBookingRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
//        derby:
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
//        derby:
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
    public void createCustomer() throws Exception{
        Logger.info("CustomerControllerTest.createCustomer");

        WSResponse response = GenerateCustomerRequest.createCustomer(null, "Hendricks", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(400, response.getStatus());
        assertEquals("A valid first name is a mandatory field.", response.getBody());

        response = GenerateCustomerRequest.createCustomer("Nicholas", "Hendricks", "nik.com", "3473473434", null, "50");
        assertEquals(400, response.getStatus());
        assertEquals("A valid email is a mandatory field.", response.getBody());

        response = GenerateCustomerRequest.createCustomer("Nicholas", "Hendricks", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(200, response.getStatus());
        JsonNode node =  response.asJson();
        String name = node.findValue("firstName").asText();
        assertEquals("Nicholas", name);
        String pay = node.findValue("payMethod").asText();
        assertEquals("Cash", pay);
    }

    @Test
    @play.db.jpa.Transactional
    public void getCustomer() throws Exception{
        Logger.info("CustomerControllerTest.getCustomer");

        WSResponse response = GenerateCustomerRequest.createCustomer("Nicholas", "Hendricks", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(200, response.getStatus());
        Long id1 = response.asJson().findValue("id").asLong();

        response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.createCustomer("Renee", "Green", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        Long id2 = response.asJson().findValue("id").asLong();

        response = GenerateCustomerRequest.createCustomer("Nicholas", "Green", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(200, response.getStatus());

        //get by id
        response = GenerateCustomerRequest.getCustomer(id1);
        String name1 = response.asJson().findValue("firstName").asText();
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.getCustomer(id2);
        String name2 = response.asJson().findValue("firstName").asText();
        assertEquals(200, response.getStatus());
        assertNotEquals(name1, name2);

        response = GenerateCustomerRequest.getCustomer(123L);
        assertEquals(200, response.getStatus());
        assertEquals("No such customers currently in the system.", response.getBody());


        //get by name
        response = GenerateCustomerRequest.getCustomer("Renee", null);
        assertEquals(200, response.getStatus());
        assertEquals(2, response.asJson().size());

        response = GenerateCustomerRequest.getCustomer("Renee", "Green");
        assertEquals(200, response.getStatus());
        assertEquals(1, response.asJson().size());

        response = GenerateCustomerRequest.getCustomer("", "Green");
        assertEquals(200, response.getStatus());
        assertEquals(2, response.asJson().size());

        response = GenerateCustomerRequest.getCustomer("Mike", "Green");
        assertEquals(200, response.getStatus());
        assertEquals("No such customers currently in the system.", response.getBody());

//        em.getTransaction().begin();
//
//        Customer c = new Customer("A", "B", "ab@testmail.com", "3473473434", null, new BigDecimal("500"));
//        em.persist(c);
//        //customerService.getAll();
//        System.out.println(em.createQuery("from CUSTOMER").executeUpdate());
//        em.getTransaction().commit();
    }

    @Test
    @play.db.jpa.Transactional
    public void getAllCustomers() throws Exception{
        Logger.info("CustomerControllerTest.getAllCustomers");
        WSResponse response = GenerateCustomerRequest.createCustomer("Nicholas", "Hendricks", "nik@testmail.com", "3473473434", null, "51");
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "check", "59.79");
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.createCustomer("Renee", "Green", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.createCustomer("Nicholas", "Green", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(200, response.getStatus());


        response = GenerateCustomerRequest.getAllCustomers(null);
        assertEquals(200, response.getStatus());
        assertEquals(4, response.asJson().size());

        Map<String, String[]> params = new HashMap<>();
        params.put("fromBalance", new String[] {"51"});
        params.put("payMethod", new String[] {"check"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        //TODO - debug criteria
        //assertEquals(1, response.asJson().size());
    }

    @Test
    @play.db.jpa.Transactional
    public void updateCustomer() throws Exception{
        Logger.info("CustomerControllerTest.updateCustomer");

        WSResponse response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        Long id = response.asJson().findValue("id").asLong();

        response = GenerateCustomerRequest.updateCustomer(id, "Rina", "Goldman", "reneeg@testmail.com", "3473473434", "check", "5.79");
        assertEquals(200, response.getStatus());

        response = GenerateCustomerRequest.getCustomer(id);
        JsonNode node = response.asJson();
        String name = node.findValue("firstName").asText();
        assertEquals(200, response.getStatus());
        assertEquals("Rina", name);
        assertEquals(new BigDecimal("5.79"), node.findValue("balance").decimalValue());
    }

    @Test
    @play.db.jpa.Transactional
    public void deleteCustomer() throws Exception{
        Logger.info("CustomerControllerTest.deleteCustomer");
        WSResponse response = GenerateCustomerRequest.createCustomer("Nicholas", "Hendricks", "nik@testmail.com", "3473473434", null, "51");
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "check", "59.79");
        assertEquals(200, response.getStatus());
        Long id1 = response.asJson().findValue("id").asLong();
        response = GenerateCustomerRequest.createCustomer("Renee", "Green", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        assertEquals(200, response.getStatus());
        response = GenerateCustomerRequest.createCustomer("Nicholas", "Green", "nik@testmail.com", "3473473434", null, "50");
        assertEquals(200, response.getStatus());

        response = GenerateCustomerRequest.getAllCustomers(null);
        assertEquals(200, response.getStatus());
        assertEquals(4, response.asJson().size());

        GenerateCustomerRequest.deleteCustomer(id1);
        response = GenerateCustomerRequest.getAllCustomers(null);
        assertEquals(200, response.getStatus());
        assertEquals(3, response.asJson().size());
    }
}
