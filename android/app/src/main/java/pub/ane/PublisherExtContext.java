package pub.ane;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;

import com.adobe.fre.FREByteArray;
import com.adobe.fre.FREObject;
import com.applovin.mediation.AppLovinExtrasBundleBuilder;
import com.applovin.mediation.ApplovinAdapter;
import com.chartboost.sdk.Chartboost;
import com.google.ads.mediation.chartboost.ChartboostAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.rockplaygames.a.MainActivity;
import com.vungle.mediation.VungleExtrasBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipInputStream;

public class PublisherExtContext extends ExtContextBase implements RewardedVideoAdListener {

    private int saveCount = 0;
    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;

    private static final String APP_ID = "ca-app-pub-2206762674984537~3927942854";
    private static final String AD_UNIT_ID = "ca-app-pub-2206762674984537/1218019039";
    private static final String AD_UNIT_INTERS = "ca-app-pub-2206762674984537/1900631033";

//    private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
//    private static final String AD_UNIT_ID = "cca-app-pub-3940256099942544/5224354917";
//    private static final String AD_UNIT_INTERS = "ca-app-pub-3940256099942544/1033173712";


    private Handler handler;
    private int triedTimes = 0;


    public FREObject readFromZip(String path) {
        FREByteArray ba = null;
        try {
            InputStream in = getActivity().getAssets().open("game.bin");
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in));
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            ba = FREByteArray.newByteArray();
            ba.setProperty("length", FREObject.newObject(bytes.length));
            ba.acquire();
            ByteBuffer bb = ba.getBytes();
            bb.put(bytes);
            ba.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ba;
    }

    @ANE
    public void saveImage(byte[] bytes) {
        String imageName = "ZIMG_" + getCurrentDate() + saveCount + ".jpg";
        try {
            saveImage(imageName, bytes);
        } catch (Exception e) {
        }
        saveCount++;
        if (saveCount > 9)
            saveCount = 0;

        dispatchStatusEventAsync("savedImg", "");
    }

    /**
     * 截屏
     */
    private void saveImage(String imageName, byte[] bytes) throws Exception {
        String filePath = "zzgames/" + imageName;
        File file = new File(Environment.getExternalStorageDirectory(),
                filePath);

        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new InvalidParameterException();
        }

        MediaStore.Images.Media.insertImage(//
                getActivity().getContentResolver(), //
                file.getAbsolutePath(), //
                imageName, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file));
        getActivity().sendBroadcast(intent);
    }

    public static String getCurrentDate() {
        Date d = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sf.format(d);
    }

    @ANE
    public void toast(String str, String str2) {
//		Toast.makeText(getActivity(), str2, Toast.LENGTH_SHORT).show();
    }

    @ANE
    public int init() {
        return 1;
    }

    @ANE
    public String getLang() {
        return Locale.getDefault().getLanguage();
    }

    @ANE
    public void askForMessage() {
        sendMessage();
    }

    public void sendMessage() {
        this.dispatchStatusEventAsync("start", "lalala");
    }

    @ANE
    public int ready() {
        Intent intent = new Intent();
        intent.setAction("cn.abel.action.broadcast");
        intent.putExtra("author", "Abel");
        getActivity().sendBroadcast(intent);
        initAD();
        ((MainActivity) getActivity()).setExtension(this);
        return 1;
    }

    @ANE
    public int getSound() {
        return 1;
    }

    @ANE
    public void startAD() {

        initAD();
    }

    private void initAD() {
        handler = new Handler();
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(getActivity(), APP_ID);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        mRewardedVideoAd.loadAd(AD_UNIT_ID,
                new AdRequest.Builder().build());

        if (AD_UNIT_INTERS != null) {
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(AD_UNIT_INTERS);
//            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
//                    Toast.makeText(getActivity(), "onAdLoaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
//                    Toast.makeText(getActivity(), "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdOpened() {
//                    Toast.makeText(getActivity(), "onAdOpened", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdLeftApplication() {
//                    Toast.makeText(getActivity(), "onAdLeftApplication", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdClosed() {
                    PublisherExtContext.this.dispatchStatusEventAsync("onAdClosed", "100");
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        }

        loadRewardedVideoAd();
    }

    @ANE
    public int showRewardAD() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
            return 0;
        } else {
            return -1;
        }
    }

    @ANE
    public int showInterstitialAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            return 0;
        } else {
            return -1;
        }
    }

    private void loadRewardedVideoAd() {
        if (triedTimes >= 3) {
            return;
        }
        triedTimes++;

        String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();
//        String[] placements = new String[]{"DEFAULT62498"};
        // build network extras bundle
        Bundle extras = new VungleExtrasBuilder()
//                .setUserId("userId")
//                .setSoundEnabled(false)
                .build();
        Bundle extras2 = new AppLovinExtrasBundleBuilder()
                .setMuteAudio(true)
                .build();

        Bundle cbExtra = new ChartboostAdapter.ChartboostExtrasBundleBuilder()
                .setFramework(Chartboost.CBFramework.CBFrameworkOther, "1.2.3")
                .build();

        // build request for reward-based ads
        AdRequest rewardedAdRequest = new AdRequest.Builder()
//                .addNetworkExtrasBundle(VungleAdapter.class, extras)
                .addNetworkExtrasBundle(ApplovinAdapter.class, extras2)
//                .addNetworkExtrasBundle(ChartboostAdapter.class, cbExtra)
                .build();
        mRewardedVideoAd.loadAd(AD_UNIT_ID, rewardedAdRequest);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
        this.dispatchStatusEventAsync("videoLoaded", "loaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
        this.dispatchStatusEventAsync("videoOpened", "open");
    }

    @Override
    public void onRewardedVideoStarted() {
//        Toast.makeText(getActivity(), "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
        this.dispatchStatusEventAsync("videoStart", "start");
    }

    @Override
    public void onRewardedVideoAdClosed() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        this.dispatchStatusEventAsync("videoClose", "closed");
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        this.dispatchStatusEventAsync("reward", "100");
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdLeftApplication",
//                Toast.LENGTH_SHORT).show();
        this.dispatchStatusEventAsync("videoLeftApp", "left");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
//        Toast.makeText(getActivity(), "onRewardedVideoAdFailedToLoad:" + i, Toast.LENGTH_SHORT).show();
        this.dispatchStatusEventAsync("videoFailedToLoad", "failedToLoad");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadRewardedVideoAd();
            }
        }, 15000);
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    @Override
    public void onResume() {
        if (mRewardedVideoAd != null)
            mRewardedVideoAd.resume(getActivity());
        super.onResume();
        Chartboost.onResume(getActivity());
    }

    @Override
    public void onPause() {
        if (mRewardedVideoAd != null)
            mRewardedVideoAd.pause(getActivity());
        super.onPause();
        Chartboost.onPause(getActivity());
    }

    @Override
    public void onDestroy() {
        if (mRewardedVideoAd != null)
            mRewardedVideoAd.destroy(getActivity());
        super.onDestroy();
        Chartboost.onDestroy(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        Chartboost.onStart(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        Chartboost.onStop(getActivity());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Chartboost.onBackPressed()) {
            return;
        } else {
            super.onBackPressed();
        }
    }
}
