package controllers;

import com.google.inject.Inject;
import enums.State;
import models.Booking;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import services.BookingService;
import services.CustomerService;
import utilities.ActionAuthenticator;
import utilities.Predicates;
import utilities.RequestUtil;
import utilities.StatusUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yael on 10/13/15.
 */
public class BookingController extends Controller {

    private final CustomerService customerService;

    private final BookingService bookingService;

    @Inject
    public BookingController(CustomerService customerService, BookingService bookingService) {
        this.customerService = customerService;
        this.bookingService = bookingService;
    }

    /** create: Persist a new booking to the database.
     * PUT request to /bookings supplying a Json representation of the new booking.
     * @return Result: The new booking or an error message if customer id is invalid.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result create() {
        Booking inputBooking = Json.fromJson(request().body().asJson(), Booking.class);
        Booking booking = bookingService.create(inputBooking, customerService);
        if(booking == null)
            return badRequest("A valid customer id is a mandatory field.");

        return ok(Json.toJson(booking.toString()));
    }

    /** getAll: Show all bookings in the database. Supports pagination.
     * GET request to /bookings.
     * @return Result: A Json representation of existing bookings.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getAll() {
        List<Booking> l = bookingService.getAll();

        if (l == null || l.isEmpty()) {
            return ok("No bookings currently in the system.");
        }
        return ok(Json.toJson(l));
    }

    /** getById: Finds and shows a booking in the database by booking's id. Supports pagination.
     * GET request to /booking/id with id as a parameter.
     * @return Result: A Json representation of the requested booking, if exists.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getById() {
        Long id = RequestUtil.getQueryParamAsLong("id");

        if (id == null) {
            Logger.error("controllers.BookingController.getById(): No id parameter");
            return badRequest("Please provide an id as a parameter");
        }

        Booking booking = bookingService.get(id);
        if (booking == null ) {
            return ok("No such bookings currently in the system.");
        }
        return ok(Json.toJson(booking));
    }

    /** getByNumTodo: Finds and shows a bookings that have a specified number of images to process.
     * GET request to /booking/getByNumTodo with from, to as parameters.
     * @return Result: A Json representation of the requested bookings, if exist.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getByNumTodo() {
        Integer from = RequestUtil.getQueryParamAsInt("from");
        Integer to = RequestUtil.getQueryParamAsInt("to");

        if (from == null && to == null) {
            Logger.error("controllers.BookingController.getByNumTodo(): No parameters");
            return badRequest("Please provide from/to parameters");
        }

        List<Booking> l = bookingService.getAll();
        if(from != null)
            l.stream().filter(Predicates.imagesToProcessGreaterOrEqual(from)).collect(Collectors.toList());
        if(to != null)
            l.stream().filter(Predicates.imagesToProcessLessOrEqual(to)).collect(Collectors.toList());

        if (l.isEmpty() ) {
            return ok("No such bookings currently in the system.");
        }
        return ok(Json.toJson(l));
    }

    /** update: Update an existing booking. Only valid fields are copied.
     * POST request to /bookings supplying a Json representation of the updated booking.
     * @return Result: The updated booking.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result update() {
        Booking updatedBooking = Json.fromJson(request().body().asJson(), Booking.class);
        Booking booking = bookingService.get(updatedBooking.getId());
        if (booking == null) {
            return ok("No such booking currently in the system.");
        }

        if(!StatusUtil.stateChangeExists(booking.getStatus(), updatedBooking.getStatus())) {
            Logger.error("controllers.BookingController.update(): can't switch to next state");
            return badRequest("No path from state " + booking.getStatus() + " to state " + updatedBooking.getStatus());
        }
        bookingService.update(updatedBooking, booking);
        return ok(Json.toJson(booking));
    }

    /** delete: Deletes an existing booking from the database by booking's id.
     * DELETE request to /bookings with id as a parameter.
     * @return Result: A status message for deleting the booking, or no such booking. An error if no parameter.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result delete() {
        Long id = RequestUtil.getQueryParamAsLong("id");
        if (id == null) {
            Logger.error("controllers.BookingController.delete(): No id parameter");
            return badRequest("Please provide an id as a parameter");
        }

        if (bookingService.delete(id) == 0)
            return ok("No such booking currently in the system.");
        return ok("Deleted booking with id " + id);
    }

}
