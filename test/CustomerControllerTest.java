import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;
import play.test.*;
import play.libs.ws.*;
import play.libs.ws.WS;
import play.Logger;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static play.test.Helpers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;


/**
 * Created by yael on 10/14/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerControllerTest extends WithServer {

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
    public void testCreateUpdateCustomer() throws Exception{
        Logger.info("CustomerControllerTest.testCreateCustomer");

        WSResponse response = GenerateRequest.createCustomer("Nicholas", "Hendricks", "nik@testmail.com", "3473473434", "cash", "55.79");
        JsonNode node =  response.asJson();
        Logger.info("testCreateUpdateCustomer created: " + node.toString());

        response = GenerateRequest.createCustomer("Renee", "Goldman", "reneeg@testmail.com", "3473473434", "cash", "55.79");
        node =  response.asJson();
        Logger.info("testCreateUpdateCustomer created: " + node.toString());

        Long id = node.findValue("id").asLong();
        response = GenerateRequest.updateCustomer(id, "Rina", "Goldman", "reneeg@testmail.com", "3473473434", "check", "5.79");
        node =  response.asJson();
        Logger.info("testCreateUpdateCustomer updated: " + node.toString());
    }
}
