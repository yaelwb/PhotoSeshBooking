package utils;

import com.fasterxml.jackson.databind.JsonNode;
import enums.State;
import models.Booking;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.*;
import play.libs.ws.WS;

import java.math.BigDecimal;
import java.sql.Timestamp;
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

    public static WSResponse getAllBookings(Map<String, String[]> requestParams) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("/bookings");
        if(requestParams != null && !requestParams.isEmpty()) {
            url.append("?");
            requestParams.forEach((k, v) ->
                            url.append((v != null && v.length > 0) ? (k + "=" +  v[0] + "&") : "")
            );
            url.setLength(url.length() - 1);
        }

        WSRequest request = WS.url(url.toString());
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

    public static WSResponse updateBooking(Long id, Long customerId, String status, Timestamp eventDate,
                                           String location, String eventType, BigDecimal duration,
                                           BigDecimal price, BigDecimal amountPaid, String keyAttendees,
                                           String requirements, String equipment, String cameraSettings,
                                           String optimalLightingSpots, Integer numPics, Integer numSelected,
                                           Integer numProcessed, String reviewNotes,
                                           String overridePayment, String override, BigDecimal refund) {

        Booking booking = new Booking(customerId);
        booking.setId(id);
        booking.setStatus(status);
        if(eventDate != null || status.equals(State.POSTPONED.name()))
            booking.setEventDate(eventDate);
        if(location != null) booking.setLocation(location);
        if(eventType != null) booking.setEventType(eventType);
        if(duration != null) booking.setDuration(duration);
        if(price != null) booking.setPrice(price);
        if(amountPaid != null) booking.setAmountPaid(amountPaid);
        if(eventDate != null) booking.setKeyAttendees(keyAttendees);
        if(keyAttendees != null) booking.setRequirements(requirements);
        if(equipment != null) booking.setEquipment(equipment);
        if(cameraSettings != null) booking.setCameraSettings(cameraSettings);
        if(optimalLightingSpots != null) booking.setOptimalLightingSpots(optimalLightingSpots);
        if(numPics != null) booking.setNumPics(numPics);
        if(numSelected != null) booking.setNumSelected(numSelected);
        if(numProcessed != null) booking.setNumProcessed(numProcessed);
        if(reviewNotes != null) booking.setReviewNotes(reviewNotes);
        JsonNode json = Json.toJson(booking);

        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("/bookings");
        if(overridePayment != null && !overridePayment.isEmpty())
            url.append("?overridePayment=").append(overridePayment);
        else if(override != null && !override.isEmpty())
            url.append("?override=").append(override);
        else if(refund != null)
            url.append("?refund=").append(refund);

        WSRequest request = WS.url(url.toString());
        WSRequest complexRequest = request.setHeader("Content-Type", "application/json")
                .setHeader("X-AUTH-TOKEN", "WaimeaBay");

        F.Promise<WSResponse> responsePromise = complexRequest.put(json);
        return responsePromise.get(timeout);
    }

    public static WSResponse updateBooked(Long id, Long customerId, Timestamp eventDate,
                                          String location, String eventType, BigDecimal duration,
                                          BigDecimal price, String keyAttendees, String requirements) {

        return updateBooking(id, customerId, State.BOOKED.name(), eventDate, location, eventType,
                duration, price, null, keyAttendees, requirements, null, null, null,
                null, null, null, null, null, null, null);
    }

    public static WSResponse updateDownpayment(Long id, Long customerId, Timestamp eventDate,
                                               String location, String eventType, BigDecimal duration,
                                               BigDecimal price, BigDecimal amountPaid,
                                               String keyAttendees, String requirements) {

        return updateBooking(id, customerId, State.DOWNPAYMENT.name(), eventDate, location, eventType,
                duration, price, amountPaid, keyAttendees, requirements, null, null, null,
                null, null, null, null, null, null, null);
    }

    public static WSResponse updatePreparation(Long id, Long customerId, BigDecimal duration,
                                               BigDecimal price, String keyAttendees,
                                               String requirements, String equipment,
                                               String cameraSettings, String optimalLightingSpots) {

        return updateBooking(id, customerId, State.PREPARATION.name(), null, null, null,
                duration, price, null, keyAttendees, requirements, equipment, cameraSettings,
                optimalLightingSpots, null, null, null, null, null, null, null);
    }

    public static WSResponse updatePhotoshoot(Long id, Long customerId, BigDecimal duration,
                                              BigDecimal price, String keyAttendees,
                                              String requirements, String cameraSettings,
                                              String optimalLightingSpots, int numPics) {

        return updateBooking(id, customerId, State.PHOTOSHOOT.name(), null, null, null,
                duration, price, null, keyAttendees, requirements, null, cameraSettings,
                optimalLightingSpots, numPics, null, null, null, null, null, null);
    }

    public static WSResponse updatePayment(Long id, Long customerId, BigDecimal amountPaid) {

        return updateBooking(id, customerId, State.PAYMENT.name(), null, null, null, null, null,
                amountPaid, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static WSResponse updateSelections(Long id, Long customerId, int numSelected,
                                              int numProcessed, String reviewNotes,
                                              String overridePayment) {

        return updateBooking(id, customerId, State.SELECTIONS.name(), null, null, null, null,
                null, null, null, null, null, null, null, null, numSelected, numProcessed,
                reviewNotes, overridePayment, null, null);
    }

    public static WSResponse updateEditing(Long id, Long customerId, int numSelected,
                                              int numProcessed, String reviewNotes) {

        return updateBooking(id, customerId, State.EDITING.name(), null, null, null, null,
                null, null, null, null, null, null, null, null, numSelected, numProcessed,
                reviewNotes, null, null, null);
    }

    public static WSResponse updateReview(Long id, Long customerId, int numSelected,
                                          int numProcessed, String override) {

        return updateBooking(id, customerId, State.REVIEW.name(), null, null, null, null,
                null, null, null, null, null, null, null, null, numSelected, numProcessed,
                null, null, override, null);
    }

    public static WSResponse updateComplete(Long id, Long customerId) {

        return updateBooking(id, customerId, State.COMPLETE.name(), null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static WSResponse updateCancel(Long id, Long customerId, BigDecimal refund) {

        return updateBooking(id, customerId, State.CANCELED.name(), null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, refund);
    }

    public static WSResponse updatePostpone(Long id, Long customerId, Timestamp eventDate) {

        return updateBooking(id, customerId, State.POSTPONED.name(), eventDate, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
