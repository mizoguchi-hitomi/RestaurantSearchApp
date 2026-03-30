package com.example.restantsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;

/**
 * 店舗画像をアメーバ状のマスク画像で切り抜くためのクラス
 */
public class AmoebaTransformation extends BitmapTransformation {
    private final Bitmap mask;//切り抜き用のマスク画像

    public AmoebaTransformation(Context context) {
        //マスク画像の読み込み
        mask = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_cutout);
    }

    //AmoebaTransformation.java の transform メソッドを差し替え
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        //1. 透明を扱える空のキャンバスを作る
        Bitmap result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //2. 店舗写真を全面に描く
        Bitmap scaledImage = Bitmap.createScaledBitmap(toTransform, outWidth, outHeight, true);
        canvas.drawBitmap(scaledImage, 0, 0, paint);

        //3. 型抜きモードを設定(DST_INを利用)
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        //4. 上からマスクを描く
        Bitmap scaledMask = Bitmap.createScaledBitmap(mask, outWidth, outHeight, true);
        canvas.drawBitmap(scaledMask, 0, 0, paint);

        paint.setXfermode(null);
        return result;
    }

    /**
     * 加工した画像を保存する際のID
     * @param messageDigest
     */
    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update("AmoebaTransformation_v1".getBytes());
    }

    /**
     *加工の種類が同じか判定するためのメソッド
     * @param o   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AmoebaTransformation;
    }

    /**
     * 加工の識別番号を作成するためのメソッド
     * @return
     */
    @Override
    public int hashCode() {
        return "AmoebaTransformation".hashCode();
    }
}