package controllers;
import com.google.inject.Inject;
import models.Customer;

import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import play.mvc.Security;
import services.CustomerService;
import utilities.ActionAuthenticator;
import utilities.Parse;
import utilities.RequestUtil;
import views.html.createCustomer;
import views.html.customers;

/**
 * Created by yael on 9/28/15.
 */
public class CustomerController extends Controller {

    private final CustomerService customerService;

    @Inject
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** create: Persist a new customer to the database.
     * PUT request to /customers supplying a Json representation of the new customer.
     * @return Result: The new customer or an error message if any field is invalid.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result create() {
        Customer inputCustomer = Json.fromJson(request().body().asJson(), Customer.class);
        String error = customerService.validate(inputCustomer);
        if(error != null)
            return badRequest(error);

        Customer customer = customerService.create(inputCustomer);
        if(customer == null)
            return badRequest("Error creating a new customer");

        return ok(Json.toJson(customer));
    }

    /** getAll: Show all customers in the database. Supports pagination.
     * GET request to /customers.
     * @return Result: A Json representation of existing customers.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getAll() {
        List<Customer> l = customerService.getAll();

        if (l == null || l.isEmpty()) {
            return ok("No customers currently in the system.");
        }
        return ok(Json.toJson(l));
    }

    /** getById: Finds and shows a customer in the database by customer's id. Supports pagination.
     * GET request to /customer/id with id as a parameter.
     * @return Result: A Json representation of the requested customer, if exists.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getById() {
        Long id = RequestUtil.getQueryParamAsLong("id");

        if (id == null) {
            Logger.error("controllers.CustomerController.getById(): No id parameter");
            return badRequest("Please provide an id as a parameter");
        }

        Customer customer = customerService.get(id);
        if (customer == null ) {
            return ok("No such customers currently in the system.");
        }
        return ok(Json.toJson(customer));
    }

    /** getByName: Finds and show customers in the database by name. Supports pagination.
     * GET request to /customers/findName with first and last names as parameters.
     * @return Result: A Json representation of the requested customers, if such exists.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getByName() {
        String first = RequestUtil.getQueryParam("first");
        String last = RequestUtil.getQueryParam("last");

        if ((first == null || !Parse.isNameValid(first)) && (last == null || !Parse.isNameValid(last))) {
            Logger.error("controllers.CustomerController.getByName(): Invalid or missing name parameters. first: "
                    + first + ", last: " + last);
            return badRequest("Please provide a name as a parameter");
        }

        List l = customerService.getByName(first, last);
        if (l == null || l.isEmpty()) {
            return ok("No such customers currently in the system.");
        }
        return ok(Json.toJson(l));
    }

    /** update: Update an existing customer. Only valid fields are copied.
     * POST request to /customers supplying a Json representation of the updated customer.
     * @return Result: The updated customer.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result update() {
        Customer updatedCustomer = Json.fromJson(request().body().asJson(), Customer.class);
        Customer customer = customerService.get(updatedCustomer.getId());
        if (customer == null) {
            return badRequest("No such customer currently in the system.");
        }
        customerService.update(updatedCustomer, customer);
        return ok(Json.toJson(customer));
    }

    /** delete: Deletes an existing customer from the database by customer's id.
     * DELETE request to /customers with id as a parameter.
     * @return Result: A status message for deleting the customer, or no such customer. An error if no parameter.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result delete() {
        Long id = RequestUtil.getQueryParamAsLong("id");
        if (id == null) {
            Logger.error("controllers.CustomerController.delete(): No id parameter");
            return badRequest("Please provide an id as a parameter");
        }

        if (customerService.delete(id) == 0)
            return badRequest("No such customer currently in the system.");
        return ok("Deleted customer with id " + id);
    }

    /** deleteAll: Deletes all existing customers from the database.
     * DELETE request to /customers/DeleteAll.
     * @return Result: A status message for deleting all customers, or no existing customers. An error if no parameter.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result deleteAll() {
        int count = customerService.deleteAll();
        if (count == 0)
            return ok("No customers currently in the system.");
        return ok("Deleted all " + count + " customers");
    }


    @Transactional
//    @Security.Authenticated(ActionAuthenticator.class)
    public Result dashGetAll() {
        List<Customer> l = customerService.getAll();

        if (l == null) {
            return ok("No customers currently in the system.");
        }
        return ok(customers.render(l));
    }

    @Transactional
//    @Security.Authenticated(ActionAuthenticator.class)
    public Result dashCreateCustomer() {
        return ok(createCustomer.render());
    }
}
