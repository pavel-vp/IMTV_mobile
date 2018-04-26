package com.mobile_me.imtv_player.service;

import com.mobile_me.imtv_player.model.MTFileApkInfo;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;

/**
 * Created by pasha on 7/21/16.
 */
public interface IMTCallbackEvent {
    void onPlayListLoaded(MTPlayList playList, MTOwnCloudHelper ownCloudHelper);
    void onVideoFileLoaded(MTPlayListRec file, MTOwnCloudHelper ownCloudHelper);
    void onUpdateFileLoaded(MTOwnCloudHelper ownCloudHelper);
    void onError(int mode, MTOwnCloudHelper ownCloudHelper,  Throwable t);
    void onUploadLog(String uploadedLocalFile);
    void onGlobalSetupLoaded(MTOwnCloudHelper ownCloudHelper, MTGlobalSetupRec setupRec);
    void onFileInfoLoaded(MTFileApkInfo fileInfo);
}
