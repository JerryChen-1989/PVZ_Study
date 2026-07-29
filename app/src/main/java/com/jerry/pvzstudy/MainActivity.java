package com.jerry.pvzstudy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * ============================================
 * 主界面：全屏 WebView 加载内置游戏页面
 *
 * 游戏本体在 assets/index.html（作业打卡 × 植物防御联动逻辑
 * 与微信小程序版一一对应）
 *
 * 数据持久化：WebView localStorage（开启 DomStorage 即可），
 * 卸载 App 前数据不丢失。
 *
 * 【拍照/选图支持】页面里的 <input type="file"> 在 Android WebView
 * 中默认点击无反应，必须重写 WebChromeClient.onShowFileChooser：
 *  - capture 属性（拍照按钮）→ 直接调起系统相机，照片经 FileProvider 写入缓存目录
 *  - 无 capture（相册按钮）→ 弹出图片选择器（同时附带相机入口）
 * 注意：Manifest 未声明 CAMERA 权限（通过 Intent 调系统相机无需权限）
 * ============================================
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQ_FILE_CHOOSER = 1001;

    private WebView webView;
    /** JS 文件选择回调：onShowFileChooser 保存，onActivityResult 回填 */
    private ValueCallback<Uri[]> filePathCallback;
    /** 拍照输出的临时文件 URI（FileProvider） */
    private Uri cameraOutputUri;

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

        // WebChromeClient：处理 JS 弹窗 + <input type="file"> 文件选择
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view,
                                             ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                // 若有未完成的回调，先置空释放，否则页面会卡住不再触发
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;

                // 1) 相机 Intent：输出到缓存目录（经 FileProvider 授权）
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraOutputUri = null;
                try {
                    File photoFile = new File(getCacheDir(),
                            "camera_" + System.currentTimeMillis() + ".jpg");
                    cameraOutputUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            getPackageName() + ".fileprovider",
                            photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception e) {
                    cameraIntent = null;   // FileProvider 配置异常时降级为只选相册
                }

                // 2) 相册 Intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");

                // 3) 页面 input 带 capture 属性（📷拍照按钮）→ 直接开相机；
                //    否则（🖼️相册按钮）→ 图片选择器 + 附带相机入口
                Intent launch;
                if (params != null && params.isCaptureEnabled() && cameraIntent != null) {
                    launch = cameraIntent;
                } else {
                    launch = Intent.createChooser(galleryIntent, "选择图片");
                    if (cameraIntent != null) {
                        launch.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                                new Intent[]{cameraIntent});
                    }
                }

                try {
                    startActivityForResult(launch, REQ_FILE_CHOOSER);
                } catch (Exception e) {
                    filePathCallback.onReceiveValue(null);
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

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

    /** 文件选择/拍照结果回填给 WebView 的 JS 回调 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_FILE_CHOOSER || filePathCallback == null) return;

        Uri[] result = null;
        if (resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // 相册选图
                result = new Uri[]{data.getData()};
            } else if (cameraOutputUri != null) {
                // 拍照（data 为空时照片已写入 EXTRA_OUTPUT 指定的 URI）
                result = new Uri[]{cameraOutputUri};
            }
        }
        filePathCallback.onReceiveValue(result);   // 取消时回填 null，页面不会卡住
        filePathCallback = null;
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
