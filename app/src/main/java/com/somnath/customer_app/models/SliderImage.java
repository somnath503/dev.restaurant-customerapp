// --- app\src\main\java\com\somnath\customer_app\models\SliderImage.java ---
package com.somnath.customer_app.models;

// SliderImage.java
public class SliderImage {
    private int id;
    private String title;
    private String imageUrl;
    private int displayOrder;
    private String actionUrl;

    public SliderImage(int id, String title, String imageUrl, int displayOrder, String actionUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.actionUrl = actionUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
// Getters and setters
}