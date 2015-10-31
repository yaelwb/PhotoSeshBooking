package models;

import enums.PayMethod;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by yael on 9/28/15.
 */
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="customer_id_seq",
            sequenceName="customer_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_id_seq")
    private Long id;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="email")
    private String email;

    @Column(name="phone")
    private String phone;

    @Column(name="pay_method")
    private String payMethod;

    @Column(name="balance")
    private BigDecimal balance;

    public Customer() {}

    public Customer(String firstName, String lastName, String email, String phone, String payMethod, BigDecimal balance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.payMethod = payMethod != null? payMethod : PayMethod.CASH.toString();
        this.balance = balance != null? balance : new BigDecimal("0.0", new MathContext(2));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod.toUpperCase();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + ", ID: " + id + ", Phone: " + phone + ", Email: " + email +
                ", Balance: " + balance + ", Method: " + payMethod;
    }
}
