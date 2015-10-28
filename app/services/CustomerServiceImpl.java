package services;

import models.Customer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import play.Logger;
import play.db.jpa.JPA;
import utilities.Parse;
import utilities.RequestUtil;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private MathContext mc = new MathContext(2); // 2 precision

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
            Logger.info("services.CustomerService.delete(): customer not found");
        else
            Logger.info("services.CustomerService.delete(): deleted customer " + id);
        return res;
    }

    @Override
    public int deleteAll() {
        Query query = JPA.em().createQuery("DELETE Customer");
        int res = query.executeUpdate();
        if (res == 0)
            Logger.info("services.CustomerService.deleteAll(): no customers found");
        else
            Logger.info("services.CustomerService.deleteAll(): deleted all " + res + " customers");
        return res;
    }

    @Override
    public List<Customer> getAll() {
        Session session = JPA.em().unwrap(Session.class);
        Criteria cr = session.createCriteria(Customer.class);

        String first = RequestUtil.getQueryParam("first");
        if(first != null)
            cr.add(Restrictions.eq("firstName", first));

        String last = RequestUtil.getQueryParam("last");
        if(last != null)
            cr.add(Restrictions.eq("lastName", last));

        String payMethod = RequestUtil.getQueryParam("payMethod");
        if(payMethod != null)
            cr.add(Restrictions.eq("payMethod", payMethod));

        String fromBalance = RequestUtil.getQueryParam("fromBalance");
        if(fromBalance != null)
            cr.add(Restrictions.ge("balance", new BigDecimal(fromBalance)));

        String toBalance = RequestUtil.getQueryParam("toBalance");
        if(toBalance != null)
            cr.add(Restrictions.le("balance", new BigDecimal(toBalance)));

        RequestUtil.paginate(cr);
        List<Customer> results = cr.list();

        if (results == null || results.isEmpty())
            Logger.info("services.CustomerService.getAll(): No Customers to show");
        else
            Logger.info("services.CustomerService.getAll(): returned " + results.size() + " customers.");
        return results;
    }

    @Override
    public Customer get(Long id) {
        Customer customer = JPA.em().find(Customer.class, id);
        if (customer == null)
            Logger.info("services.CustomerService.get(): customer not found");
        else
            Logger.info("services.CustomerService.get(): returned " + customer.toString());
        return customer;
    }

    @Override
    public List<Customer> getByName(String first, String last) {
        Session session = JPA.em().unwrap(Session.class);
        Criteria cr = session.createCriteria(Customer.class);
        if(first != null)
            cr.add(Restrictions.eq("firstName", first));
        if(last != null)
            cr.add(Restrictions.eq("lastName", last));
        List<Customer> results = cr.list();
        if(results.isEmpty())
            Logger.info("services.CustomerService.getByName(): customer not found");
        else
            Logger.info("services.CustomerService.getByName(): returned " + results.size() + " customers.");
        return results;
    }

    @Override
    public void addToBalance(Long id, BigDecimal balance) {
        Customer customer = get(id);
        if(customer != null)
            customer.setBalance(balance.add(customer.getBalance(), mc));
    }

    @Override
    public void subtractFromBalance(Long id, BigDecimal balance) {
        Customer customer = get(id);
        if(customer != null)
            customer.setBalance(balance.subtract(customer.getBalance(), mc));
    }
}

