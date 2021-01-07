package com.cheers.cheers;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private int i=0;        //잔 카운팅
    private TextView CupText;   //잔 카운팅 텍스트뷰

    private LinearLayout linearLayout;  //캡쳐화면뜰 레이아웃
    private LinearLayout LinearView;    //전체화면

    private static final int REQUEST_CODE = 0;      //갤러리
    private static final int PICK_FROM_CAMERA = 1;  //카메라

    private File dir;

    //잔맥주, 소주, 칵테일, 병맥주
    private int leftimage[] = {R.drawable.left, R.drawable.sojuleft, R.drawable.cocktailleft, R.drawable.bottleleft};
    private int rightimage[] = {R.drawable.right, R.drawable.sojuright, R.drawable.cocktailright, R.drawable.bottleright};
    private int index = 1;    //이미지 교체 인덱스

    private InterstitialAd mInterstitialAd;     //전면광고/

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //광고
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //SoundManager.java를 불러옴
        SoundManager.getInstance();
        SoundManager.initSound(this);
        SoundManager.loadSounds();

        //애니메이션 선언
        final Animation animTransLeft = AnimationUtils.loadAnimation(this, R.anim.anim_scale_left);
        final Animation animTransRight = AnimationUtils.loadAnimation(this, R.anim.anim_scale_right);
        final Animation animTransClick = AnimationUtils.loadAnimation(this, R.anim.anim_scale_click);
        final Animation animTransFadein = AnimationUtils.loadAnimation(this, R.anim.anim_fadein);
        //이미지 선언
        ImageView RightImageView =findViewById(R.id.RightImageView);
        ImageView LeftImageView =findViewById(R.id.LeftImageView);
        //백그라운드 레이아웃
        linearLayout = findViewById(R.id.linearLayout);
        //전체 레이아웃 화면
        LinearView = findViewById(R.id.LinearView);
        //텍스트 선언
        CupText = findViewById(R.id.CupText);

        //이미지 저장 경로
        //이미지경로, 피일
        String address = Environment.getExternalStorageDirectory() + "/android/data" + "/.jpeg";
        dir = new File(address);
        //카메라 권한 체크
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("Camera permission is required.")
                .setDeniedMessage("You have denied camera permissions.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();


        //Click버튼
        ImageView ClickView =findViewById(R.id.ClickView);
        ClickView.startAnimation(animTransClick);

        //Cheers! 버튼
        findViewById(R.id.SoundButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickView.setAlpha(0);
                //애니메이션
                LeftImageView.startAnimation(animTransLeft);
                RightImageView.startAnimation(animTransRight);

            if (index==0){      //맥주
                //앞 숫자는 노래목록, 뒤의 숫자는 속도
                SoundManager.playSound(1, 1);
            } else if (index==1){       //소주
                SoundManager.playSound(3, 1);
            }else if (index==2){        //칵테일
                SoundManager.playSound(3, 1);
            }else {         //병맥주
                SoundManager.playSound(2, 1);
            }

                //잔 카운트뷰 애니메이션
                ObjectAnimator animator = ObjectAnimator.ofFloat(CupText, "rotationX", 0, -360);   //CupText가 y축 위로 360도 회전
                animator.setDuration(500);
                animator.start();
                //잔 카운팅 메소드
                onButtonClick(true);
            }
        });

        //reset버튼
        findViewById(R.id.ResetLinear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(CupText, "rotationX", 0, 360);   //CupText가 x축 위로 360도 회전
                animator.setDuration(500);
                animator.start();
                onButtonClick(false);
            }
        });

        //퍼미션 체크
        checkPermission();
        //인스타 share버튼
        LinearLayout InstaLinear =findViewById(R.id.InstaLinear);
        InstaLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //인스타share
                share();
            }
        });

        //체인지 버튼
        findViewById(R.id.changeLinear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //잔 바뀔 때 애니메이션
                LeftImageView.startAnimation(animTransFadein);
                RightImageView.startAnimation(animTransFadein);

                if (index < leftimage.length) {
                    LeftImageView.setImageResource(leftimage[index]);
                    RightImageView.setImageResource(rightimage[index]);
                    ++index;
                }else {
                    index =0;
                    LeftImageView.setImageResource(leftimage[0]);
                    RightImageView.setImageResource(rightimage[0]);
                    index++;
                }
            }
        });

        //백그러운드 메뉴
        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(),v);
                getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_menu1){
                            //사진촬영
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_FROM_CAMERA);
                        }else if (menuItem.getItemId() == R.id.action_menu2){
                            //갤러리
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent, REQUEST_CODE);
                        }else {
                            //하얀색으로
                            LinearView.setBackgroundColor(Color.WHITE);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        //데이터 불러오기
        SharedPreferences preferences = getSharedPreferences("file", Context.MODE_PRIVATE);
        String CupTextView = preferences.getString("CupTextView", "");
        i = preferences.getInt("CheersCount", 0);
        CupText.setText(CupTextView);

        //-- admob 전면광고 setting
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9457112413608323/2075694926");
        //광고 이벤트리스너 등록
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("TAGHSS", "onAdLoaded");
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d("TAGHSS", "onAdFailedToLoad");
            }
            @Override
            public void onAdOpened() {
                Log.d("TAGHSS", "onAdOpened");
            }
            @Override
            public void onAdClicked() {
                Log.d("TAGHSS", "onAdClicked");
            }
            @Override
            public void onAdLeftApplication() {
                Log.d("TAGHSS", "onAdLeftApplication");
            }
            @Override
            public void onAdClosed() {
                Log.d("TAGHSS", "onAdClosed");
                // Code to be executed when the interstitial ad is closed.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                finish();
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //갤러리
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    Bitmap img = BitmapFactory.decodeStream(in);
                    Drawable DrawableImg = new BitmapDrawable(getResources(), img);    //bitmap을 drawable로 형변환
                    in.close();

                    //투명도0~255
                    DrawableImg.setAlpha(204);
                    LinearView.setBackground(DrawableImg);
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancel", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK &&data.hasExtra("data")){
            //카메라
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Drawable CaptureImg = new BitmapDrawable(getResources(), bitmap);    //bitmap을 drawable로 형변환
            if (bitmap != null) {
                //투명도0~255
                CaptureImg.setAlpha(204);
                LinearView.setBackground(CaptureImg);
            }
        }

    }


    //인스타 share
    private void share(){
        //전체화면 스크린샷
        //View container = getWindow().getDecorView();
        //linearLayout 스크린샷
        linearLayout.buildDrawingCache();
        Bitmap captureView = linearLayout.getDrawingCache();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(dir);
            captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        Uri uri = FileProvider.getUriForFile(getApplicationContext(),"com.cheers.cheers.fileprovider", dir);

        Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
        intent.setDataAndType(uri, "image/*");

        grantUriPermission("com.instagram.android",uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share"));
    }


    //갤러리 퍼미션 체크
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission(){
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }
    //카메라 퍼미션
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    };


    //ture(afterbtnw버튼)일 경우 i와 cnt 증가, false(beforebtnb버튼)일 경우 i와 cnt 감소
    private void onButtonClick(Boolean isPlus) {
        if (isPlus) {
            i ++;
            CupText.setText(" "+i+ " ");       //settext는 text를 바꾸는 함수
        } else {
            i =0;
            CupText.setText(" "+i+ " ");       //settext는 text를 바꾸는 함수
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        //데이터 저장
        SharedPreferences preferences = getSharedPreferences("file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String CupTextView = CupText.getText().toString();     //CupText저장

        editor.putInt("CheersCount", i);
        editor.putString("CupTextView", CupTextView);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.cleanup();
    }

    /* backkey 누르면 광고 뜨게 처리 */
    @Override
    public void onBackPressed() {

        if (mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        } else {
            Log.d("TAGHSS", "The interstitial wasn't loaded yet.");
        }

    }

}