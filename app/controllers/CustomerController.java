package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Customer;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.math.BigDecimal;
import java.util.List;

import play.mvc.Security;
import services.CustomerService;
import utilities.ActionAuthenticator;
import utilities.Parse;
import utilities.RequestUtil;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

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
     * @return Result: The new customer or an error message is any field is invalid.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result create() {
        JsonNode json = request().body().asJson();
        Customer inputCustomer = Json.fromJson(json, Customer.class);
        if (inputCustomer.getFirstName() == null || inputCustomer.getFirstName().isEmpty() ||
                !Parse.isNameValid(inputCustomer.getFirstName())) {
            Logger.error("controllers.CustomerController.create(): First name missing/invalid");
            return badRequest("A valid first name is a mandatory field.");
        }
        if (inputCustomer.getLastName() == null || inputCustomer.getLastName().isEmpty()||
                !Parse.isNameValid(inputCustomer.getLastName())) {
            Logger.error("controllers.CustomerController.create(): Last name missing/invalid");
            return badRequest("A valid last name is a mandatory field.");
        }
        if (inputCustomer.getEmail() == null || inputCustomer.getEmail().isEmpty()||
                !Parse.isEmailValid(inputCustomer.getEmail())) {
            Logger.error("controllers.CustomerController.create(): Email missing/invalid");
            return badRequest("A valid email is a mandatory field.");
        }
        if(inputCustomer.getPhone() == null || inputCustomer.getPhone().isEmpty() ||
                !Parse.isPhoneNumberValid(inputCustomer.getPhone())) {
            Logger.error("controllers.CustomerController.create(): Phone missing/invalid");
            return badRequest("A valid phone is a mandatory field.");
        }

        Customer customer = new Customer(inputCustomer.getFirstName(), inputCustomer.getLastName(),
                inputCustomer.getEmail(), inputCustomer.getPhone(),
                inputCustomer.getPayMethod(), inputCustomer.getBalance());

        JPA.em().persist(customer);
        Logger.info("controllers.CustomerController.create(): Created customer " + customer.toString());
        return ok(Json.toJson(customer.toString()));
    }

    /** getAll: Show all customers in the database. Supports pagination.
     * GET request to /customers.
     * @return Result: A Json representation of existing customers.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result getAll() {
        String queryString = "from Customer";
        TypedQuery<Customer> query = JPA.em().createQuery(queryString, Customer.class);
        RequestUtil.paginate(query);
        List<Customer> l = query.getResultList();

        if (l == null || l.isEmpty()) {
            Logger.info("controllers.CustomerController.getAll(): No Customers to show");
            return ok("No customers currently in the system.");
        }
        Logger.info("controllers.CustomerController.getAll() returned " + l.size() + " customers.");
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

        Query query = JPA.em().createQuery("from Customer WHERE id = :id", Customer.class).setParameter("id", id);
        RequestUtil.paginate(query);
        List l = query.getResultList();

        if (l == null || l.isEmpty()) {
            Logger.info("controllers.CustomerController.getById(): customer not found");
            return ok("No such customers currently in the system.");
        }
        Logger.info("controllers.CustomerController.getById() returned " + Json.toJson(l));
        return ok(Json.toJson(l));
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
            Logger.error("controllers.CustomerController.getByName(): Invalid or missing name parameters");
            return badRequest("Please provide a name as a parameter");
        }

        Query query = null;
        if (first != null) {
            if (last == null) {
                query = JPA.em().createQuery("from Customer WHERE firstName = :first", Customer.class)
                        .setParameter("first", first);
            } else {
                query = JPA.em().createQuery("from Customer WHERE firstName = :first AND lastName = :last", Customer.class)
                        .setParameter("first", first).setParameter("last", last);
            }
        } else {
            query = JPA.em().createQuery("from Customer WHERE lastName = :last", Customer.class)
                    .setParameter("last", last);
        }

        RequestUtil.paginate(query);
        List l = query.getResultList();

        if (l == null || l.isEmpty()) {
            Logger.info("controllers.CustomerController.getByName(): customer not found");
            return ok("No such customers currently in the system.");
        }
        Logger.info("controllers.CustomerController.getByName() returned " + l.size() + " customers.");
        return ok(Json.toJson(l));
    }

    /** update: Update an existing customer. Only valid fields are copied.
     * POST request to /customers supplying a Json representation of the updated customer.
     * @return Result: The updated customer.
     */
    @Transactional
    @Security.Authenticated(ActionAuthenticator.class)
    public Result update() {
        JsonNode json = request().body().asJson();
        Customer updatedCustomer = Json.fromJson(json, Customer.class);
        Customer customer = JPA.em().find(Customer.class, updatedCustomer.getId());
        if (customer == null) {
            Logger.info("controllers.CustomerController.update(): customer not found");
            return ok("No such customer currently in the system.");
        }

        String first = updatedCustomer.getFirstName();
        if (first != null && Parse.isNameValid(first))
            customer.setFirstName(first);

        String last = updatedCustomer.getLastName();
        if (last != null && Parse.isNameValid(last))
            customer.setLastName(last);

        String email = updatedCustomer.getEmail();
        if (email != null && Parse.isEmailValid(email))
            customer.setEmail(email);

        String phone = updatedCustomer.getPhone();
        if (phone != null && Parse.isPhoneNumberValid(phone))
            customer.setPhone(phone);

        String payMethod = updatedCustomer.getPayMethod();
        if (payMethod != null)
            customer.setPayMethod(payMethod);

        BigDecimal balance = updatedCustomer.getBalance();
        if (balance != null)
            customer.setBalance(balance);

        Logger.info("controllers.CustomerController.update(): Updated customer: " + customer.toString());
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

        Query query = JPA.em().createQuery("DELETE Customer WHERE id =:id ").setParameter("id", id);
        int res = query.executeUpdate();
        if (res == 0) {
            Logger.info("controllers.CustomerController.delete(): customer not found");
            return ok("No such customer currently in the system.");
        }
        Logger.info("controllers.CustomerController.delete(): deleted customer " + id);
        return ok("Deleted customer with id " + id);
    }

}
