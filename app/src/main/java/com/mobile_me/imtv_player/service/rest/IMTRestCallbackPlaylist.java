package com.mobile_me.imtv_player.service.rest;

import com.mobile_me.imtv_player.model.MTPlayList;

/**
 * Created by pasha on 07.03.18.
 */
public interface IMTRestCallbackPlaylist {
    void onPlaylistLoaded(MTPlayList playlist);
    void onError(Throwable t);
}
