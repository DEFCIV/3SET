package com.haennya.fitness;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;

/** Hosted 3SET web UI, packaged offline fallback, on-device history, native reminders. */
public class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST=4242;
    private static final String REMOTE="https://defciv.github.io/3SET/";
    private static final String FALLBACK="file:///android_asset/index.html";
    private static final String TRUSTED_HOST="defciv.github.io";
    private static final String TRUSTED_PATH="/3SET/";
    private WebView webView;
    private boolean fallingBack=false;
    private long lastShown=0L;
    private boolean firstResume=true;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff0b0c0f);
        getWindow().setNavigationBarColor(0xff0b0c0f);
        ReminderScheduler.ensureChannel(this);
        ReminderScheduler.rebuild(this);
        webView=new WebView(this);
        webView.setBackgroundColor(0xff0b0c0f);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebViewClient(new AppClient());
        webView.addJavascriptInterface(new NativeBridge(),"AndroidBridge");
        setContentView(webView);
        loadLatestSite();
    }

    private boolean isTrusted(android.net.Uri uri){
        return "https".equalsIgnoreCase(uri.getScheme()) &&
            TRUSTED_HOST.equalsIgnoreCase(uri.getHost()) &&
            uri.getPath()!=null && uri.getPath().startsWith(TRUSTED_PATH);
    }

    private void loadLatestSite(){
        fallingBack=false;
        lastShown=SystemClock.elapsedRealtime();
        webView.loadUrl(REMOTE+"?app="+System.currentTimeMillis());
    }
    private void showFallback(){
        if(fallingBack)return;
        fallingBack=true;
        webView.loadUrl(FALLBACK);
    }

    private final class AppClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request){
            android.net.Uri u=request.getUrl();
            return !(isTrusted(u)||FALLBACK.equals(u.toString()));
        }
        @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest request){
            android.net.Uri u=request.getUrl();
            if(isTrusted(u)||FALLBACK.equals(u.toString()) ||
                ("file".equalsIgnoreCase(u.getScheme()) && u.getPath()!=null &&
                 u.getPath().startsWith("/android_asset/")))return null;
            return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
        }
        @Override public void onReceivedError(WebView v,WebResourceRequest request,WebResourceError error){
            if(request.isForMainFrame()&&!fallingBack)showFallback();
        }
        @Override public void onReceivedHttpError(WebView v,WebResourceRequest request,WebResourceResponse response){
            if(request.isForMainFrame()&&response.getStatusCode()>=400&&!fallingBack)showFallback();
        }
    }

    private void askPermission(){
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_PERMISSION_REQUEST);
    }

    public class NativeBridge {
        @JavascriptInterface public String readState(){
            return getSharedPreferences("haennya-state",MODE_PRIVATE).getString("data","");
        }
        @JavascriptInterface public void writeState(String json){
            if(json==null||json.length()>200000)return;
            try{
                JSONObject value=new JSONObject(json);
                if(!value.has("days")||!value.has("times"))return;
                getSharedPreferences("haennya-state",MODE_PRIVATE).edit().putString("data",json).apply();
            }catch(Exception ignored){}
        }
        @JavascriptInterface public void syncReminders(String json){
            ReminderScheduler.storeAndSchedule(MainActivity.this,json);
        }
        @JavascriptInterface public void requestNotificationPermission(){
            runOnUiThread(()->askPermission());
        }
        @JavascriptInterface public void showTestNotification(){
            runOnUiThread(()->{
                if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
                    askPermission();
                    Toast.makeText(MainActivity.this,"알림 권한을 허용한 뒤 다시 눌러줘",Toast.LENGTH_LONG).show();
                } else ReminderScheduler.showTest(MainActivity.this);
            });
        }
    }
    @Override protected void onResume(){
        super.onResume();
        if(!firstResume&&webView!=null&&SystemClock.elapsedRealtime()-lastShown>120000L)loadLatestSite();
        firstResume=false;
    }
    @Override protected void onDestroy(){
        if(webView!=null){webView.removeJavascriptInterface("AndroidBridge");webView.destroy();}
        super.onDestroy();
    }
}