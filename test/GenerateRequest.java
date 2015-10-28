import com.fasterxml.jackson.databind.JsonNode;
import models.Booking;
import models.Customer;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.*;
import play.libs.ws.WS;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yael on 10/27/15.
 */
public class GenerateRequest {

    private static final int timeout = 10000;
    private static final String baseUrl = "http://localhost:9000";


    public static WSResponse createCustomer(String firstName, String lastName, String email, String phone,
                                            String payMethod, String balance) {

        Customer customer = new Customer(firstName, lastName, email, phone, payMethod, new BigDecimal(balance));
        JsonNode json = Json.toJson(customer);

        WSRequest request = WS.url(baseUrl + "/customers");
        WSRequest complexRequest = request.setHeader("Content-Type", "application/json")
                .setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.post(json);
        return responsePromise.get(timeout * 10);
    }


    public static WSResponse updateCustomer(Long id, String firstName, String lastName, String email, String phone,
                                            String payMethod, String balance) {

        Customer customer = new Customer(firstName, lastName, email, phone, payMethod, new BigDecimal(balance));
        customer.setId(id);
        JsonNode json = Json.toJson(customer);

        WSRequest request = WS.url(baseUrl + "/customers");
        WSRequest complexRequest = request.setHeader("Content-Type", "application/json")
                .setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.put(json);
        return responsePromise.get(timeout * 10);
    }

    public static WSResponse getCustomer(Long id) {

        WSRequest request = WS.url(baseUrl + "/customers/byId?id=" + id);
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.get();
        return responsePromise.get(timeout * 10);
    }

    public static WSResponse getCustomer(String firstName, String lastName) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("/customers/byName?");
        if(firstName != null) {
            url.append("first=").append(firstName);
            if(lastName != null)
                url.append("&");
        }
        if(lastName != null)
            url.append("last=").append(lastName);

        WSRequest request = WS.url(url.toString());
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.get();
        return responsePromise.get(timeout * 10);
    }

    public static WSResponse deleteCustomer(Long id) {

        WSRequest request = WS.url(baseUrl + "/customers?id=" + id);
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return responsePromise.get(timeout * 10);
    }

    public static WSResponse deleteAllCustomers() {

        WSRequest request = WS.url(baseUrl + "/customers/DeleteAll");
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return responsePromise.get(timeout * 10);
    }

    //bookings

    public static WSResponse createBooking(Long customerId) {
        Map<String,Object> params = new HashMap<>();
        params.put("customerId",customerId);
        JsonNode json = Json.toJson(params);
        Logger.info("createBooking with json: " + json);

        WSRequest request = WS.url(baseUrl + "/bookings");
        WSRequest complexRequest = request.setHeader("Content-Type", "application/json")
                .setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.post(json);
        return responsePromise.get(timeout * 10);
    }

    public static WSResponse deleteAllBookings() {

        WSRequest request = WS.url(baseUrl + "/bookings/DeleteAll");
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return  responsePromise.get(timeout * 10);
    }
}
