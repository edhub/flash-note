
package com.kekousoft.flashnote;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;

import java.io.IOException;

public class VoiceHelper {

    private Context mContext;

    private MediaPlayer mMediaPlayer;

    private MediaRecorder mRecorder;

    private String mFilePrefix;

    public VoiceHelper(Context context) {
        mContext = context;
        mFilePrefix = context.getFilesDir().getPath() + "/";
    }

    public void playVoice(final String voiceRecord) {
        Uri voiceUri = Uri.parse(mFilePrefix + voiceRecord);
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mContext, voiceUri);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            });
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecording(final String fileName) {
        stopPlaying();
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilePrefix + fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void finishRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
        }
    }

    public void stopPlaying() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void stop() {
        stopPlaying();
        finishRecording();
    }
}
