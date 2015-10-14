package services;

import models.Customer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import play.Logger;
import play.db.jpa.JPA;
import play.libs.Json;
import utilities.Parse;
import utilities.RequestUtil;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
public class CustomerServiceImpl implements CustomerService {

    @Override
    public boolean customerIdExists(Long id) {
        return (get(id) != null);
    }

    @Override
    public Customer create(Customer inputCustomer) {

        Customer customer = new Customer(inputCustomer.getFirstName(), inputCustomer.getLastName(),
                inputCustomer.getEmail(), inputCustomer.getPhone(),
                inputCustomer.getPayMethod(), inputCustomer.getBalance());

        JPA.em().persist(customer);
        Logger.info("services.CustomerService.create(): Created customer " + customer.toString());
        return customer;
    }

    @Override
    public String validate(Customer customer) {
        if (customer.getFirstName() == null || customer.getFirstName().isEmpty() ||
                !Parse.isNameValid(customer.getFirstName())) {
            Logger.error("services.CustomerService.validate(): First name missing/invalid");
            return "A valid first name is a mandatory field.";
        }
        if (customer.getLastName() == null || customer.getLastName().isEmpty() ||
                !Parse.isNameValid(customer.getLastName())) {
            Logger.error("services.CustomerService.validate(): Last name missing/invalid");
            return "A valid last name is a mandatory field.";
        }
        if (customer.getEmail() == null || customer.getEmail().isEmpty() ||
                !Parse.isEmailValid(customer.getEmail())) {
            Logger.error("services.CustomerService.validate(): Email missing/invalid");
            return "A valid email is a mandatory field.";
        }
        if (customer.getPhone() == null || customer.getPhone().isEmpty() ||
                !Parse.isPhoneNumberValid(customer.getPhone())) {
            Logger.error("services.CustomerService.validate(): Phone missing/invalid");
            return "A valid phone is a mandatory field.";
        }
        return null;
    }

    @Override
    public void update(Customer from, Customer to) {
        String first = from.getFirstName();
        if (first != null && Parse.isNameValid(first))
            to.setFirstName(first);

        String last = from.getLastName();
        if (last != null && Parse.isNameValid(last))
            to.setLastName(last);

        String email = from.getEmail();
        if (email != null && Parse.isEmailValid(email))
            to.setEmail(email);

        String phone = from.getPhone();
        if (phone != null && Parse.isPhoneNumberValid(phone))
            to.setPhone(phone);

        String payMethod = from.getPayMethod();
        if (payMethod != null)
            to.setPayMethod(payMethod);

        BigDecimal balance = from.getBalance();
        if (balance != null)
            to.setBalance(balance);

        Logger.info("services.CustomerService.update(): Updated customer: " + to.toString());
    }

    @Override
    public int delete(Long id) {
        Query query = JPA.em().createQuery("DELETE Customer WHERE id =:id ").setParameter("id", id);
        int res = query.executeUpdate();
        if (res == 0)
            Logger.info("controllers.CustomerController.delete(): customer not found");
        else
            Logger.info("controllers.CustomerController.delete(): deleted customer " + id);
        return res;
    }

    @Override
    public List<Customer> getAll() {
        String queryString = "from Customer";
        TypedQuery<Customer> query = JPA.em().createQuery(queryString, Customer.class);
        RequestUtil.paginate(query);
        List<Customer> l = query.getResultList();

        if (l == null || l.isEmpty())
            Logger.info("controllers.CustomerController.getAll(): No Customers to show");
        else
            Logger.info("controllers.CustomerController.getAll() returned " + l.size() + " customers.");
        return l;
    }

    @Override
    public Customer get(Long id) {
        Customer customer = JPA.em().find(Customer.class, id);
        if (customer == null)
            Logger.info("services.CustomerService.update(): customer not found");
        else
            Logger.info("controllers.CustomerController.getById() returned " + customer.toString());
        return customer;
    }

    @Override
    public List<Customer> getByName(String first, String last) {

        //Criteria cr = session.createCriteria(Customer.class);
        //cr.add(Restrictions.eq("firstName", first));
        //List results = cr.list();
        //return results;
        return null;
    }

}

