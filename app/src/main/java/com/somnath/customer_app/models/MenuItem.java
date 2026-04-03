package com.somnath.customer_app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class MenuItem implements Parcelable {

    // --- Data Fields ---
    private Long id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private boolean available;
    private boolean isBestseller;

    // --- Constructors ---

    /**
     * A no-argument constructor is required for some Android frameworks
     * and libraries like Gson or Firebase. It is a best practice to include it.
     */
    public MenuItem() {
    }

    /**
     * Constructor with all fields.
     */
    public MenuItem(Long id, String name, String description, double price, String imageUrl, String category, boolean available, boolean isBestseller) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.available = available;
        this.isBestseller = isBestseller;
    }


    // --- Parcelable Implementation ---

    /**
     * This special constructor is used to re-create the object from a Parcel.
     * The order of reading data MUST EXACTLY match the order of writing data in writeToParcel().
     */
    protected MenuItem(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        imageUrl = in.readString();
        category = in.readString();
        available = in.readByte() != 0;      // Reading the 'available' boolean
        isBestseller = in.readByte() != 0;   // Reading the 'isBestseller' boolean
    }

    /**
     * This is the required CREATOR field that generates instances of your Parcelable class.
     */
    public static final Creator<MenuItem> CREATOR = new Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel in) {
            return new MenuItem(in);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * This method flattens the object into a Parcel.
     * The order of writing data MUST EXACTLY match the order of reading data in the constructor.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeByte((byte) (available ? 1 : 0));    // Writing the 'available' boolean
        dest.writeByte((byte) (isBestseller ? 1 : 0)); // Writing the 'isBestseller' boolean
    }


    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Getter for the isBestseller field, as requested.
     * Note: The standard Java convention is isBestseller(), but getIsBestseller() also works.
     *
     * @return boolean value of isBestseller
     */
    public boolean getIsBestseller() {
        return isBestseller;
    }

    public void setBestseller(boolean bestseller) {
        isBestseller = bestseller;
    }
}