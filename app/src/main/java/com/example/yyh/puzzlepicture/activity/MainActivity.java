package com.example.yyh.puzzlepicture.activity;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.yyh.puzzlepicture.R;
import com.example.yyh.puzzlepicture.activity.Util.GameItemView;
import com.example.yyh.puzzlepicture.activity.Util.ToastUtil;
public class MainActivity extends AppCompatActivity {
    //选择图片的标记
    private static final int CHOICE_PHOTO=999;
    /**
     * 利用二维数组创建若干个游戏小方框
     */
    private ImageView [][] iv_game_arr=new ImageView[3][5];
    /**
     *游戏主界面
     *
     */
    private GridLayout gl_game_layout;
    //小方块的行和列
    private int i;
    private int j;
    /**空方块的全局变量*/
    private ImageView iv_null_imagview;
    //当前手势对象
    private GestureDetector gestureDetector;
    //判断游戏是否开始
    private boolean isStart=false;
    //判断当前动画是否在移动状态。（若在移动状态，不可其他操作）
    private boolean isAminMove=false;
    //选择图片的按钮
    private Button bt_choice;
    //图片显示
    private ImageView photo;
    private Bitmap bt_tupan;
    //显示步数的text
    private TextView tv_step;
    //操作的步数
    private static int step=0;
    //实时更新操作的步数
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==007){
                step++;
                tv_step.setText("已用步数："+String.valueOf(step));



            }
        }
    };



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHOICE_PHOTO:
                if (resultCode==RESULT_OK){
                    //判断手机系统版本
                    if (Build.VERSION.SDK_INT>=19){
                        handleImageOnKitKat(data);
                        //得到imageview中的图片
                        BitmapDrawable bitmapDrawable= (BitmapDrawable) photo.getDrawable();
                        bt_tupan=bitmapDrawable.getBitmap();
                        removeGameItem();
                        setGameItem();
                        startGame();
                    }else {
                        handleImageBeforeKitKat(data);
                        //得到imageview中的图片
                        BitmapDrawable bitmapDrawable= (BitmapDrawable) photo.getDrawable();
                        bt_tupan=bitmapDrawable.getBitmap();
                        removeGameItem();
                        setGameItem();
                        startGame();
                    }
                }
        }
    }
    //手机不大于19的取数据方法
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri =data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }
    /**
     * 手机大于19的取数据方法
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath=null;
        Uri uri=data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的url,则通过document的id处理。
            String docId=DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id =docId.split(":")[1];//解析出数字格式的id;
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contenturi= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contenturi,null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果不是document类型的uri,则使用普通的方式处理。
            imagePath=getImagePath(uri,null);
        }
        displayImage(imagePath);
    }
    /**
     * 显示图片
     * @param imagePath  //图片的路径。
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (isHeigthBigWidth(bitmap)) {
                Bitmap bt = rotaingImageView(bitmap);//将图片旋转90度。
                Bitmap disbitmapt = ajustBitmap(bt);
                photo.setImageBitmap(disbitmapt);
            } else {
                Bitmap disbitmap = ajustBitmap(bitmap);
                photo.setImageBitmap(disbitmap);
            }
        }
    }

    /**
     * 调整图片的方向
     * @param bitmap
     * @return
     */
    private Bitmap rotaingImageView(Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();;
        matrix.postRotate(270);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
    /**
     * 得到图片的路径
     * @param externalContentUri
     * @param selection
     * @return
     */
    private String getImagePath(Uri externalContentUri, String selection) {
        String path=null;
        Cursor cursor=getContentResolver().query(externalContentUri, null, selection, null, null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        }
        cursor.close();
        return path;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        photo= (ImageView) findViewById(R.id.iv);
        photo.setImageResource(R.drawable.haizei);

        //显示步数
        tv_step= (TextView) findViewById(R.id.tv_step);

        //得到imageview中的图片
        BitmapDrawable bitmapDrawable= (BitmapDrawable) photo.getDrawable();
        bt_tupan=bitmapDrawable.getBitmap();

        bt_choice= (Button) findViewById(R.id.bt_choice);
        bt_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, CHOICE_PHOTO);//打开相册
            }
        });

        /**
         *  初始化游戏界面，并添加上小方块。
         */
        gl_game_layout= (GridLayout) findViewById(R.id.gl);
        setGameItem();
        startGame();

    }
    private void setGameItem() {

        //调整图片的尺寸
        Bitmap abitmap=ajustBitmap(bt_tupan);
        int ivWidth=getWindowManager().getDefaultDisplay().getWidth()/5;//每个游戏小方块的宽和高。切成正方形
        int tuWidth=abitmap.getWidth()/5;
        /*WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int tuWidth = outMetrics.widthPixels;*/
        // int tuHeigth=bitmap.getHeight();
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                //将大图切成小方块
                Bitmap bm=Bitmap.createBitmap(abitmap,j*tuWidth,i*tuWidth,tuWidth,tuWidth);
                iv_game_arr[i][j]=new ImageView(this);
                //v_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(tuWidth,tuWidth));
                //iv_game_arr[i][j].setScaleType(ImageView.ScaleType.FIT_XY);
                iv_game_arr[i][j].setImageBitmap(bm);//设置每一个小方块的图案
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(ivWidth, ivWidth));
                //设置方块之间的间距
                iv_game_arr[i][j].setPadding(2, 2, 2, 2);
                iv_game_arr[i][j].setTag(new GameItemView(i, j, bm)); //绑定自定义数据
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = isAdjacentNullImageView((ImageView) v);
                        // Toast.makeText(getApplicationContext(),flag+"",Toast.LENGTH_SHORT).show();
                        if (flag) {
                            //   Log.i("进入changeDateByImageView","！！！！！！！！！！！！！！！！！！！！");
                            changeDateByImageView((ImageView) v);
                            handler.sendEmptyMessage(007);


                        }
                    }
                });
            }
        }
    }

    /**
     * 移除GridLayout
     */
    private void removeGameItem(){
        for (i = 0; i <iv_game_arr.length; i++){
            for (j = 0; j < iv_game_arr[0].length; j++){
                gl_game_layout.removeView(iv_game_arr[i][j]);
            }
        }
    }





    /**
     * 将小方格放入GridLayout
     */
    private void startGame() {
        tv_step.setText("已用步数：0");

        for (i = 0; i <iv_game_arr.length; i++){
            for (j = 0; j <iv_game_arr[0].length; j++){
                gl_game_layout.addView(iv_game_arr[i][j]);
            }
        }
        //将最后一个方块设置为设置空方块。
        setNullImageView(iv_game_arr[i-1][j-1]);
        randomOrder();
        isStart=true;//游戏开始
        //创建手势对象
        gestureDetector =new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
            @Override
            public void onShowPress(MotionEvent e) {
            }
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }
            @Override
            public void onLongPress(MotionEvent e) {
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Toast.makeText(MainActivity.this,""+getDirctionByGesure(e1.getX(),e1.getY(),e2.getX(),e2.getY()),Toast.LENGTH_SHORT).show();
                int type=getDirctionByGesure(e1.getX(),e1.getY(),e2.getX(),e2.getY());
                changeByDirGes(type);
                handler.sendEmptyMessage(007);
                return false;
            }
        });
    }

    //调整图片的大小
    private Bitmap ajustBitmap(Bitmap bitmap) {
        int width=getWindowManager().getDefaultDisplay().getWidth()-(iv_game_arr[0].length-1)*2;
        int heigth=width/5*3;
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap, width, heigth, true);
        return scaledBitmap;
    }
    public void changeByDirGes(int type){
        //默认有动画
        changeByDirGes(type,true);
    }
    /**重载changeByDirGes(int type)方法;
     * 根据手势的方向，对空方块相邻位置的方块进行移动。
     * @param type 方向的返回值  1:上 2：下 3：左 5：右
     * @param isAnim 是否有动画 true:有动画，false:无动画
     */
    public void changeByDirGes(int type,boolean isAnim){
        //1.获取当前空方块的位置。
        GameItemView null_gameItemView= (GameItemView) iv_null_imagview.getTag();
        int new_x=null_gameItemView.getX();
        int new_y=null_gameItemView.getY();
        //2.根据方向，设置相应相邻的位置坐标。
        if (type==1){//说明空方块在要移动的方块的上面。
            new_x++;
        }else if (type==2){//空方块在要移动的方块的下面
            new_x--;
        }else if (type==3){//空方块在要移动的方块的左面
            new_y++;
        }else if (type==4){//空方块在要移动的方块的右面
            new_y--;
        }
        //3.判断这个新坐标是否存在
        if(new_x>=0&&new_x<iv_game_arr.length&&new_y>=0&&new_y<iv_game_arr[0].length){
            //存在，可以移动交换数据
            if(isAnim){//有动画
                changeDateByImageView(iv_game_arr[new_x][new_y]);
            }else{
                changeDateByImageView(iv_game_arr[new_x][new_y],isAnim);
            }
        }else{
            //什么也不做
        }
    }
    /**
     * 增加手势滑动，根据手势判断是上下左右滑动
     * @param start_x 手势起始点x
     * @param start_y 手势起始点y
     * @param end_x 手势终止点 x
     * @param end_y 手势终止点y
     * @return 1:上 2：下 3：左 5：右
     */
    public int getDirctionByGesure(float start_x,float start_y,float end_x,float end_y){
        boolean isLeftOrRight =(Math.abs(end_x-start_x)>Math.abs(end_y-start_y))?true:false; //是否是左右
        if(isLeftOrRight){//左右
            boolean isLeft=(end_x-start_x)>0?false:true;
            if(isLeft){
                return 3;
            }else {
                return 4;
            }
        }else{//上下
            boolean isUp=(end_y-start_y)>0?false:true;
            if (isUp){
                return 1;
            }else {
                return 2;
            }

        }
    }
    /**
     * 设置动画，动画结束之后，交换两个方块的数据。
     * @param itemimageView 点击的方块
     */
    public void changeDateByImageView(final ImageView itemimageView){
        //默认有动画
        changeDateByImageView(itemimageView,true);
    }


    /**增加重载方法 判断是否有动画
     * 设置动画，动画结束之后，交换两个方块的数据。
     * @param itemimageView 点击的方块
     * @param isAnim 是否有动画
     */
    public void changeDateByImageView(final ImageView itemimageView,boolean isAnim){
        if (isAminMove){//如果动画正在执行，不做交换操作
            return;
        }
        //1.创建一个动画，设置方向，移动的距离
        TranslateAnimation translateAnimation=null;
        if (!isAnim){
            //得到点击方块绑定的数据
            GameItemView gameItemView = (GameItemView) itemimageView.getTag();
            //将空方块的图案设置为点击方块
            iv_null_imagview.setImageBitmap(gameItemView.getBm());
            //得到空方块绑定的数据
            GameItemView null_gameItemView = (GameItemView) iv_null_imagview.getTag();
            //交换数据（将点击方块的数据传入空方块）
            null_gameItemView.setBm(gameItemView.getBm());
            null_gameItemView.setP_x(gameItemView.getP_x());
            null_gameItemView.setP_y(gameItemView.getP_y());
            //设置当前点击的方块为空方块。
            setNullImageView(itemimageView);
            if (isStart){
                isGameWin();//成功时，会弹一个吐司。
            }
            return;
        }
        //判断方向，设置动画
        if (itemimageView.getX()>iv_null_imagview.getX()){//当前点击的方块在空方块的上面
            //下移
            translateAnimation = new TranslateAnimation(0.1f,-itemimageView.getWidth(),0.1f,0.1f);
        }else if (itemimageView.getX()<iv_null_imagview.getX()){//当前点击的方块在空方块的下面
            //上移
            boolean f=itemimageView.getX()<iv_null_imagview.getX();
            //Log.i("点击方块","sssssssssssssssssssssssss"+f);
            translateAnimation = new TranslateAnimation(0.1f,itemimageView.getWidth(),0.1f,0.1f);
        }else if (itemimageView.getY()>iv_null_imagview.getY()){//当前点击的方块在空方块的左面
            //右移
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,-itemimageView.getWidth());
        }else if(itemimageView.getY()<iv_null_imagview.getY()){//当前点击的方块在空方块的右面
            //左移
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,itemimageView.getWidth());
        }
        //2.设置动画的各种参数
        translateAnimation.setDuration(80);
        translateAnimation.setFillAfter(true);
        //3.设置动画的监听
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAminMove=true;
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束，交换数据
                isAminMove=false;
                itemimageView.clearAnimation();
                //得到点击方块绑定的数据
                GameItemView gameItemView = (GameItemView) itemimageView.getTag();
                //将空方块的图案设置为点击方块
                iv_null_imagview.setImageBitmap(gameItemView.getBm());
                //得到空方块绑定的数据
                GameItemView null_gameItemView = (GameItemView) iv_null_imagview.getTag();
                //交换数据（将点击方块的数据传入空方块）
                null_gameItemView.setBm(gameItemView.getBm());
                null_gameItemView.setP_x(gameItemView.getP_x());
                null_gameItemView.setP_y(gameItemView.getP_y());
                //设置当前点击的方块为空方块。
                setNullImageView(itemimageView);
                if (isStart){
                    isGameWin();//成功时，会弹一个吐司。
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        //动画执行
        itemimageView.startAnimation(translateAnimation);
    }
    /**
     * 设置某个方块为空方块
     * @param imageView  当前要设置为空方块的实例。
     */
    public void setNullImageView(ImageView imageView){
        imageView.setImageBitmap(null);
        iv_null_imagview=imageView;
    }

    /**
     *   判断当前点击的方块，是否和空方块相邻。
     * @param imageView 当前点击的方块
     * @return true：相邻。 false:不相邻。
     */
    public boolean isAdjacentNullImageView(ImageView imageView){
        //获取当前空方块的位置与点击方块的位置
        GameItemView null_gameItemView= (GameItemView) iv_null_imagview.getTag();
        GameItemView now_gameItem_view = (GameItemView) imageView.getTag();
       if(null_gameItemView.getY()==now_gameItem_view.getY()&&now_gameItem_view.getX()+1==null_gameItemView.getX()){//当前点击的方块在空方块的上面
           return true;
       }else if(null_gameItemView.getY()==now_gameItem_view.getY()&&now_gameItem_view.getX()==null_gameItemView.getX()+1){//当前点击的方块在空方块的下面
           return true;
       }else if(null_gameItemView.getY()==now_gameItem_view.getY()+1&&now_gameItem_view.getX()==null_gameItemView.getX()){//当前点击的方块在空方块的左面
           return true;
       }else if(null_gameItemView.getY()+1==now_gameItem_view.getY()&&now_gameItem_view.getX()==null_gameItemView.getX()){ ////当前点击的方块在空方块的右面
           return true;
       }
        return false;
    }
    //随机打乱图片的顺序
    public void randomOrder(){
        //打乱的次数
        for (int i=0;i<5;i++){
            //根据手势，交换数据，无动画。
            int type = (int) (Math.random()*4)+1;
           // Log.i("sssssssssfdfdfd","交换次数"+i+"type的值"+type);
           changeByDirGes(type, false);
        }
    }
    /**
     * 判断游戏结束的方法
     */
    public void isGameWin(){
        //游戏胜利标志
        boolean isGameWin =true;
        //遍历每个小方块
        for (i = 0; i <iv_game_arr.length; i++){
            for (j = 0; j <iv_game_arr[0].length; j++){
                //为空的方块不判断 跳过
                if (iv_game_arr[i][j]==iv_null_imagview){
                    continue;
                }
                GameItemView gameItemView= (GameItemView) iv_game_arr[i][j].getTag();
                if (!gameItemView.isTrue()){
                    isGameWin=false;
                    //跳出内层循环
                    break;
                }
            }
            if (!isGameWin){
                //跳出外层循环
                break;
            }
        }
        //根据一个开关变量觉得游戏是否结束，结束时给提示。
        if (isGameWin){
           // Toast.makeText(this,"游戏胜利",Toast.LENGTH_SHORT).show();
            ToastUtil.makeText(this,"恭喜你，游戏胜利，用了"+step+"步",ToastUtil.LENGTH_SHORT,ToastUtil.SUCCESS);
            step=0;

        }
    }
    /**
     * 判断图片高是否大于宽 如果大于则要旋转图片
     * @return
     */
    public boolean isHeigthBigWidth(Bitmap bitmap) {
        int width= bitmap.getWidth();
        int heigth=bitmap.getHeight();
        if (heigth>width){
            return true;
        }else {
            return false;
        }

    }
}
