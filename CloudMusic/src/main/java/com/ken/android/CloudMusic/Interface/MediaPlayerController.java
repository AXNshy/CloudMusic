package com.ken.android.CloudMusic.Interface;

import com.ken.android.CloudMusic.FilesRead.MusicInfo;

/**
 * Created by axnshy on 16/8/9.
 */
public interface MediaPlayerController {
    public abstract void play(MusicInfo musicInfo);
    public abstract void pause();
    public abstract void previous();
    public abstract void next();
    public abstract void repeat();
    public abstract void shuffle();
}
