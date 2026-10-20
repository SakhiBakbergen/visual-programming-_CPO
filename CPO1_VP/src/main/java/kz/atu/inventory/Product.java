package kz.atu.inventory;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final IntegerProperty minQuantity = new SimpleIntegerProperty();
    private final StringProperty supplier = new SimpleStringProperty();

    public Product(int id, String name, String category, double price,
                   int quantity, int minQuantity, String supplier) {
        this.id.set(id);
        this.name.set(name);
        this.category.set(category);
        this.price.set(price);
        this.quantity.set(quantity);
        this.minQuantity.set(minQuantity);
        this.supplier.set(supplier);
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty categoryProperty() { return category; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
    public IntegerProperty minQuantityProperty() { return minQuantity; }
    public StringProperty supplierProperty() { return supplier; }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v); }
    public String getCategory() { return category.get(); }
    public void setCategory(String v) { category.set(v); }
    public double getPrice() { return price.get(); }
    public void setPrice(double v) { price.set(v); }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int v) { quantity.set(v); }
    public int getMinQuantity() { return minQuantity.get(); }
    public void setMinQuantity(int v) { minQuantity.set(v); }
    public String getSupplier() { return supplier.get(); }
    public void setSupplier(String v) { supplier.set(v); }

    public boolean isLowStock() { return getQuantity() <= getMinQuantity(); }
}