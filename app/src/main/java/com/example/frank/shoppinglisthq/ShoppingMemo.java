package com.example.frank.shoppinglisthq;

public class ShoppingMemo {
/*
TODO Artikel nach Geschäft auflisten,

 */
    private long id;
    private int quantity;
    private String product;
    private String unit;
    private double price;
    private boolean checked;

    public ShoppingMemo(long id, int quantity, String product, boolean checked, String unit, double price) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
        this.checked = checked;
        this.unit = unit;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return quantity + " " + unit + " " + product + " " + price + " €";
    }
}
