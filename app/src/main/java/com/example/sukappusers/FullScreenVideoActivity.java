package com.example.sukappusers;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.concurrent.TimeUnit;

public class FullScreenVideoActivity extends BaseActivity {

    private VideoView fullScreenVideoView;
    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private ImageButton fullScreenButton;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);

        fullScreenVideoView = findViewById(R.id.fullScreenVideoView);
        playPauseButton = findViewById(R.id.playPauseButton);
        seekBar = findViewById(R.id.seekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        fullScreenButton = findViewById(R.id.fullScreenButton);
        fullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit);


        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String videoPath = getIntent().getStringExtra("videoPath");
        int currentPosition = getIntent().getIntExtra("currentPosition", 0);

        if (videoPath != null) {
            Uri videoUri = Uri.parse(videoPath);
            fullScreenVideoView.setVideoURI(videoUri);
            fullScreenVideoView.start();
        }

        playPauseButton.setOnClickListener(v -> togglePlayPause());

        fullScreenButton.setOnClickListener(v -> onBackPressed());


        fullScreenVideoView.setOnPreparedListener(mp -> {
            fullScreenVideoView.start();
            fullScreenVideoView.seekTo(currentPosition);
            playPauseButton.setImageResource(R.drawable.ic_pause);
            isPlaying = true;
            seekBar.setMax(fullScreenVideoView.getDuration());
            updateTimeLabels();
        });

        fullScreenVideoView.setOnClickListener(v -> toggleMediaControlsVisibility());

        fullScreenVideoView.setOnCompletionListener(mp -> {
            fullScreenVideoView.seekTo(0);
            playPauseButton.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        });

        fullScreenVideoView.setOnErrorListener((mp, what, extra) -> {
            playPauseButton.setImageResource(R.drawable.ic_play);
            isPlaying = false;
            return false;
        });

        fullScreenVideoView.setOnInfoListener((mp, what, extra) -> {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                seekBar.post(() -> {
                    seekBar.setMax(fullScreenVideoView.getDuration());
                    startSeekBarUpdate();
                });
            }
            return false;
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    fullScreenVideoView.seekTo(progress);
                    updateTimeLabels();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseSeekBarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startSeekBarUpdate();
            }
        });
    }

    private void toggleMediaControlsVisibility() {
        if (playPauseButton.getVisibility() == View.VISIBLE) {
            // Hide media controls
            playPauseButton.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            currentTimeTextView.setVisibility(View.GONE);
            totalTimeTextView.setVisibility(View.GONE);
            fullScreenButton.setVisibility(View.GONE);
        } else {
            // Show media controls
            playPauseButton.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            currentTimeTextView.setVisibility(View.VISIBLE);
            totalTimeTextView.setVisibility(View.VISIBLE);
            fullScreenButton.setVisibility(View.VISIBLE);
        }
    }

    private void togglePlayPause() {
        if (isPlaying) {
            fullScreenVideoView.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            fullScreenVideoView.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
        isPlaying = !isPlaying;
    }

    private void startSeekBarUpdate() {
        seekBar.postDelayed(seekBarUpdateRunnable, 100);
    }

    private void pauseSeekBarUpdate() {
        seekBar.removeCallbacks(seekBarUpdateRunnable);
    }

    private final Runnable seekBarUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (fullScreenVideoView != null) {
                int currentPosition = fullScreenVideoView.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updateTimeLabels();
                seekBar.postDelayed(this, 100);
            }
        }
    };

    private void updateTimeLabels() {
        int currentTime = fullScreenVideoView.getCurrentPosition();
        int totalTime = fullScreenVideoView.getDuration();

        String currentTimeFormatted = formatTime(currentTime);
        String totalTimeFormatted = formatTime(totalTime);

        currentTimeTextView.setText(currentTimeFormatted);
        totalTimeTextView.setText(totalTimeFormatted);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onBackPressed() {
        fullScreenVideoView.pause(); // Pause the video playback
        Intent data = new Intent();
        data.putExtra("currentPosition", fullScreenVideoView.getCurrentPosition());
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }
}
