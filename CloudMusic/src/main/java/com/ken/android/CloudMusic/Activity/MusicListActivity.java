package com.ken.android.CloudMusic.Activity;


import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.FragmentManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;


import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.ken.android.CloudMusic.Adapter.MyAdapter;
import com.ken.android.CloudMusic.Config;
import com.ken.android.CloudMusic.Dialog.CollectDialog;
import com.ken.android.CloudMusic.FilesRead.ListsInfo;
import com.ken.android.CloudMusic.FilesRead.MusicInfo;
import com.ken.android.CloudMusic.Fragment.List_Fragment;
import com.ken.android.CloudMusic.Fragment.SystemListFragment;
import com.ken.android.CloudMusic.PlayerService;
import com.ken.android.CloudMusic.R;
import com.ken.android.CloudMusic.Service.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by axnshy on 16/5/20.
 */
public class MusicListActivity extends AppCompatActivity implements View.OnClickListener, List_Fragment.OnItemClickListener,SystemListFragment.OnItemClickListener, Observer, MyAdapter.OnItemMenuClickListener {


    private LinearLayout musicListLayout;
    private ImageView repeatImg;
    private ImageView previousImg;
    private ImageView playImg;
    private ImageView nextImg;
    private ImageView shuffleImg;
    private Context mContext;
    private Toolbar toolbar;
    private ImageView returnImg;
    private TextView listTitle;
    private CoordinatorLayout coordinator;
    private TextView musicNameTx;
    private TextView commentCount;
    private TextView singerName;
    private TextView albumName;
    private LinearLayout nextLayout;
    private LinearLayout collectLayout;
    private LinearLayout downloadLayout;
    private LinearLayout commentLayout;
    private LinearLayout shareLayout;
    private LinearLayout deleteLayout;
    private LinearLayout singerLayout;
    private LinearLayout albumLayout;
    private LinearLayout playerBarLayout;

    private BottomSheetDialog bottomSheetDialog;

    WindowManager.LayoutParams params;
    Window window;

    private boolean PlayerBarToken;
    //绑定service与activity
    private PlayerService mService;

    List<ListsInfo> ListsList;
    int CurrentListPosition;

    // 定义ServiceConnection
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 通过定义的Binder来获取Service实例来供使用
            mService = ((PlayerService.MyBinder) service).getService();
            Log.w(PlayerService.LOG_TAG, "Activity onServiceConnected");
            mService.addObserver(MusicListActivity.this);
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            // 当Service被意外销毁时
            Log.w(PlayerService.LOG_TAG, "Activity onServiceDisconnected");
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // bindService
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.w(PlayerService.LOG_TAG, "Activity bindService");
        // updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 进行unbind
        unbindService(conn);
        Log.w(PlayerService.LOG_TAG, "Activity unbindService");
    }


    private int selectItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        Intent intent=getIntent();
        ListsList=intent.getParcelableArrayListExtra("ListsList");
        CurrentListPosition=intent.getIntExtra(Config.LIST,-1);
        mContext = this;
        params = getWindow().getAttributes();
        window = getWindow();
        initView();
        initEvent();
        PlayerBarToken=true;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void initView() {
        coordinator = (CoordinatorLayout) findViewById(R.id.layout_coordinator);
        musicListLayout = (LinearLayout) coordinator.findViewById(R.id.layout_list_activity);
        returnImg = (ImageView) coordinator.findViewById(R.id.iv_back_list_fragment);
        repeatImg = (ImageView) coordinator.findViewById(R.id.iv_media_repeat);
        previousImg = (ImageView) coordinator.findViewById(R.id.iv_media_previous);
        playImg = (ImageView) coordinator.findViewById(R.id.iv_media_play);
        nextImg = (ImageView) coordinator.findViewById(R.id.iv_media_next);
        shuffleImg = (ImageView) coordinator.findViewById(R.id.iv_media_shuffle);
        toolbar = (Toolbar) coordinator.findViewById(R.id.toolbar_list_fragment);
        listTitle = (TextView) coordinator.findViewById(R.id.tv_toolbar_title);
        playerBarLayout= (LinearLayout) findViewById(R.id.music_playerBarInApp);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        setSupportActionBar(toolbar);

        musicListLayout.setVisibility(View.VISIBLE);
        Log.w("TAG", "musicname-----------" + musicNameTx + "comment------------ " + commentCount);
        if(CurrentListPosition>-1) {
            List_Fragment fragment = new List_Fragment();

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.FL_list_fragment
                    , fragment).commit();
            Bundle bundle = new Bundle();
            if (CurrentListPosition >= 0) {
                String name = ListsList.get(CurrentListPosition).getListName();
                listTitle.setText(name);
            }
            bundle.putParcelableArrayList("ListsList", (ArrayList<? extends Parcelable>) ListsList);
            bundle.putInt(Config.LIST, CurrentListPosition);
            fragment.setArguments(bundle);
        }
        else {
            SystemListFragment fragment = new SystemListFragment();

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.FL_list_fragment
                    , fragment).commit();
            Bundle bundle = new Bundle();
            if (CurrentListPosition >= 0) {
                String name = ListsList.get(CurrentListPosition).getListName();
                listTitle.setText(name);
            }
            bundle.putParcelableArrayList("ListsList", (ArrayList<? extends Parcelable>) ListsList);
            fragment.setArguments(bundle);
        }
    }

    private void initEvent() {
        playImg.setOnClickListener(this);
        returnImg.setOnClickListener(this);
        previousImg.setOnClickListener(this);
        nextImg.setOnClickListener(this);
        repeatImg.setOnClickListener(this);
        shuffleImg.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        //判断Service是否存在

        if (!Service.isMyServiceRunning(mContext, PlayerService.class)) {
            Intent serviceintent = new Intent(mContext, PlayerService.class);
            startService(serviceintent);
            Log.e("TAGS", "MediaPlayerService does not existed");
        } else {
            Log.e("TAGS", "MediaPlayerService existed");

        }
        switch (v.getId()) {
            case R.id.iv_media_repeat:
                if (mService.repeatTag == 0) {
                    repeatImg.setBackgroundResource(R.drawable.basecopy);
                    mService.repeatTag = 1;
                } else {
                    repeatImg.setBackgroundResource(R.drawable.base);
                    mService.repeatTag = 0;
                }
                break;
            case R.id.iv_media_shuffle:
                if (mService.shuffleTag == 0) {
                    shuffleImg.setBackgroundResource(R.drawable.basecopy);
                    mService.shuffleTag = 1;
                } else {
                    shuffleImg.setBackgroundResource(R.drawable.base);
                    mService.shuffleTag = 0;
                }
                break;
            case R.id.iv_media_previous:

                mService.play(mService.getPreviousMusic());
                break;
            case R.id.iv_media_next:
                mService.play(mService.getNextMusic());
                break;
            case R.id.iv_media_play: {
                if (mService.getPlayerState() == mService.MediaPlayer_PLAY) {
                    mService.pause();
                    mService.setPlayerState(mService.MediaPlayer_PAUSE);
                } else {
                    mService.getPlayerInstance().start();
                    mService.setPlayerState(mService.MediaPlayer_PLAY);
                }
                break;
            }
            case R.id.iv_back_list_fragment:
                onBackPressed();
                break;
        }
        updateUI();
    }

    private void updateUI() {
        //PlayerState代表播放状态
        switch (mService.getPlayerState()) {
            case 1: {
                playImg.setImageResource(R.drawable.play);
                //Toast.makeText(context, "music is prepared", Toast.LENGTH_SHORT).show();
                break;
            }
            case 2: {
                playImg.setImageResource(R.drawable.pause);
                //Toast.makeText(context, "music is playing", Toast.LENGTH_SHORT).show();
                break;
            }
            case 3: {
                playImg.setImageResource(R.drawable.play);
                //Toast.makeText(context, "music is paused", Toast.LENGTH_SHORT).show();
                break;

            }
        }
        if(PlayerBarToken==true&&PlayerService.PlayerState!=PlayerService.MediaPlayer_PLAY){
            ObjectAnimator.ofFloat(playerBarLayout,"translationY",0F,180F).setDuration(0).start();
            PlayerBarToken=false;
        }
    }

    @Override
    public void updateToolbar(String string) {
        //topMusicDisplay.setText(string);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public void update(Observable observable, Object data) {
        updateUI();
    }
    @Override
    public void showItemMenu(final MusicInfo music) {
        bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.item_menu, null);
        musicNameTx = (TextView) view.findViewById(R.id.tv_item_menu_music_name);
        commentCount = (TextView) view.findViewById(R.id.tv_item_menu_commentCount);
        singerName = (TextView) view.findViewById(R.id.tv_item_menu_singer);
        albumName = (TextView) view.findViewById(R.id.tv_item_menu_album);
        nextLayout = (LinearLayout) view.findViewById(R.id.menu_item_next);
        collectLayout = (LinearLayout) view.findViewById(R.id.menu_item_collect);
        downloadLayout = (LinearLayout) view.findViewById(R.id.menu_item_download);
        commentLayout = (LinearLayout) view.findViewById(R.id.menu_item_comment);
        shareLayout = (LinearLayout) view.findViewById(R.id.menu_item_share);
        deleteLayout = (LinearLayout) view.findViewById(R.id.menu_item_delete);
        singerLayout = (LinearLayout) view.findViewById(R.id.menu_item_singer);
        albumLayout = (LinearLayout) view.findViewById(R.id.menu_item_album);
        bottomSheetDialog.setContentView(view);
        musicNameTx.setText(music.getMusicName());
        commentCount.setText("(" + music.size + ")");
        singerName.setText(music.getMusicArtist());
        albumName.setText(music.album);
        collectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollectDialog dialog = new CollectDialog(mContext, ListsList, music);
                bottomSheetDialog.dismiss();
                dialog.show();

            }
        });
        bottomSheetDialog.show();
        Log.w("TAG", "it's a interface callback in MyAdapter");
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
            }
        }
    };
}