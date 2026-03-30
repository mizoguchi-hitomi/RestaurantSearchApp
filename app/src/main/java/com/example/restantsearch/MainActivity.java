package com.example.restantsearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 *検索画面を担うアクティビティクラス
 */
public class MainActivity extends AppCompatActivity {

    private EditText editLocation;//GPSで取得した地名を表示・保持しておく用
    private Button btnGPS, btnSearch;//現在地取得ボタン, 検索実行ボタン
    private Spinner spinnerRange;//検索範囲を選択するドロップダウン

    private FusedLocationProviderClient fusedLocationClient;//Google Play Servicesの位置情報取得クラス
    private double currentLat = 0.0, currentLng = 0.0;//現在取得した位置の経緯

    private static final int PERMISSION_REQUEST_LOCATION = 100;//位置情報アクセス権限リクエスト用の定数

    //OkHttpのインスタンスを作成
    private final OkHttpClient client = new OkHttpClient();

    /**
     *アクティビティの初期化を行うメソッド
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //activity_main.xmlのUI要素を取得
        editLocation = findViewById(R.id.editLocation);
        btnGPS = findViewById(R.id.btnGPS);
        btnSearch = findViewById(R.id.btnSearch);
        spinnerRange = findViewById(R.id.spinnerRange);

        //現在地取得クライアントを初期化
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //現在地取得ボタン押下時の処理
        btnGPS.setOnClickListener(v -> checkLocationPermissionAndFetch());
        //検索ボタン押下時の処理
        btnSearch.setOnClickListener(v -> {
            if (currentLat == 0.0 && currentLng == 0.0) {
                Toast.makeText(this, "現在地を取得してください", Toast.LENGTH_SHORT).show();
                return;
            }

            //スピナー選択値をAPI仕様の数値に変換
            int range = convertRangeToApiValue(spinnerRange.getSelectedItem().toString());
            //検索実行
            searchRestaurants(currentLat, currentLng, range);
        });
    }

    /**
     * 位置情報権限の確認と取得処理を行うメソッド
     */
    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // 権限リクエスト
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            getCurrentLocation();//権限あれば取得
        }
    }

    /**
     * 現在地の経緯を取得して表示するメソッド
     */
    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();

                            try {
                                //Geocoderで住所に変換
                                Geocoder geocoder = new Geocoder(this, java.util.Locale.JAPAN);
                                List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);

                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String city = address.getLocality();
                                    String subLocal = address.getSubLocality();

                                    if (city != null && subLocal != null) {
                                        editLocation.setText(city + subLocal + "付近");
                                    } else if (city != null) {
                                        editLocation.setText(city + "付近");
                                    } else {
                                        editLocation.setText(currentLat + ", " + currentLng + "付近");
                                    }
                                } else {
                                    editLocation.setText(currentLat + ", " + currentLng + "付近");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                editLocation.setText(currentLat + ", " + currentLng + "付近");
                            }
                        } else {
                            Toast.makeText(this, "現在地を取得できませんでした。GPSを確認してください。", Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "位置情報の権限がありません", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * スピナーで選択された文字列をAPIの距離パラメータに変換するメソッド
     * @param rangeText
     * @return
     */
    private int convertRangeToApiValue(String rangeText) {
        switch(rangeText){
            case "300m以内": return 1;
            case "500m以内": return 2;
            case "1km以内": return 3;
            case "2km以内": return 4;
            case "3km以内": return 5;
            default: return 3;
        }
    }

    /**
     * Hot Pepper APIにリクエストを送って飲食店情報を取得するメソッド
     * @param lat
     * @param lng
     * @param range
     */
    private void searchRestaurants(double lat, double lng, int range) {
        String apiKey = "自身のAPIキーを設定する";
        String url = "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=" + apiKey +
                "&lat=" + lat + "&lng=" + lng +
                "&range=" + range + "&count=30" + "&format=json";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(MainActivity.this, "通信エラーが発生しました", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonResponse = response.body().string();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("JSON_DATA", jsonResponse);
                        intent.putExtra("LOCATION_NAME", editLocation.getText().toString());
                        intent.putExtra("CURRENT_LAT", String.valueOf(lat));
                        intent.putExtra("CURRENT_LNG", String.valueOf(lng));
                        intent.putExtra("RANGE", String.valueOf(range));
                        startActivity(intent);
                    });
                }
            }
        });
    }

    /**
     * 位置情報権限リクエストの結果を受け取るメソッド
     * @param requestCode The request code passed in {@link #requestPermissions}.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     {@link android.content.pm.PackageManager#PERMISSION_GRANTED} or
     *                     {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}