package com.inledco.exoterra.adddevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.List;

public class ProductAdapter extends SimpleAdapter<String, ProductAdapter.ProductViewHolder> {
    public ProductAdapter(@NonNull Context context, List<String> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_product;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ProductViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, final int position) {
        final String pkey = mData.get(position);
        if (DeviceUtil.containsProduct(pkey) == false) {
            return;
        }
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pkey));
        holder.tv_type.setText(DeviceUtil.getProductName(pkey));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    class  ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_type;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_product_icon);
            tv_type = itemView.findViewById(R.id.item_product_type);
        }
    }
}
