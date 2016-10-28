package com.example.yyh.puzzlepicture.activity.Util;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by yyh on 2016/10/21.
 */
public class GameItemView{

    /**
     * 每个小方块的信息
     */

    //每个小方块的实际位置x,
    private int x=0;
    //每个小方块的实际位置y,
    private int y=0;
    //每个小方块的图片，
    private Bitmap bm;
    //每个小方块的图片位置x,
    private int p_x=0;

    //每个小方块的图片位置y.
    private int p_y=0;

    public GameItemView(int x, int y, Bitmap bm) {
        super();
        this.x = x;
        this.y = y;
        this.bm = bm;
        this.p_x=x;
        this.p_y=y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }



    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public int getP_x() {
        return p_x;
    }

    public void setP_x(int p_x) {
        this.p_x = p_x;
    }

    public int getP_y() {
        return p_y;
    }

    public void setP_y(int p_y) {
        this.p_y = p_y;
    }

    /**
     * 判断每个小方块的位置是否正确
     * @return
     */
    public boolean isTrue(){
        if (x==p_x&&y==p_y){
            return true;
        }
        return false;
    }
}
