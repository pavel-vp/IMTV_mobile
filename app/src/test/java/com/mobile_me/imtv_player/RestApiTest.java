package com.mobile_me.imtv_player;

import android.util.Log;
import com.mobile_me.imtv_player.model.MTFileApkInfo;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.model.MTStatRec;
import com.mobile_me.imtv_player.service.rest.MTRestHelper;
import com.squareup.okhttp.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import retrofit.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pasha on 18.03.18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
@PowerMockIgnore("javax.net.ssl.*")
public class RestApiTest {

    MTRestHelper restHelper;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
        this.restHelper = MTRestHelper.getInstance("http://crm.darilkin-shop.ru/api/");
    }

    @Test
    public void getPlayList_test() throws InterruptedException, IOException {
        // http://crm.darilkin-shop.ru/api/screen/getPlayList.php?code=b8b58378e361
        MTPlayListRec[] res = restHelper.getPlayListSync("b8b58378e361");
        System.out.println(Arrays.asList(res));
    }

    @Test
    public void getGlobalSetup_test() throws InterruptedException, IOException {
        // http://crm.darilkin-shop.ru/api/screen/getPlayList.php?code=b8b58378e361
        MTGlobalSetupRec res = restHelper.getGlobalSetupRecSync("b8b58378e361");
        System.out.println(res);
    }

    @Test
    public void postAddNewDevice_test() throws InterruptedException, IOException {
        // http://crm.darilkin-shop.ru/api/screen/getPlayList.php?code=b8b58378e361
        restHelper.postNewDeviceSync("8656546742");
        System.out.println("ok");
    }

    @Test
    public void getVideoFile() throws IOException {
        String fileName="/6/6604_7967.mp4"; //http://crm.darilkin-shop.ru/api/video/getVideoFile.php?fn=/6/6604_7967.mp4
        byte[] res=restHelper.getVideoFileSync(fileName);
        System.out.println(res ==null ? null : res.length);
        File file = new File("/tmp/6604_7967.mp4");
        FileOutputStream fos = new FileOutputStream (file);
        fos.write(res);
        fos.flush();
        fos.close();

    }

    @Test
    public void postStat() throws IOException {

        List<MTStatRec> list = new ArrayList<>();
        MTStatRec r = new MTStatRec();
        r.setId(1L);
        r.setDt("6546546545");
        list.add(r);
        list.add(r);
        list.add(r);
        list.add(r);
        ResponseBody body = restHelper.postStatSync("8656546742", list);
        System.out.println(body);
    }

    @Test
    public void postLog() throws IOException {
        String base64 = "392827982792874892498247892";
        ResponseBody body = restHelper.postLogSync("8656546742", base64);
        System.out.println(body);
    }

    @Test
    public void getLastApk_test() throws InterruptedException, IOException {
        MTFileApkInfo res = restHelper.getLastApkSync();
        System.out.println(res);
    }


    @Test
    public void getApkTest() throws IOException {
        byte[] res=restHelper.getApkSync();
        System.out.println(res ==null ? null : res.length);
        File file = new File("/tmp/last.apk");
        FileOutputStream fos = new FileOutputStream (file);
        fos.write(res);
        fos.flush();
        fos.close();

    }


}
