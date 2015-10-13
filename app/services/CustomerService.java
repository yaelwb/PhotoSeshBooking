package services;

import com.google.inject.ImplementedBy;

/**
 * Created by yael on 10/13/15.
 */
@ImplementedBy(CustomerServiceImpl.class)
public interface CustomerService {

    boolean customerIdExists(Long id);
}
