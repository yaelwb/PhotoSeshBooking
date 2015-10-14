package services;

import com.google.inject.ImplementedBy;
import models.Customer;

import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
@ImplementedBy(CustomerServiceImpl.class)
public interface CustomerService {

    boolean customerIdExists(Long id);
    Customer create(Customer inputCustomer);
    String validate(Customer customer);
    void update(Customer from, Customer to);
    int delete(Long id);

    List<Customer> getAll();
    Customer get(Long id);
    List<Customer> getByName(String first, String last);

}
