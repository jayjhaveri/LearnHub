package com.jayjhaveri.learnhub.model;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

/**
 * Created by ADMIN-PC on 22-03-2017.
 */

public class Category {

    private GoogleMaterial.Icon imageResource;
    private String categoryName;

    public Category(GoogleMaterial.Icon imageResource, String categoryName) {
        this.imageResource = imageResource;
        this.categoryName = categoryName;
    }

    public GoogleMaterial.Icon getImageResource() {
        return imageResource;
    }

    public void setImageResource(GoogleMaterial.Icon imageResource) {
        this.imageResource = imageResource;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
