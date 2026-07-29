package com.jerry.pvzstudy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ============================================
 * 主界面：全屏 WebView 加载内置游戏页面
 *
 * 游戏本体在 assets/index.html（作业打卡 × 植物防御联动逻辑
 * 与微信小程序版一一对应，核心函数名一致：
 *   finishSubject / checkUnfinished / goRepair / gameTick）
 *
 * 数据持久化：WebView localStorage（开启 DomStorage 即可），
 * 卸载 App 前数据不丢失。
 * ============================================
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);        // 游戏逻辑全靠 JS
        s.setDomStorageEnabled(true);        // localStorage 存打卡/花园数据
        s.setAllowFileAccess(true);          // 允许读取 asset 本地文件
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setTextZoom(100);                  // 防止系统字体缩放打乱布局

        // 所有跳转都留在 WebView 内，不弹外部浏览器
        webView.setWebViewClient(new WebViewClient());

        // 加载内置游戏页面
        webView.loadUrl("file:///android_asset/index.html");

        // 返回键：优先 WebView 后退，否则退出
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
