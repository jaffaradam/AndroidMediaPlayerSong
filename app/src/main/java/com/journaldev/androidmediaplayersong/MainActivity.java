package com.journaldev.androidmediaplayersong;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    MediaPlayer mediaPlayer = new MediaPlayer();
    SeekBar seekBar;
    boolean wasPlaying = false;
    FloatingActionButton fab;
    private static String mFileName = null;
    ImageView btnPlay;
    boolean fileStorage = false;
    final public static int PERMISSION_REQUEST_WRITE_EXTERNAL = 12;
    TextView txtTimestamp;
    Handler handler = new Handler();

    long total_seconds = 0;
    long secondsFor1Percent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);


        fab = findViewById(R.id.button);
        btnPlay = findViewById(R.id.btnOwnPlay);
        txtTimestamp = findViewById(R.id.txtTimeStamp);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/Music/Engengae.mp3";
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermission();
            }
        });

        seekBar = findViewById(R.id.seekbar);
        seekBar.setPadding(7, 0, 0, 0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

                Log.e("progresschanged", "" + progress);

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    MainActivity.this.seekBar.setProgress(0);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void checkStoragePermission() {
        int filePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (filePermission != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission((AppCompatActivity) MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_WRITE_EXTERNAL);
        } else {
            fileStorage = true;
        }

        if (fileStorage) {
            playSong();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkStoragePermission();
        }
    }

    private void showPauseMode() {
        btnPlay.setImageResource(R.drawable.audio_play);
    }

    private void showPlayMode() {
        btnPlay.setImageResource(R.drawable.audio_pause);
    }

    public void playSong() {

        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                showPauseMode();
                wasPlaying = true;
            }

            if (!wasPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }


                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(mFileName);
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInmillisec = Long.parseLong(time);
                total_seconds = timeInmillisec / 1000;

                /*long hours = duration / 3600;
                long minutes = (duration - hours * 3600) / 60;
                long seconds = duration - (hours * 3600 + minutes * 60);*/

                /*total_seconds = (minutes * 60) + seconds;*/
                secondsFor1Percent = total_seconds / 100;

                Log.e("MEDIA_LOG total_seconds: ", total_seconds + "");
                Log.e("MEDIA_LOG secondsFor1Percent: ", secondsFor1Percent + "");

                //AssetFileDescriptor descriptor = getAssets().openFd("chinnamachan.mp3");
                //descriptor.close();
                mediaPlayer.setDataSource(mFileName);
                mediaPlayer.prepare();
                //mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setLooping(false);
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                showPlayMode();

                Log.e("MEDIA_LOG timeInmillisec", "" + mediaPlayer.getDuration());


                updateSeekBar();
                //new Thread(this).start();


            }

            wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", "Exception" + e);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(runnable, 1000);
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        Log.e("MEDIA_LOG mediaPlayer.getCurrentPosition()","" + mediaPlayer.getCurrentPosition());

        txtTimestamp.setText(String.valueOf(getTimeLeft(mediaPlayer.getCurrentPosition())));

        //getTimeLeft(mediaPlayer.getCurrentPosition());
    }

    private String getTimeLeft(long completedMilliSeconds) {

        long completed_seconds = completedMilliSeconds / 1000;
        long seconds_remaining = (total_seconds - completed_seconds);
        long minute = seconds_remaining / 60;
        long seconds = (seconds_remaining % 60);

        String displayMinutes = String.format("%2s", minute).replace(' ', '0');
        String displaySeconds = String.format("%2s", seconds).replace(' ', '0');
                /*String.format("%1$-" + 2 + "s", seconds).replace(' ', '0');*/


        Log.e("MEDIA_LOG completed_seconds: ", completed_seconds + "");
        Log.e("MEDIA_LOG seconds_remaining: ", "" + seconds_remaining);

        Log.e("MEDIA_LOG displayMinutes: ", displayMinutes);
        Log.e("MEDIA_LOG displaySeconds: ", displaySeconds);

        return (displayMinutes + ":" + displaySeconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    /*public void run() {

        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();


        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();

                int min = currentPosition / 60;
                Log.e("SongDuration",""+min);

            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);

        }
    }*/
}
