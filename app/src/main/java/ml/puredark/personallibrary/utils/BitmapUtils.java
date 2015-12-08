package ml.puredark.personallibrary.utils;

import android.graphics.Bitmap;

/**
 * Created by PureDark on 2015/12/7.
 */
public class BitmapUtils {
    /**
     * 按正方形裁切图片
     */
    public static Bitmap cropToSquare(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
    }
}
