
package com.kekousoft.flashnote;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;

import java.io.IOException;

public class VoiceHelper {

    private Context sContext;

    private MediaPlayer sMediaPlayer;

    private MediaRecorder sRecorder;

    private String sFilePrefix;

    public VoiceHelper(Context context) {
        sContext = context;
        sFilePrefix = context.getFilesDir().getPath() + "/";
    }

    public void playVoice(String voiceRecord) {
        Uri voiceUri = Uri.parse(sFilePrefix + voiceRecord);
        if (sMediaPlayer != null) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.reset();
        } else {
            sMediaPlayer = new MediaPlayer();
        }
        try {
            sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            sMediaPlayer.setDataSource(sContext, voiceUri);
            sMediaPlayer.prepare();
            sMediaPlayer.start();
            sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    sMediaPlayer.release();
                    sMediaPlayer = null;
                }
            });
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

    public void startRecording(String fileName) {
        stop(); // stop any other action before a recording.
        sRecorder = new MediaRecorder();
        sRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        sRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        sRecorder.setOutputFile(sFilePrefix + fileName);
        sRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            sRecorder.prepare();
            sRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finishRecording() {
        if (sRecorder != null) {
            sRecorder.stop();
            sRecorder.release();
            sRecorder = null;
        }
    }

    public void stopPlaying() {
        if (sMediaPlayer != null) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
    }

    public void stop() {
        stopPlaying();
        finishRecording();
    }
}
