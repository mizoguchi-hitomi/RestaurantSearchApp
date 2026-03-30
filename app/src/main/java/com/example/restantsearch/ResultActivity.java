package com.example.restantsearch;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 検索結果を表示し、スワイプ操作とページングを管理するクラス
 */
public class ResultActivity extends AppCompatActivity {

    private LinearLayout pagingContainer;//ページ番号表示コンテナ
    private ViewPager2 viewPager;//スワイプで店舗切り替えるためのViewPager2
    private ShopAdapter adapter;//店舗データをViewPager2に表示するためのアダプター
    private List<Shop> shopList = new ArrayList<>();//店舗データのリスト

    private int currentPage = 1;//初期ページ
    private int startCount = 1;//API取得開始位置
    private final int PAGE_COUNT = 30;//1ページ30店舗
    private int totalAvailable = 0;//総件数
    private boolean isLoading = false;//読み込み中フラグ

    private String lat, lng, range;//位置情報に関する変数

    /**
     *画面の初期設定をするメソッド
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //activity_result.xmlからUIパーツ取得
        pagingContainer = findViewById(R.id.pagingContainer);
        viewPager = findViewById(R.id.viewPager);
        EditText editLocation = findViewById(R.id.editLocation);
        Button btnBackSearch = findViewById(R.id.btnBackSearch);

        //MainActivityから渡された情報からデータ取得
        String jsonData = getIntent().getStringExtra("JSON_DATA");
        lat = getIntent().getStringExtra("CURRENT_LAT");
        lng = getIntent().getStringExtra("CURRENT_LNG");
        range = getIntent().getStringExtra("RANGE");
        String locationName = getIntent().getStringExtra("LOCATION_NAME");

        //検索地点を表示
        if (locationName != null) editLocation.setText(locationName);
        //検索画面へ戻るボタン
        btnBackSearch.setOnClickListener(v -> finish());

        //初期データをセット
        if (jsonData != null) {
            setupInitialData(jsonData);
        }
        //ページングUI初期化
        updatePagingUI(1);

        //スワイプ処理
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int newPage = (position / PAGE_COUNT) + 1;
                if (newPage != currentPage) {
                    currentPage = newPage;
                    updatePagingUI(currentPage);
                }
                if (position >= shopList.size() - 2 && !isLoading && shopList.size() < totalAvailable) {
                    loadNextPage();
                }
            }
        });
    }

    /**
     * JSON → Shopオブジェクト変換するメソッド
    */
    private void setupInitialData(String json) {
        try {
            JSONObject response = new JSONObject(json);
            JSONObject results = response.getJSONObject("results");
            totalAvailable = results.getInt("results_available");//総件数取得
            JSONArray array = results.getJSONArray("shop");//店舗配列取得

            //変換してリストに追加
            for (int i = 0; i < array.length(); i++) {
                shopList.add(parseShop(array.getJSONObject(i)));
            }

            //Adapterセット
            adapter = new ShopAdapter(shopList);
            viewPager.setAdapter(adapter);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * JSONからShopオブジェクトを生成するメソッド
    */
    private Shop parseShop(JSONObject obj) {
        //1. 画像URLリストの作成(urls)
        List<String> urls = new ArrayList<>();
        JSONObject photo = obj.optJSONObject("photo");
        if (photo != null) {
            JSONObject pc = photo.optJSONObject("pc");
            if (pc != null) {
                String pcL = pc.optString("l");
                if (!pcL.isEmpty()) {
                    urls.add(pcL);
                }
            }
        }

        //2. 距離の計算(dist)
        double sLat = obj.optDouble("lat", 0.0);
        double sLng = obj.optDouble("lng", 0.0);
        String dist = "距離不明";

        try {
            float[] res = new float[1];
            android.location.Location.distanceBetween(
                    Double.parseDouble(lat),
                    Double.parseDouble(lng),
                    sLat,
                    sLng,
                    res
            );

            float m = res[0];

            dist = (m >= 1000) ? String.format("%.1fkm", m/1000) : String.format("%dm", (int)m);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4. Shopオブジェクトの生成（すべての変数が揃った状態で渡す）
        return new Shop(
                obj.optString("name", "店名不明"),
                obj.optString("address", "住所不明"),
                urls,
                obj.optString("open", "情報なし"),
                dist
        );
    }

    private void loadNextPage() {
        isLoading = true;
        startCount += PAGE_COUNT;
        String apiKey = "d38450e195b1552c";
        String url = "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=" + apiKey
                + "&lat=" + lat + "&lng=" + lng + "&range=" + range
                + "&count=" + PAGE_COUNT + "&start=" + startCount + "&format=json";

        new OkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { isLoading = false; }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String resStr = response.body().string();
                        JSONArray newShops = new JSONObject(resStr).getJSONObject("results").getJSONArray("shop");
                        runOnUiThread(() -> {
                            for (int i = 0; i < newShops.length(); i++) {
                                try { shopList.add(parseShop(newShops.getJSONObject(i))); } catch (Exception e) {}
                            }
                            adapter.notifyDataSetChanged();
                            isLoading = false;
                        });
                    } catch (Exception e) { isLoading = false; }
                }
            }
        });
    }

    private class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {
        private final List<Shop> shops;
        ShopAdapter(List<Shop> shops) { this.shops = shops; }

        /**
         *item_shop.xmlの枠を作るメソッド
         */
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
            return new ViewHolder(v);
        }

        /**
         *店舗詳細の1つ1つの中身をセットするメソッド
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Shop shop = shops.get(position);
            holder.tvName.setText(shop.getName());
            holder.tvDistance.setText("現在地から " + shop.getDistance());

            //画像表示
            Glide.with(holder.itemView.getContext())
                    .load(shop.getTopImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .transform(new AmoebaTransformation(holder.itemView.getContext())) // ★ここを追加
                    .into(holder.imgShop);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("SHOP_DATA", shop);
                v.getContext().startActivity(intent);
            });
        }

        /**
         *合計何件の店舗を表示すべきかを、システムに伝えるメソッド
         */
        @Override
        public int getItemCount() { return shops.size(); }

        /**
         * 内部クラスのコンストラクタ
         * レイアウト内のテキストビューや画像ビューなどの各パーツを見つけ出し、いつでも使えるように保持しておくためのメソッド
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDistance;
            ImageView imgShop;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.shopName);
                tvDistance = v.findViewById(R.id.shopAccess);
                imgShop = v.findViewById(R.id.imgShop);

                // 【修正】切り抜き処理を確実に行うためのおまじないを追加
                if (imgShop != null) {
                    imgShop.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }
        }
    }

    /**
     *ページング処理を行うメソッド
     * @param  activePage 今表示中のページ番号
     */
    private void updatePagingUI(int activePage) {
        //表示をリセットする
        pagingContainer.removeAllViews();
        //最大ページ数を計算
        int maxPageCount = (int) Math.ceil((double) totalAvailable / PAGE_COUNT);
        if (maxPageCount <= 0) maxPageCount = 1;
        //表示するページ番号の範囲を計算
        int startPage = Math.max(1, activePage - 2);
        int endPage = Math.min(maxPageCount, startPage + 4);
        if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);

        //ページ番号やアイコンを作成
        for (int i = startPage; i <= endPage; i++) {
            final int targetPage = i;
            FrameLayout frame = new FrameLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(130, 130);
            lp.setMargins(10, 0, 10, 0);
            frame.setLayoutParams(lp);
            frame.setOnClickListener(v -> viewPager.setCurrentItem((targetPage - 1) * PAGE_COUNT, true));

            //ページ番号背景画像
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            int imageNum = ((i - 1) % 5) + 1;
            String imageName = (i == activePage) ? "paging" + imageNum + "_select" : "paging" + imageNum;
            int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (imageId != 0) iv.setImageResource(imageId);

            //ページ番号のテキスト
            TextView tv = new TextView(this);
            tv.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            tv.setText(String.valueOf(i));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            if (i == activePage) {
                tv.setTextColor(Color.parseColor("#333333"));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTextColor(Color.parseColor("#999999"));
            }

            //activity_result.xmlに背景とテキストを追加
            frame.addView(iv);
            frame.addView(tv);
            pagingContainer.addView(frame);
        }
    }
}