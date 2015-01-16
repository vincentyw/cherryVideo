package com.example.cherryvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tv.matchstick.flint.Flint;
import tv.matchstick.flint.MediaStatus;
import tv.matchstick.flint.RemoteMediaPlayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.Menu;
import android.view.View;

public class PlayerActivity extends ActionBarActivity implements
FlintStatusChangeListener {

	//Flint SDK
	private static final int PLAYER_STATE_NONE = 0;
	private static final int PLAYER_STATE_PLAYING = 1;
	private static final int PLAYER_STATE_PAUSED = 2;
	private static final int PLAYER_STATE_BUFFERING = 3;
	private static final int REFRESH_INTERVAL_MS = (int) TimeUnit.SECONDS.toMillis(1);
	
	private boolean mSeeking;
	private boolean mIsUserSeeking;
	private int mPlayerState;
	protected Handler mHandler;
	private Runnable mRefreshRunnable;
	private FlintVideoManager mFlintVideoManager;

	/** Called when the activity is first created. */
	private SurfaceView  surfaceview;
	private ListView listview;
	private SeekBar seekbar;  
	private Player mplayer;
	private TextView tvTitle, tvTime;

	//video list
	private List<String> mVideoList = new ArrayList<String>();
	private String mUrl="http://fling.infthink.com/droidream/samples/yuanyexiaoyingxiong.mp4";
	private String mTitle = new String();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.util.Log.d("XXXXXXXXXX", "onCreate");

		setContentView(R.layout.activity_player);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		findView();
		videoList();
		listener();

		mHandler = new Handler();

		String applicationId = "~flintplayer";  //TODO: change app name
		Flint.FlintApi.setApplicationId(applicationId);
		mFlintVideoManager = new FlintVideoManager(this, applicationId, this);

		setupControls();

		mRefreshRunnable = new Runnable() {
			@Override
			public void run() {
				android.util.Log.d("XXXXXXXXXX", "mSeeking = " + mSeeking);
				if (!mSeeking) {
					android.util.Log.d("XXXXXXXXXX", "mFlintVideoManager.getMediaCurrentTime() = " + mFlintVideoManager.getMediaCurrentTime());

					refreshPlaybackPosition(mFlintVideoManager.getMediaCurrentTime(), mFlintVideoManager.getMediaDuration());
				}
				updateButtonStates();
				startRefreshTimer();
			}
		};
	}

	void videoList() {
		mVideoList.add("原野小英雄");
		mVideoList.add("怪物史莱克");
		mVideoList.add("虫虫特工");

		listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, mVideoList));
	}


	void findView() {
		listview = (ListView)this.findViewById(R.id.list_video);
		surfaceview=(SurfaceView)this.findViewById(R.id.cv_surfceview);
		seekbar=(SeekBar)this.findViewById(R.id.skbProgress);
		mplayer = new Player(this, surfaceview, seekbar);
		tvTitle = (TextView)this.findViewById(R.id.tvTitle);
		tvTime = (TextView)this.findViewById(R.id.tvTime);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void setupControls() {
		
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				mIsUserSeeking = false;
				seekbar.setSecondaryProgress(0);
				onSeekBarMoved(TimeUnit.SECONDS.toMillis(seekbar.getProgress()));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				mIsUserSeeking = true;
				seekbar.setSecondaryProgress(seekbar.getProgress());
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mIsUserSeeking = false;
	}
	
	private void onSeekBarMoved(long position) {
		if (!mFlintVideoManager.isMediaConnectioned())
			return;
		
		refreshPlaybackPosition(position, -1);
		
		mSeeking = true;
		mFlintVideoManager.seekMedia(position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED);
	}
	
	private void refreshPlaybackPosition(long position, long duration) {
		android.util.Log.d("XXXXXXXXXX", "position = " + position
				+ "; duration = " + duration
				+ "; mIsUserSeeking = " + mIsUserSeeking);

		if (!mIsUserSeeking) {
			if (position == 0) {
				seekbar.setProgress(0);
			} else if (position > 0) {
				seekbar.setProgress((int) TimeUnit.MILLISECONDS.toSeconds(position));
			}
		}
		
		if (duration == 0) {
			seekbar.setMax(0);
		} else if (duration > 0) {
			if (!mIsUserSeeking) {
				seekbar.setMax((int) TimeUnit.MILLISECONDS.toSeconds(duration));
			}
		}
		android.util.Log.d("XXXXXXXXXX", "text = " + formatTime(position)+"/"+formatTime(duration));
		tvTime.setText(formatTime(position)+"/"+formatTime(duration));
	}
	
    private String formatTime(long millisec) {
        int seconds = (int) (millisec / 1000);
        int hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        int minutes = seconds / 60;
        seconds %= 60;

        String time;
        if (hours > 0) {
            time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            time = String.format("%d:%02d", minutes, seconds);
        }
        return time;
    }
    
    private void updateButtonStates() {
        boolean hasDeviceConnection = mFlintVideoManager.isDeviceConnectioned();
        boolean hasAppConnection = mFlintVideoManager.isAppConnectioned();
        boolean hasMediaConnection = mFlintVideoManager.isMediaConnectioned();
        boolean hasMedia = false;

        if (hasMediaConnection) {
            MediaStatus mediaStatus = mFlintVideoManager.getMediaStatus();
            if (mediaStatus != null) {
                int mediaPlayerState = mediaStatus.getPlayerState();
                int playerState = PLAYER_STATE_NONE;
                if (mediaPlayerState == MediaStatus.PLAYER_STATE_PAUSED) {
                    playerState = PLAYER_STATE_PAUSED;
                } else if (mediaPlayerState == MediaStatus.PLAYER_STATE_PLAYING) {
                    playerState = PLAYER_STATE_PLAYING;
                } else if (mediaPlayerState == MediaStatus.PLAYER_STATE_BUFFERING) {
                    playerState = PLAYER_STATE_BUFFERING;
                }
                setPlayerState(playerState);

                hasMedia = mediaStatus.getPlayerState() != MediaStatus.PLAYER_STATE_IDLE;
            }
        } else {
            setPlayerState(PLAYER_STATE_NONE);
        }

        setSeekBarEnabled(hasMediaConnection && hasMedia);
    }
    
    private void setSeekBarEnabled(boolean enabled) {
    	seekbar.setEnabled(enabled);
    }
    
    private void setPlayerState(int playerState) {
        mPlayerState = playerState;
    }

	void listener() {
		listview.setOnItemClickListener(new ItemClickListener());
	}

	class ItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0:
				mUrl="http://fling.infthink.com/droidream/samples/yuanyexiaoyingxiong.mp4";
				mTitle="原野小英雄";
				break;
			case 1:
				mUrl="http://fling.infthink.com/droidream/samples/guaiwushilaike.mp4";
				mTitle="怪物史莱克";
				break;
			case 2:
				mUrl="http://fling.infthink.com/droidream/samples/chongchongtegong.mp4";
				mTitle="虫虫小特工";
				break;
			default:
				break;
			}
			tvTitle.setText(mTitle);
		}
	}
	
    private void clearMediaState() {
        refreshPlaybackPosition(0, 0);
    }

    protected final void startRefreshTimer() {
        mHandler.postDelayed(mRefreshRunnable, REFRESH_INTERVAL_MS);
    }

    protected final void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
		android.util.Log.d("XXXXXXXXXX", "onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
		android.util.Log.d("XXXXXXXXXX", "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
		android.util.Log.d("XXXXXXXXXX", "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
		android.util.Log.d("XXXXXXXXXX", "onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
		android.util.Log.d("XXXXXXXXXX", "onDestroy");

        mFlintVideoManager.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mFlintVideoManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
		clearMediaState();
		updateButtonStates();
	}

	@Override
	public void onConnectionFailed() {
		updateButtonStates();
		clearMediaState();
		cancelRefreshTimer();
	}

	@Override
	public void onConnected() {
		mFlintVideoManager.launchApplication(); 
	}

	@Override
	public void onNoLongerRunning(boolean isRunning) {
        if (isRunning) {
            startRefreshTimer();
        } else {
            clearMediaState();
            updateButtonStates();
        }
	}

	@Override
	public void onConnectionSuspended() {
        cancelRefreshTimer();
        updateButtonStates();
	}

	@Override
	public void onMediaStatusUpdated() {
        MediaStatus mediaStatus = this.mFlintVideoManager.getMediaStatus();
        if ((mediaStatus != null)
                && (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE)) {
            clearMediaState();
        }

        refreshPlaybackPosition(mFlintVideoManager.getMediaCurrentTime(),
                mFlintVideoManager.getMediaDuration());
        updateButtonStates();	
    }

	@Override
	public void onMediaMetadataUpdated(String title, String artist, Uri imageUrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onApplicationConnectionResult(String applicationStatus) {
		if (!TextUtils.isEmpty(mUrl))
			mFlintVideoManager.loadMedia(mUrl);
        startRefreshTimer();
        updateButtonStates();
	}

	@Override
	public void onLeaveApplication() {
        updateButtonStates();
	}

	@Override
	public void onStopApplication() {
        updateButtonStates();
	}

	@Override
	public void onMediaSeekEnd() {
        mSeeking = false;
	}

	@Override
	public void onMediaVolumeEnd() {
		// TODO Auto-generated method stub

	}
}