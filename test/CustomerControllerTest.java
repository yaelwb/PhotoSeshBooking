import com.fasterxml.jackson.databind.JsonNode;
import play.test.*;
import play.libs.ws.*;
import play.Logger;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import utils.GenerateCustomerRequest;


/**
 * Created by yael on 10/14/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerControllerTest extends WithServer {

    @Before
    public void setUp() {
        GenerateCustomerRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
    }

    @After
    public void tearDown() {
        GenerateCustomerRequest.deleteAllBookings();
        GenerateCustomerRequest.deleteAllCustomers();
    }

    @Test
    @play.db.jpa.Transactional
    public void testCreateUpdateCustomer() throws Exception{
        Logger.info("CustomerControllerTest.testCreateCustomer");

        WSResponse response = GenerateCustomerRequest.createCustomer("Nicholas", "Hendricks", "nik@testmail.com", "3473473434", "cash", "55.79");
        JsonNode node =  response.asJson();
        Logger.info("testCreateUpdateCustomer created: " + node.toString());

        response = GenerateCustomerRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        node =  response.asJson();
        Logger.info("testCreateUpdateCustomer created: " + node.toString());

        Long id = node.findValue("id").asLong();
        response = GenerateCustomerRequest.updateCustomer(id, "Rina", "Goldman", "reneeg@testmail.com", "3473473434", "check", "5.79");
        node =  response.asJson();
        Logger.info("testCreateUpdateCustomer updated: " + node.toString());
    }
}
