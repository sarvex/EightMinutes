/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.eightmins.eightminutes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaMetadata.Builder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.eightmins.eightminutes.PlaybackOverlayFragment.OnPlayPauseClickedListener;
import com.eightmins.eightminutes.R.id;
import com.eightmins.eightminutes.R.layout;
import com.eightmins.eightminutes.R.string;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackOverlayActivity extends Activity implements
    OnPlayPauseClickedListener {
  private static final String TAG = "PlaybackOverlayActivity";

  private VideoView mVideoView;
  private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;
  private MediaSession mSession;


  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(layout.playback_controls);
    this.loadViews();
    this.setupCallbacks();
    this.mSession = new MediaSession(this, "LeanbackSampleApp");
    this.mSession.setCallback(new MediaSessionCallback());
    this.mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

    this.mSession.setActive(true);
  }

  private void loadViews() {
    this.mVideoView = (VideoView) this.findViewById(id.videoView);
    this.mVideoView.setFocusable(false);
    this.mVideoView.setFocusableInTouchMode(false);
  }

  private void setupCallbacks() {

    this.mVideoView.setOnErrorListener(new OnErrorListener() {

      @Override
      public boolean onError(MediaPlayer mp, int what, int extra) {
        String msg = "";
        if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
          msg = PlaybackOverlayActivity.this.getString(string.video_error_media_load_timeout);
        } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
          msg = PlaybackOverlayActivity.this.getString(string.video_error_server_inaccessible);
        } else {
          msg = PlaybackOverlayActivity.this.getString(string.video_error_unknown_error);
        }
        PlaybackOverlayActivity.this.mVideoView.stopPlayback();
        PlaybackOverlayActivity.this.mPlaybackState = LeanbackPlaybackState.IDLE;
        return false;
      }
    });

    this.mVideoView.setOnPreparedListener(new OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        if (PlaybackOverlayActivity.this.mPlaybackState == LeanbackPlaybackState.PLAYING) {
          PlaybackOverlayActivity.this.mVideoView.start();
        }
      }
    });

    this.mVideoView.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        PlaybackOverlayActivity.this.mPlaybackState = LeanbackPlaybackState.IDLE;
      }
    });

  }

  @Override
  public void onResume() {
    super.onResume();
    this.mSession.setActive(true);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (this.mVideoView.isPlaying()) {
      if (!this.requestVisibleBehind(true)) {
        // Try to play behind launcher, but if it fails, stop playback.
        this.stopPlayback();
      }
    } else {
      this.requestVisibleBehind(false);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    this.mSession.release();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.mVideoView.suspend();
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) this.getFragmentManager().findFragmentById(id.playback_controls_fragment);
    switch (keyCode) {
      case KeyEvent.KEYCODE_MEDIA_PLAY:
        playbackOverlayFragment.togglePlayback(false);
        return true;
      case KeyEvent.KEYCODE_MEDIA_PAUSE:
        playbackOverlayFragment.togglePlayback(false);
        return true;
      case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        if (this.mPlaybackState == LeanbackPlaybackState.PLAYING) {
          playbackOverlayFragment.togglePlayback(false);
        } else {
          playbackOverlayFragment.togglePlayback(true);
        }
        return true;
      default:
        return super.onKeyUp(keyCode, event);
    }
  }

  @Override
  public void onVisibleBehindCanceled() {
    super.onVisibleBehindCanceled();
  }

  private void stopPlayback() {
    if (this.mVideoView != null) {
      this.mVideoView.stopPlayback();
    }
  }

  /**
   * Implementation of OnPlayPauseClickedListener
   */
  public void onFragmentPlayPause(Movie movie, int position, Boolean playPause) {
    this.mVideoView.setVideoPath(movie.getVideoUrl());

    if (position == 0 || this.mPlaybackState == LeanbackPlaybackState.IDLE) {
      this.setupCallbacks();
      this.mPlaybackState = LeanbackPlaybackState.IDLE;
    }

    if (playPause && this.mPlaybackState != LeanbackPlaybackState.PLAYING) {
      this.mPlaybackState = LeanbackPlaybackState.PLAYING;
      if (position > 0) {
        this.mVideoView.seekTo(position);
        this.mVideoView.start();
      }
    } else {
      this.mPlaybackState = LeanbackPlaybackState.PAUSED;
      this.mVideoView.pause();
    }
    this.updatePlaybackState(position);
    this.updateMetadata(movie);
  }

  private void updatePlaybackState(int position) {
    PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
        .setActions(this.getAvailableActions());
    int state = PlaybackState.STATE_PLAYING;
    if (this.mPlaybackState == LeanbackPlaybackState.PAUSED) {
      state = PlaybackState.STATE_PAUSED;
    }
    stateBuilder.setState(state, position, 1.0f);
    this.mSession.setPlaybackState(stateBuilder.build());
  }

  private void updateMetadata(Movie movie) {
    final Builder metadataBuilder = new Builder();

    String title = movie.getTitle().replace("_", " -");

    metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
    metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
        movie.getDescription());
    metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
        movie.getCardImageUrl());

    // And at minimum the title and artist for legacy support
    metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
    metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, movie.getStudio());

    Glide.with(this)
        .load(Uri.parse(movie.getCardImageUrl()))
        .asBitmap()
        .into(new SimpleTarget<Bitmap>(500, 500) {
          @Override
          public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
            PlaybackOverlayActivity.this.mSession.setMetadata(metadataBuilder.build());
          }
        });
  }

  private long getAvailableActions() {
    long actions = PlaybackState.ACTION_PLAY |
        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
        PlaybackState.ACTION_PLAY_FROM_SEARCH;

    if (this.mPlaybackState == LeanbackPlaybackState.PLAYING) {
      actions |= PlaybackState.ACTION_PAUSE;
    }

    return actions;
  }

  /*
   * List of various states that we can be in
   */
  public enum LeanbackPlaybackState {
    PLAYING, PAUSED, BUFFERING, IDLE
  }

  private class MediaSessionCallback extends Callback {
  }
}
