package com.wanxiang.shell;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.net.wifi.WifiManager;

public class MainActivity extends Activity {

    private static final String TARGET_URL = "https://deskai.reexin.cn/flash/";
    private static final String DEFAULT_LIC = "EMO-VYPB-KDHQ-U8Y4";
    private static final int PERM_REQ = 100;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String[] needPerms = { Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };
        boolean needReq = false;
        for (String p : needPerms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                needReq = true;
                break;
            }
        }
        if (needReq) requestPermissions(needPerms, PERM_REQ);

        String deviceId = getDeviceId();

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        final String jsInject = "(function(){"
            + "if(typeof window.API==='undefined') window.API={};"
            + "window.API.userId='" + deviceId + "';"
            + "window.API.lic='" + DEFAULT_LIC + "';"
            + "})();";

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(jsInject, null);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        webView.loadUrl(TARGET_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }

    private String getDeviceId() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm != null) {
                String mac = wm.getConnectionInfo().getMacAddress();
                if (mac != null && !mac.equals("02:00:00:00:00:00")) {
                    return mac.replace(":", "").toLowerCase();
                }
            }
        } catch (Exception ignored) {}
        try {
            String id = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if (id != null) return id;
        } catch (Exception ignored) {}
        return "unknown";
    }
}
