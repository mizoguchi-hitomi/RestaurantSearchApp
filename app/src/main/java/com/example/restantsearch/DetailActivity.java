package com.example.restantsearch;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

/**
 * ホットペッパーAPIなどで取得した店舗情報を詳細画面で表示するActivityクラス
 */
public class DetailActivity extends AppCompatActivity {

    /**
     * Activity作成時に呼ばれるメイン処理を行うメソッド
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //detail_shop.xmlを画面として設定
        setContentView(R.layout.detail_shop);

        //1. detail_shop.xmlからUIパーツの取得
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView imgDetail = findViewById(R.id.imgDetail);

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvAddress = findViewById(R.id.tvDetailAddress);
        TextView tvOpen = findViewById(R.id.tvDetailOpen);

        // 2. Intentで渡されたShopデータを取得
        Shop shop = (Shop) getIntent().getSerializableExtra("SHOP_DATA");

        if (shop != null) {
            //店名・住所・距離・営業時間を画面に反映
            tvName.setText(shop.getName());
            tvAddress.setText(shop.getAddress() + " (" + shop.getDistance() + ")");
            tvOpen.setText(shop.getOpen());

            //3. 画像の表示
            Glide.with(this)
                    .load(shop.getTopImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(imgDetail);
        }

        //戻るボタンの処理
        btnBack.setOnClickListener(v -> finish());
    }
}