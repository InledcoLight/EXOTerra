package com.liruya.exoterra.adddevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.util.DeviceUtil;

import java.util.List;

public abstract class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context mContext;
    private List<String> mProducts;

    public ProductAdapter(Context context, List<String> products) {
        mContext = context;
        mProducts = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ProductViewHolder holder = new ProductViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_product, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int i) {
        if (mProducts == null || i < 0 || i >= mProducts.size()) {
            return;
        }
        final String prdt = mProducts.get(i);
        if (DeviceUtil.containsProduct(prdt) == false) {
            return;
        }
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(prdt));
        holder.tv_type.setText(DeviceUtil.getProductType(prdt));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItem(prdt);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts == null ? 0 : mProducts.size();
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

    protected abstract void onClickItem(String prdt);
}
