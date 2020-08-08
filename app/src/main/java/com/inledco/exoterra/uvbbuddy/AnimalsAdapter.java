package com.inledco.exoterra.uvbbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AnimalsAdapter extends SimpleAdapter<Animal, AnimalsAdapter.AnimalsViewHolder> {

    public AnimalsAdapter(@NonNull Context context, List<Animal> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_animal;
    }

    @NonNull
    @Override
    public AnimalsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new AnimalsViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalsViewHolder holder, int i) {
        final int positon = holder.getAdapterPosition();
        Animal animal = mData.get(positon);
        try {
            InputStream stream = mContext.getAssets().open("animals/" + animal.getIcon());
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            holder.icon.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.name.setText(animal.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(positon);
                }
            }
        });
    }

    class AnimalsViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;

        public AnimalsViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_animal_icon);
            name = itemView.findViewById(R.id.item_animal_name);
        }
    }
}
