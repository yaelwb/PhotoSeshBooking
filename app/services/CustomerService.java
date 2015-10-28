package services;

import com.google.inject.ImplementedBy;
import models.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by yael on 10/13/15.
 */
@Service
@ImplementedBy(CustomerServiceImpl.class)
public interface CustomerService {

    Customer create(Customer inputCustomer);
    String validate(Customer customer);
    void update(Customer from, Customer to);
    int delete(Long id);
    int deleteAll();

    List<Customer> getAll();
    Customer get(Long id);
    List<Customer> getByName(String first, String last);

    //Auxiliary methods for booking usage
    boolean customerIdExists(Long id);
    void addToBalance(Long id, BigDecimal balance);
    void subtractFromBalance(Long id, BigDecimal balance);
}
