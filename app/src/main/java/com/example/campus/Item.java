package com.example.campus;

public class Item {

    //helper class for better management with data, that beleng to Item object

    private String name;
    private String price;
    private String image;
    private String quantity;
    private float discount;
    private int X;
    private int Y;
    private long time;

    public Item(String name, String price, String image, String quantity, float discount){
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
        this.discount = discount;
    }

    public Item(String name, String price, String image, float discount, int X, int Y){
        this.image = image;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.X = X;
        this.Y = Y;
    }

    public Item(String name, String quantity){
        this.name = name;
        this.quantity = quantity;
    }

    public Item(String name, String quantity, long time){
        this.name = name;
        this.quantity = quantity;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public long getTime() {
        return time;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public float getDiscount() {
        return discount;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public void setX(int x) {
        this.X = x;
    }

    public void setY(int y) {
        this.Y = y;
    }
}

