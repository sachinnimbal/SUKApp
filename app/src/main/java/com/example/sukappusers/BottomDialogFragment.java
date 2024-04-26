package com.example.sukappusers;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.TimeUnit;

public class BottomDialogFragment extends BottomSheetDialogFragment {

    private VideoView videoView;
    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private ImageButton fullScreenButton;
    private boolean isPlaying = false;
    private static final int REQUEST_CODE_FULLSCREEN = 1;
    ImageView close;
    MaterialButton btn_ReadMore;

    public BottomDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_dialog, container, false);

        videoView = view.findViewById(R.id.videoView);
        playPauseButton = view.findViewById(R.id.playPauseButton);
        seekBar = view.findViewById(R.id.seekBar);
        currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
        totalTimeTextView = view.findViewById(R.id.totalTimeTextView);
        fullScreenButton = view.findViewById(R.id.fullScreenButton);
        close = view.findViewById(R.id.close);

        String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.video; // Replace "your_video" with the actual name of your video file in the raw folder
        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);

        playPauseButton.setOnClickListener(v -> togglePlayPause());
        btn_ReadMore = view.findViewById(R.id.btn_ReadMore);

        btn_ReadMore.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), KnowAppActivity.class);
            intent.putExtra("appName", "SUK App");
            intent.putExtra("appDescription", "Department of MCA");
            startActivity(intent);
        });


        fullScreenButton.setOnClickListener(v -> {
            int currentPosition = videoView.getCurrentPosition();
            Intent intent = new Intent(getActivity(), FullScreenVideoActivity.class);
            intent.putExtra("videoPath", videoPath);
            intent.putExtra("currentPosition", currentPosition);
            startActivityForResult(intent, REQUEST_CODE_FULLSCREEN);
        });

        close.setOnClickListener(view1 -> {
            dismiss();
        });

        videoView.setOnClickListener(v -> toggleMediaControlsVisibility());

        videoView.setOnPreparedListener(mp -> {
            videoView.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            isPlaying = true;
            seekBar.setMax(videoView.getDuration());
            updateTimeLabels();
        });

        videoView.setOnCompletionListener(mp -> {
            videoView.seekTo(0);
            playPauseButton.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            playPauseButton.setImageResource(R.drawable.ic_play);
            isPlaying = false;
            return false;
        });

        videoView.setOnInfoListener((mp, what, extra) -> {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                seekBar.post(() -> {
                    seekBar.setMax(videoView.getDuration());
                    startSeekBarUpdate();
                });
            }
            return false;
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
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

        return view;
    }

    private void togglePlayPause() {
        if (isPlaying) {
            videoView.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            videoView.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
        isPlaying = !isPlaying;
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


    private void startSeekBarUpdate() {
        seekBar.postDelayed(seekBarUpdateRunnable, 100);
    }

    private void pauseSeekBarUpdate() {
        seekBar.removeCallbacks(seekBarUpdateRunnable);
    }

    private final Runnable seekBarUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (videoView != null) {
                int currentPosition = videoView.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updateTimeLabels();
                seekBar.postDelayed(this, 100);
            }
        }
    };

    private void updateTimeLabels() {
        int currentTime = videoView.getCurrentPosition();
        int totalTime = videoView.getDuration();

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
    public void onDestroyView() {
        super.onDestroyView();
        pauseSeekBarUpdate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FULLSCREEN && resultCode == RESULT_OK && data != null) {
            int currentPosition = data.getIntExtra("currentPosition", 0);
            // Use the currentPosition as needed
            videoView.seekTo(currentPosition);
        }
    }

}
