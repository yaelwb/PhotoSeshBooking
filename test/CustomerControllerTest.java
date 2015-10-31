import com.fasterxml.jackson.databind.JsonNode;
import enums.PayMethod;
import play.test.*;
import play.libs.ws.*;
import play.Logger;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import utils.GenerateBookingRequest;
import utils.GenerateCustomerRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;


/**
 * Created by yael on 10/14/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerControllerTest extends WithServer {

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
        assertEquals(PayMethod.CASH.toString(), pay);
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
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(3, response.asJson().size());

        params.put("payMethod", new String[] {PayMethod.CASH.toString()});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(2, response.asJson().size());

        params.clear();
        params.put("fromBalance", new String[] {"51.1"});
        params.put("toBalance", new String[] {"55.79"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(1, response.asJson().size());
    }


    @Test
    @play.db.jpa.Transactional
    public void getAllCustomersPaginated() throws Exception{
        Logger.info("CustomerControllerTest.getAllCustomersPaginated");
        String[] firstNames = new String[] {"Nicholas", "Renee", "Mike", "Danny"};
        String[] lastNames = new String[] {"Hendricks", "Goldman", "Green", "Baker", "Jones", "Lee"};
        String[] method = new String[] {null, PayMethod.CASH.name(), PayMethod.CHECK.name(), PayMethod.PAYPAL.name(), PayMethod.SQUARE.name()};

        IntStream.rangeClosed(1, 24).forEach(i -> {
            WSResponse createResponse = GenerateCustomerRequest.createCustomer(
                    firstNames[i%4], lastNames[i%5],
                    firstNames[i%4].charAt(0) + lastNames[i%5].charAt(0) + "@testmail.com",
                    "3473473434", method[i%5], Double.toString(51.3 + 5*i));
            assertEquals(200, createResponse.getStatus());
        });

        Map<String, String[]> params = new HashMap<>();
        WSResponse response = GenerateCustomerRequest.getAllCustomers(null);
        assertEquals(200, response.getStatus());
        assertEquals(24, response.asJson().size());

        params.put("page", new String[]{"1"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(10, response.asJson().size());

        params.put("payMethod", new String[] {"cash"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(9, response.asJson().size());
        params.remove("payMethod");

        params.put("payMethod", new String[] {"check"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(5, response.asJson().size());
        params.remove("payMethod");

        params.put("maxItems", new String[]{"7"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(7, response.asJson().size());

        params.remove("page");
        params.put("page", new String[]{"4"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals(3, response.asJson().size());

        params.remove("page");
        params.put("page", new String[]{"5"});
        response = GenerateCustomerRequest.getAllCustomers(params);
        assertEquals(200, response.getStatus());
        assertEquals("No customers currently in the system.", response.getBody());
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
