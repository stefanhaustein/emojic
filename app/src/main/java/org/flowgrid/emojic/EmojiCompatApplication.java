package org.flowgrid.emojic;

import android.app.Application;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;

public class EmojiCompatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final EmojiCompat.Config config = new BundledEmojiCompatConfig(getApplicationContext());
        EmojiCompat.init(config);
    }

}
