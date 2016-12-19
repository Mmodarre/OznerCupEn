package com.ozner.qianye.Command;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by ozner_67 on 2016/4/22.
 */
public class NoticeUtil {
    public static void notice(Context context) {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
                player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                player.setLooping(false);
                player.prepare();
                player.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
