package com.example.restantsearch;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * ViewPager2やRecyclerViewで店舗リストを表示するためのアダプター
 * 解析済みの店舗データを、リストの各カードに流し込むためのクラス
 */
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    private final List<Shop> shopList; // Shopオブジェクトのリスト

    /**
     * すでに解析済みの Shop リストを受け取る
     */
    public ShopAdapter(List<Shop> shopList) {
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_shop.xml を 1 行分の View として生成
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 表示するデータを 1 件取得
        Shop shop = shopList.get(position);

        // --- 1. テキストのセット ---
        holder.nameText.setText(shop.getName());
        // 距離は ResultActivity で計算済みなので getDistance() を呼ぶだけ
        holder.addressText.setText("現在地から " + shop.getDistance());

        // --- 2. 画像の表示 ---
        // Shopクラスの getTopImageUrl() を使用して安全に 1 枚目を取得
        Glide.with(holder.itemView.getContext())
                .load(shop.getTopImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .transform(new AmoebaTransformation(holder.itemView.getContext()))
                .into(holder.imageView);

        // --- 3. 詳細画面への遷移 ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            // Shop クラスは Serializable なのでそのまま渡せます
            intent.putExtra("SHOP_DATA", shop);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    /**
     * ViewHolder：item_shop.xml 内の各パーツを保持する箱
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, addressText;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.shopName);
            addressText = itemView.findViewById(R.id.shopAccess);
            imageView = itemView.findViewById(R.id.imgShop);

            if (imageView != null) {
                imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
    }
}