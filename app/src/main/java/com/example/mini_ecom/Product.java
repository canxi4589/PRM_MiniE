package com.example.mini_ecom;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int imageResource;
    private String category;

    public Product(int id, String name, String description, double price, int imageResource, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResource = imageResource;
        this.category = category;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getImageResource() { return imageResource; }
    public String getCategory() { return category; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setCategory(String category) { this.category = category; }
} 