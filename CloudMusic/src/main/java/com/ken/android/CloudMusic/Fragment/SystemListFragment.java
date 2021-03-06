package com.ken.android.CloudMusic.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ken.android.CloudMusic.Activity.MusicListActivity;
import com.ken.android.CloudMusic.Activity.MusicPlayingActivity;
import com.ken.android.CloudMusic.Adapter.MyAdapter;
import com.ken.android.CloudMusic.Config;
import com.ken.android.CloudMusic.DBHelper.MusicInfoDao;
import com.ken.android.CloudMusic.FilesRead.ListsInfo;
import com.ken.android.CloudMusic.FilesRead.MusicInfo;
import com.ken.android.CloudMusic.PlayerService;
import com.ken.android.CloudMusic.R;
import com.ken.android.CloudMusic.Service.Service;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by axnshy on 16/8/12.
 */
@ContentView(R.layout.custom_list_fragment)
public class SystemListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private MusicListActivity activity;
    private View view;
    private ArrayList<MusicInfo> mList;
    private MyAdapter mAdapter;
    private ListView mListView;
    private PlayerService mService;
    private List<ListsInfo> ListsList;
    private int listId;
    OnItemClickListener mListener;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    mList = (ArrayList<MusicInfo>) msg.obj;
                    mAdapter = new MyAdapter(view.getContext(), activity, mList);
                    System.out.println("mAdapter" + mAdapter);
                    mListView.setAdapter(mAdapter);
                    break;
            }
        }
    };


    // 定义ServiceConnection
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 通过定义的Binder来获取Service实例来供使用
            mService = ((PlayerService.MyBinder) service).getService();
            Log.w(PlayerService.LOG_TAG, "Activity onServiceConnected");
            Log.w("TAG", "Service   -------------------------   " + mService);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            // 当Service被意外销毁时
            Log.w(PlayerService.LOG_TAG, "Activity onServiceDisconnected");
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.custom_list_fragment, container, false);
        super.onCreateView( inflater,  container,  savedInstanceState);
        Bundle bundle = getArguments();
        ListsList = bundle.getParcelableArrayList("ListsList");
        initView();
        initList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initList() {
        // mList=new ArrayList<>();
        Log.e("TAG", "Service   --------------------------    " + mService);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg=Message.obtain();
                msg.what=0;
                msg.obj= MusicInfoDao.getAllMusic(view.getContext());
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        // bindService
        Intent intent = new Intent(view.getContext(), PlayerService.class);
        view.getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.w(PlayerService.LOG_TAG, "Activity bindService");
    }


    @Override
    public void onPause() {
        super.onPause();
        // 进行unbind
        view.getContext().unbindService(conn);
        Log.w(PlayerService.LOG_TAG, "Activity unbindService");
    }

    private void initView() {

        mListView = (ListView) view.findViewById(R.id.lv_fragment_musicList);
        mListView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View mview, int position, long id) {
        //判断Service是否存在
        if (!Service.isMyServiceRunning(view.getContext(), PlayerService.class)) {
            Intent serviceintent = new Intent(view.getContext(), PlayerService.class);
            view.getContext().startService(serviceintent);
            Log.e("TAGS", "MediaPlayerService does not existed");
        }
        mService.setPlayerList(mList);
        mService.play(mList.get(position));
        //mListener.updateToolbar(mService.getMyList().get(position).musicName);
        Intent intent = new Intent(view.getContext(), MusicPlayingActivity.class);
        startActivity(intent);
    }


    /*
    * 接口回调,在父Activity中实现该方法,在fragment中想要回调的地方调用mLister的方法
    * */
    public interface OnItemClickListener {
        public void updateToolbar(String string);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mListener == null)
            mListener = (OnItemClickListener) activity;
        this.activity = (MusicListActivity) activity;
    }
}
