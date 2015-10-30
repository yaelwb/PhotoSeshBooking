package utils;

import com.fasterxml.jackson.databind.JsonNode;
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
public class GenerateCustomerRequest {

    private static final int timeout = 100000;
    private static final String baseUrl = "http://localhost:9000";


    public static WSResponse createCustomer(String firstName, String lastName, String email, String phone,
                                            String payMethod, String balance) {

        Customer customer = new Customer(firstName, lastName, email, phone, payMethod, new BigDecimal(balance));
        JsonNode json = Json.toJson(customer);

        WSRequest request = WS.url(baseUrl + "/customers");
        WSRequest complexRequest = request.setHeader("Content-Type", "application/json")
                .setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.post(json);
        return responsePromise.get(timeout);
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
        return responsePromise.get(timeout);
    }

    public static WSResponse getAllCustomers(Map<String, String[]> requestParams) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("/customers");
        if(requestParams != null && !requestParams.isEmpty()) {
            url.append("?");
            requestParams.forEach((k, v) ->
                            url.append(k + "=" + v + "&")
            );
            url.setLength(url.length() - 1);
        }

        WSRequest request = WS.url(url.toString());
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.get();
        return responsePromise.get(timeout);
    }

    public static WSResponse getCustomer(Long id) {

        WSRequest request = WS.url(baseUrl + "/customers/byId?id=" + id);
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.get();
        return responsePromise.get(timeout);
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
        return responsePromise.get(timeout);
    }

    public static WSResponse deleteCustomer(Long id) {

        WSRequest request = WS.url(baseUrl + "/customers?id=" + id);
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return responsePromise.get(timeout);
    }

    public static WSResponse deleteAllCustomers() {

        WSRequest request = WS.url(baseUrl + "/customers/DeleteAll");
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return responsePromise.get(timeout);
    }
}
