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
 * Created by yael on 10/28/15.
 */

public class GenerateBookingRequest {

    private static final int timeout = 100000;
    private static final String baseUrl = "http://localhost:9000";

    public static WSResponse createBooking(Long customerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerId", customerId);
        JsonNode json = Json.toJson(params);
        Logger.info("createBooking with json: " + json);

        WSRequest request = WS.url(baseUrl + "/bookings");
        WSRequest complexRequest = request.setHeader("Content-Type", "application/json")
                .setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.post(json);
        return responsePromise.get(timeout);
    }


    public static WSResponse getAllBookings() {

        WSRequest request = WS.url(baseUrl + "/bookings");
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.get();
        return responsePromise.get(timeout);
    }

    public static WSResponse getBooking(Long id) {

        WSRequest request = WS.url(baseUrl + "/bookings/byId?id=" + id);
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.get();
        return responsePromise.get(timeout);
    }

    public static WSResponse deleteBooking(Long id) {

        WSRequest request = WS.url(baseUrl + "/bookings?id=" + id);
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return responsePromise.get(timeout);
    }

    public static WSResponse deleteAllBookings() {

        WSRequest request = WS.url(baseUrl + "/bookings/DeleteAll");
        WSRequest complexRequest = request.setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.delete();
        return responsePromise.get(timeout);
    }
}
