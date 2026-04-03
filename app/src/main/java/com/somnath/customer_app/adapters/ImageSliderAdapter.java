package com.somnath.customer_app.adapters;

import android.util.Log; // Use android.util.Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.somnath.customer_app.R;
import com.somnath.customer_app.models.SliderImage;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
    private List<SliderImage> sliderImageList;
    private String baseUrl;
    public ImageSliderAdapter(List<SliderImage> sliderImageList, String baseUrl) {
        this.sliderImageList = sliderImageList;
        this.baseUrl = baseUrl;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        if (sliderImageList == null || sliderImageList.isEmpty() || position >= sliderImageList.size()) {
            // Handle invalid state, maybe load a default error image or log
            Log.e("ImageSliderAdapter", "Invalid sliderImageList or position: " + position);
            holder.imageView.setImageResource(R.drawable.error_restaurant); // Example error drawable
            return;
        }

        SliderImage currentSliderItem = sliderImageList.get(position);
        String relativeImageUrl = currentSliderItem.getImageUrl(); // Get the URL string from the SliderImage object
        String finalImageUrl = null;

        if (relativeImageUrl != null && !relativeImageUrl.trim().isEmpty()) {
            if (relativeImageUrl.startsWith("http://") || relativeImageUrl.startsWith("https://")) {
                finalImageUrl = relativeImageUrl; // It's already an absolute URL
            } else if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                // Construct the full URL
                String tempBase = baseUrl;
                if (!tempBase.endsWith("/")) {
                    tempBase += "/";
                }
                String tempRelative = relativeImageUrl;
                if (tempRelative.startsWith("/")) {
                    tempRelative = tempRelative.substring(1);
                }
                finalImageUrl = tempBase + tempRelative;
            }
        }

        Log.d("ImageSliderAdapter", "Attempting to load URL for position " + position + ": " + finalImageUrl);

        if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(finalImageUrl)
                    .placeholder(R.drawable.placeholder_restaurant) // Make sure this drawable exists
                    .error(R.drawable.error_restaurant)         // Make sure this drawable exists
                    .centerCrop() // Or any other transformation you need
                    .into(holder.imageView);
        } else {
            // URL is null or empty, load error placeholder
            Log.w("ImageSliderAdapter", "Final image URL is null or empty for position: " + position);
            holder.imageView.setImageResource(R.drawable.error_restaurant);
        }
        holder.itemView.setOnClickListener(v -> {
        });
    }

    @Override
    public int getItemCount() {
        return sliderImageList != null ? sliderImageList.size() : 0;
    }
    public void updateData(List<SliderImage> newSliderImageList, String newBaseUrl) {
        this.sliderImageList = newSliderImageList;
        this.baseUrl = newBaseUrl; // In case the base URL could also change
        notifyDataSetChanged();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_slider_image);
        }
    }
}