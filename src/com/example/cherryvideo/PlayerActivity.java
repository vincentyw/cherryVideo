package com.example.cherryvideo;

import java.util.ArrayList;
import java.util.List;

import tv.matchstick.flint.Flint;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class PlayerActivity extends ActionBarActivity implements
FlintStatusChangeListener {
	/** Called when the activity is first created. */
	private SurfaceView  surfaceview;
	private ListView listview;
	private Button butonstop, buttonstart;
	private SeekBar skbProgress;  
	private Player mplayer;

	private int isPlaying = 0;
	//video list
	private List<String> mVideoList = new ArrayList<String>();
	private String mUrl = new String();
	
	//Flint SDK
	private FlintVideoManager mFlintVideoManager;

	//private Player; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		findView();
		videoList();
		listener();
		
        String applicationId = "~samplemediaplayer";  //TODO: change app name
        Flint.FlintApi.setApplicationId(applicationId);
        mFlintVideoManager = new FlintVideoManager(this, applicationId, this);
	}

	void videoList() {
		mVideoList.add("ElephantsDream");
		mVideoList.add("Godzilla");
		mVideoList.add("Sintel");

		listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, mVideoList));

	}


	void findView() {
		listview = (ListView)this.findViewById(R.id.list_video);
		surfaceview=(SurfaceView)this.findViewById(R.id.cv_surfceview);
		skbProgress=(SeekBar)this.findViewById(R.id.skbProgress);
		buttonstart = (Button) this.findViewById(R.id.btnPlay);  
		butonstop=(Button)this.findViewById(R.id.btnStop);
		mplayer = new Player(this, surfaceview, skbProgress);
	}

	void listener() {
		skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
		buttonstart.setOnClickListener(new ClickEvent());  
		butonstop.setOnClickListener(new ClickEvent()); 
		listview.setOnItemClickListener(new ItemClickListener());
	}

	class ItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0:
				mUrl="http://fling.infthink.com/droidream/samples/ElephantsDream.mp4";
				break;
			case 1:
				mUrl="http://fling.infthink.com/droidream/samples/Godzilla.mp4";
				break;
			case 2:
				mUrl="http://fling.infthink.com/droidream/samples/Sintel.mp4";
				break;
			default:
				break;
			}
		}
	}

	class ClickEvent implements OnClickListener{
		@Override  
		public void onClick(View arg0) {  
			if (arg0 == buttonstart) {  
				if ( 0 == isPlaying ) {
					isPlaying = 1;
					buttonstart.setText("暂停");
					mplayer.playUrl(mUrl);
					mFlintVideoManager.launchApplication();   //TO DO
				} else {
					isPlaying = 0;
					buttonstart.setText("播放");
					mplayer.pause();
				}
			} else if (arg0 == butonstop) {  
				mplayer.stop();  
			}  
		}  
	}

	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener{
		int progress;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			this.progress = progress * mplayer.mediaPlayer.getDuration() / seekBar.getMax(); 
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			mplayer.mediaPlayer.seekTo(progress);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mFlintVideoManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }
	
	@Override
	public void onDeviceSelected(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceUnselected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVolumeChanged(double percent, boolean muted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onApplicationStatusChanged(String status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onApplicationDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNoLongerRunning(boolean isRunning) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuspended() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaStatusUpdated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaMetadataUpdated(String title, String artist, Uri imageUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onApplicationConnectionResult(String applicationStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeaveApplication() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopApplication() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaSeekEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaVolumeEnd() {
		// TODO Auto-generated method stub
		
	}
}