package app.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "products")
public class Product implements Comparable<Product> {
    @Id
    private int id;
    @Column
    private String name;
    @Column
    private String category;
    @Column
    private double price;
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    @Column
    private String pathToImg;
    @Column
    private boolean isVisible;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPathToImg(String pathToImg) {
        this.pathToImg = pathToImg;
    }

    public String getPathToImg() {
        return pathToImg;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    @Override
    public int compareTo(Product o) {
        return Integer.compare(this.id, o.id);
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }
}