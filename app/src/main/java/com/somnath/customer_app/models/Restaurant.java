package com.somnath.customer_app.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Restaurant implements Parcelable {
    private Long id;
    private String name;

    @SerializedName("cuisine")
    private String cuisine;
    private String rating;
    private String imageUrl;

    private String deliveryFee;
    private String address;
    private String phone;
    private String description;
    @SerializedName("open")
    private boolean isOpen;

    @SerializedName("openingTime")
    private String openingTime;
    @SerializedName("deliveryTime")
    private String deliveryTime;
    public Restaurant(Long id, String name, String cuisine, String rating, String imageUrl, String deliveryTime,
                      String deliveryFee, String address, String phone, String description
    , boolean isOpen, String openingTime) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.deliveryTime = deliveryTime;
        this.deliveryFee = deliveryFee;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.isOpen = isOpen;
        this.openingTime = openingTime;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }
    protected Restaurant(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        cuisine = in.readString();
        rating = in.readString();
        imageUrl = in.readString();
        deliveryTime = in.readString();
        deliveryFee = in.readString();
        address = in.readString();
        phone = in.readString();
        description = in.readString();
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeString(cuisine);
        dest.writeString(rating);
        dest.writeString(imageUrl);
        dest.writeString(deliveryTime);
        dest.writeString(deliveryFee);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(description);
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public String getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(String deliveryFee) { this.deliveryFee = deliveryFee; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
