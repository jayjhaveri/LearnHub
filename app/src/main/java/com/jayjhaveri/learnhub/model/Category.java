package com.jayjhaveri.learnhub.model;

/**
 * Created by ADMIN-PC on 22-03-2017.
 */

public class Category {

    private int imageResource;
    private String categoryName;

    public Category(int imageResource, String categoryName) {
        this.imageResource = imageResource;
        this.categoryName = categoryName;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
