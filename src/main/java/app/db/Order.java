package app.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order implements Comparable<Order> {
    @Id
    private long id;
    @Column
    private String productNames;
    @Column
    private String userName;
    @Column
    private double price;
    @Column
    private String status;
    @Column
    private String paymentId;

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserName(String userId) {
        this.userName = userId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(List<String> productNames) {
        this.productNames = productNames.toString();
    }

    public String getUserName() {
        return userName;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public int compareTo(Order o) {
        return Long.compare(this.id, o.id);
        // throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }
}