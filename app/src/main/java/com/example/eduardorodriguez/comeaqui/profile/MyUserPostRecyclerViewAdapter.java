package com.example.eduardorodriguez.comeaqui.profile;

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eduardorodriguez.comeaqui.general.FoodLookActivity;
import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.objects.FoodPost;
import com.example.eduardorodriguez.comeaqui.utilities.ImageLookActivity;

import java.util.ArrayList;

public class MyUserPostRecyclerViewAdapter extends RecyclerView.Adapter<MyUserPostRecyclerViewAdapter.ViewHolder> {

    private ArrayList<FoodPost> mValues;


    public MyUserPostRecyclerViewAdapter(ArrayList<FoodPost> items) {
        mValues = items;
    }

    public void addNewRow(ArrayList<FoodPost> data){
        this.mValues = data;
        this.notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_post_element, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FoodPost foodPost = mValues.get(position);

        holder.postNameView.setText(foodPost.plate_name);
        holder.postPrice.setText(foodPost.price + "€");
        holder.postTime.setText(foodPost.time);
        holder.posterDescriptionView.setText(foodPost.description);

        if (!foodPost.food_photo.contains("no-image")) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.mView.getContext()).load(foodPost.food_photo).into(holder.imageView);
            holder.imageView.setOnClickListener((v) -> {
                Intent imageLook = new Intent(holder.mView.getContext(), ImageLookActivity.class);
                imageLook.putExtra("image_url", foodPost.food_photo);
                holder.mView.getContext().startActivity(imageLook);
            });
        }

        holder.cardButtonView.setOnClickListener(v -> {
                Intent foodLook = new Intent(holder.mView.getContext(), FoodLookActivity.class);
                foodLook.putExtra("foodPostId", foodPost.id);
                holder.mView.getContext().startActivity(foodLook);
            });

        holder.cardButtonView.setOnClickListener(v -> {
            Intent foodLook = new Intent(holder.mView.getContext(), FoodLookActivity.class);
            foodLook.putExtra("foodPostId", foodPost.id);
            holder.mView.getContext().startActivity(foodLook);
        });

        setTypes(foodPost.type, holder);

    }

    void setTypes(String types, ViewHolder holder){
        ImageView[] imageViews = new ImageView[]{
                holder.vegetarian,
                holder.vegan,
                holder.cereal,
                holder.spicy,
                holder.fish,
                holder.meat,
                holder.dairy
        };
        ArrayList<ImageView> imageViewArrayList = new ArrayList<>();
        for (ImageView imageView: imageViews){
            imageView.setVisibility(View.GONE);
            imageViewArrayList.add(imageView);
        }
        int[] resources = new int[]{
                R.drawable.vegetarianfill,
                R.drawable.veganfill,
                R.drawable.cerealfill,
                R.drawable.spicyfill,
                R.drawable.fishfill,
                R.drawable.meatfill,
                R.drawable.dairyfill,
        };
        for (int i = 0; i < types.length(); i++){
            if (types.charAt(i) == '1'){
                imageViewArrayList.get(i).setImageResource(resources[i]);
                imageViewArrayList.get(i).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mValues != null ? mValues.size(): 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView foodNameView;
        public TextView postTime;
        public TextView postPrice;
        public TextView posterDescriptionView;
        public TextView postNameView;
        public View cardButtonView;
        public ImageView imageView;

        ImageView vegetarian;
        ImageView vegan;
        ImageView cereal;
        ImageView spicy;
        ImageView fish;
        ImageView meat;
        ImageView dairy;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            foodNameView = view.findViewById(R.id.plateName);
            postNameView = view.findViewById(R.id.plate_name);
            postTime = view.findViewById(R.id.time);
            postPrice = view.findViewById(R.id.price);
            posterDescriptionView = view.findViewById(R.id.description);
            cardButtonView = view.findViewById(R.id.cardButton);
            imageView = view.findViewById(R.id.image);

            vegetarian = view.findViewById(R.id.vegetarian);
            vegan = view.findViewById(R.id.vegan);
            cereal = view.findViewById(R.id.cereal);
            spicy = view.findViewById(R.id.spicy);
            fish = view.findViewById(R.id.fish);
            meat = view.findViewById(R.id.meat);
            dairy = view.findViewById(R.id.dairy);
        }
    }
}
