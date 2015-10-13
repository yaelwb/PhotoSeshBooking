package services;

import models.Customer;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
public class CustomerServiceImpl implements CustomerService {

    @Override
    public boolean customerIdExists(Long id) {
        Query query = JPA.em().createQuery("from Customer WHERE id = :id", Customer.class).setParameter("id", id);

        List<Customer> l = query.getResultList();
        if(l == null || l.isEmpty())
            return false;
        Customer customer = l.get(0);
        return customer != null;
    }
}
