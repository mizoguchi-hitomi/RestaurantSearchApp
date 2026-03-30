package com.example.restantsearch;

import java.io.Serializable;
import java.util.List;

/**
 * 店舗情報を保持するデータクラス
 */
public class Shop implements Serializable {
    private String name, address, open, distance;//店舗名、住所、営業時間。距離
    private List<String> imageUrls;//店舗画像

    public Shop(String name, String address, List<String> imageUrls, String open, String distance) {
        this.name = name;
        this.address = address;
        this.imageUrls = imageUrls;
        this.open = open;
        this.distance = distance;
    }

    /**
     * 店舗名を返すメソッド
     */
    public String getName() {return name; }
    /**
     * 住所を返すメソッド
     */
    public String getAddress() { return address; }
    /**
     * 営業時間を返すメソッド
     */
    public String getOpen() { return open; }
    /**
     * 距離を返すメソッド
     */
    public String getDistance() { return distance; }

    /**
     * リストの先頭画像を返す（画像がない場合は空文字）
     */
    public String getTopImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : "";
    }
}