package com.honglu.future.ui.main.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.cfmmc.app.sjkh.*;
import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseActivity;
import com.honglu.future.base.PermissionsListener;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.AlertFragmentDialog;
import com.honglu.future.events.FragmentRefreshEvent;
import com.honglu.future.events.UIBaseEvent;
import com.honglu.future.http.HttpManager;
import com.honglu.future.ui.main.bean.MoreContentBean;
import com.honglu.future.ui.main.contract.MyContract;
import com.honglu.future.ui.main.presenter.MyPresenter;
import com.honglu.future.util.LogUtils;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.StringUtil;
import com.honglu.future.util.ToastUtil;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;


/**
 * 网页加载容器
 * xiejingwen
 */
@Route(path = "/future/webview")
public class WebViewActivity extends BaseActivity<MyPresenter> implements MyContract.View {

    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.tv_tag_content)
    TextView mTvTagContent;
    @BindView(R.id.dialog_view)
    LinearLayout mDialogView;
    @BindView(R.id.progressbar)
    ProgressBar mProgressBar;

    private String title;
    private String mUrl;
    private HashMap<String, String> mHashMap;
    private boolean isZhbTitle;
    private boolean isFinish;
    private Dialog dialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_future_webview;
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
    }

    @Override
    public void loadData() {
        mTitle.setTitle("");
        mTitle.showClose(null);
        initView();
        if (!TextUtils.isEmpty(mUrl)) {
            mUrl = HttpManager.getUrl(mUrl);
            mWebView.loadUrl(mUrl);
        }
    }

    public void initView() {
        mHashMap = new HashMap<>();
        if (getIntent() != null) {
            if (!StringUtil.isBlank(getIntent().getStringExtra("title"))) {
                title = getIntent().getStringExtra("title");
                mTitle.setTitle(title);
            }
            if (!StringUtil.isBlank(getIntent().getStringExtra("improveUrl"))) {//该链接是为了提额的改动
                mUrl = getIntent().getStringExtra("improveUrl");
            } else {
                mUrl = getIntent().getStringExtra("url");
            }

            if (App.getConfig().isDebug() && !TextUtils.isEmpty(mUrl)) {
                LogUtils.loge("当前页面链接:" + mUrl);
                String toastInfo = "";
                toastInfo += "当前页面链接:" + mUrl;
                //ToastUtil.showToast(toastInfo);
            }
        }
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        //WebView属性设置！！！
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不适用缓存
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDatabaseEnabled(true);

        //webview在安卓5.0之前默认允许其加载混合网络协议内容
        // 在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.addJavascriptInterface(new JavaMethod(), "nativeMethod");
        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (message != null) {
//            //打开WebViewActivity,自定义参数必须传入"url"，选传"title"
//            String customContent = message.getCustomContent();
//            if (!TextUtils.isEmpty(customContent)) {//自定义参数是否为空
//                try {
//                    JSONObject contentObj = new JSONObject(customContent);
//                    mUrl = contentObj.optString("url");
//                    XgTitle = contentObj.optString("title");
//                    if (!TextUtils.isEmpty(mUrl)) {
//                        mUrl = HttpManager.getUrl(mUrl);
//                        mWebView.loadUrl(mUrl);
//                        if (!TextUtils.isEmpty(XgTitle)) {
//                            title = XgTitle;
//                            mTitle.setTitle(XgTitle);
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        XGPushManager.onActivityStoped(this);
    }

    private void showDialog() {
        mWebView.setVisibility(View.GONE);
        isZhbTitle = true;
        mDialogView.setVisibility(View.VISIBLE);
        mTvTagContent.setText("正在认证中...");
    }

    private void dismissDialog(String message) {
        isZhbTitle = false;
        mDialogView.setVisibility(View.GONE);
        ToastUtil.showToast(message);
        finish();
    }

//    public void toShare() {
//        if (mMoreContentBean == null) {
//            mPresenter.getInfo();
//        } else {
//            showShare();
//        }
//    }

//    private void showShare() {
//        if (mMoreContentBean != null) {
//          /*  new ShareAction(WebViewActivity.this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA
//                    .WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
//                    .withTitle(mMoreContentBean.getShare_title())
//                    .withText(mMoreContentBean.getShare_body())
//                    .withTargetUrl(mMoreContentBean.getShare_url())
//                    .withMedia(new UMImage(WebViewActivity.this, mMoreContentBean.getShare_logo()))
//                    .setCallback(umShareListener).open();*/
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!TextUtils.isEmpty(mUrl) && mUrl.contains("repayment/detail")) {
            //通知还款或续期成功
            EventBus.getDefault().post(new FragmentRefreshEvent(UIBaseEvent.EVENT_REPAY_SUCCESS));
        }
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView.destroy();
        }
    }


    @Override
    public void showLoading(String content) {
        //认证支付宝因为已经有了dialog，所以不弹出
        if (mWebView.getVisibility() == View.VISIBLE) {
            App.loadingContent(this, content);
        }
    }

    @Override
    public void stopLoading() {
        App.hideLoading();
    }

    @Override
    public void showErrorMsg(String msg, String type) {
        if (TextUtils.isEmpty(type)) {
            ToastUtil.showToast(msg);
            return;
        }

    }


    @Override
    public void userInfoSuccess(MoreContentBean result) {

    }

    public class JavaMethod {
        /*************** START  支付宝和淘宝等第三方数据抓取和认证操作***************/
        /**
         * 隐藏页面 显示认证进度 布局
         *
         * @param type 传入 0
         */
        @JavascriptInterface
        public void goneLayout(int type) {
            switch (type) {
                //隐藏webview
                case 0:
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mWebView.setVisibility(View.GONE);
//                            isZhbTitle = true;
//                            mDialogView.setVisibility(View.VISIBLE);
//                            mTvTagContent.setText(AUTHTAG + "0%");
//                        }
//                    });
                    break;
                default:
                    break;
            }
        }

        /**
         * 跳转开户页面
         */
        @JavascriptInterface
        public void openAccount() {
            Intent intent = new Intent(WebViewActivity.this, com.cfmmc.app.sjkh.MainActivity.class);
            intent.putExtra("brokerId", "0101");
            intent.putExtra("channel", "@200$088-2");
            intent.putExtra("packName", "com.honglu.future");
            String userMobile = SpUtil.getString(Constant.CACHE_TAG_MOBILE);
            if (!TextUtils.isEmpty(userMobile)) {
                intent.putExtra("mobile", userMobile);
            }
            startActivity(intent);
        }

        /**
         * 保存传入的键值
         *
         * @param key
         * @param text
         */
        @JavascriptInterface
        public void saveText(String key, String text) {
            mHashMap.put(key, text);
        }

        /**
         * 根据键获取值
         *
         * @param key 传入键
         * @return
         */
        @JavascriptInterface
        public String getText(String key) {
            //调用这个方法返回数据
            String result = mHashMap.get(key);
            return result;
        }

        /**
         * 根据键获取值
         *
         * @return
         */
        @JavascriptInterface
        public void reBankCard() {
            //调用这个方法返回数据
            WebViewActivity.this.finish();
        }

        /**
         * 设置当前的认证进度
         *
         * @param progress 当前进度  0-100
         */
        @JavascriptInterface
        public void setProgress(final int progress) {

        }

        /**
         * 上传传入的数据 到info-capture/info-upload这个接口 入参： String data
         */
        @JavascriptInterface
        public void submitText(String text) {

        }

        /***************END 支付宝和淘宝等第三方数据抓取和认证操作***************/


        /**
         * 跳转到app的方法
         *
         * @param type json字符串
         *             {
         *             type:"0";
         *             }
         *             0：关闭当前页面
         *             1：跳转到忘记登录密码
         *             2：跳转到忘记支付密码
         *             3：跳转到认证中心
         *             4：跳转到首页
         *             5：跳转到qq，并打开指定号码的客服聊天界面
         */
        @JavascriptInterface
        public void returnNativeMethod(String type) {
//            LogUtils.logd("TAG", "type:" + type);
//            if ("0".equals(type)) {
//                finish();
//            } else if ("1".equals(type)) {
//                Intent intent = new Intent(mContext, ResetPwdActivity.class);
//                intent.putExtra(Constant.CACHE_TAG_USERNAME, SpUtil.getString(Constant.CACHE_TAG_USERNAME));
//                startActivity(intent);
//            } else if ("2".equals(type)) {
//                Intent intent = new Intent(mContext, ResetPwdActivity.class);
//                startActivity(intent);
//            } else if ("3".equals(type)) {
//                Intent intent = new Intent(mContext, PerfectInformationActivity.class);
//                startActivity(intent);
//            } else if ("4".equals(type)) {
//                Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                EventBus.getDefault().post(new ChangeTabMainEvent(FragmentFactory.FragmentStatus.Lend));
//            } else if ("5".equals(type)) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTitle.setRightTitle("咨询客服", new OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                // 这里只能为联系客服特殊处理，转到联系我们的页面
//                                String qq_url =
//                                        "mqqwpa://im/chat?chat_type=crm&uin=2152872885&version=1&src_type=web&web_src" +
//                                                "=file:://";//938009600
//                                try {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qq_url)));
//                                } catch (Exception e) {
//                                    ToastUtil.showToast("请确认安装了QQ客户端");
//                                }
//                            }
//                        });
//                    }
//                });
//
//            }
        }

        /**
         * 调用改方法去发送短信
         *
         * @param phoneNumber 手机号码
         * @param message     短信内容
         **/
        @JavascriptInterface
        public void sendMessage(String phoneNumber, String message) {
            // 注册广播 发送消息
            //发送短信并且到发送短信页面
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }

        /**
         * 调用该方法可以复制文字到手机的粘贴板
         *
         * @param text 需要复制的文字
         *             PS:暂未使用到
         */
        @JavascriptInterface
        public void copyTextMethod(String text) {
//            CopyTextBean bean = ConvertUtil.toObject(text, CopyTextBean.class);
//
//            ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//            c.setPrimaryClip(ClipData.newPlainText("text", bean.getText()));
//
//            ToastUtil.showToast(bean.getTip());
        }

        /**
         * 跳转到拨号页面，拨打传入的手机号码
         *
         * @param tele PS:暂未使用到
         */
        @JavascriptInterface
        public void callPhoneMethod(final String tele) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, new PermissionsListener() {
                @Override
                public void onGranted() {
                    String mTele = tele.replaceAll("-", "").trim();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mTele));
                    if (ActivityCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(intent);
                }

                @Override
                public void onDenied(List<String> deniedPermissions, boolean isNeverAsk) {

                }
            });

        }

        /**
         * 跳转到智齿机器人界面
         */
        @JavascriptInterface
        public void sobot() {
            //UDeskUtils.getInstance().entryChat(WebViewActivity.this);
        }

        /**
         * 协议下载(留存)
         */
        @JavascriptInterface
        public void download(final String link) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.addCategory("android.intent.category.DEFAULT");
            startActivity(intent);
        }

        /**
         * 调用app的分享
         *
         * @param shareBean 分享的json字符串
         *                  int type;   //类型  如需要点击标题右上角“分享"按钮再分享 ，则传入1。  否则传入其他  会直接弹出分享弹窗
         *                  String share_title; //分享标题
         *                  String share_body;  //分享的内容
         *                  String share_url;   //分享后点击打开的地址
         *                  String share_logo;  //分享的图标
         */
        @JavascriptInterface
        public void shareMethod(String shareBean) {
            //
//            final WebShareBean bean = ConvertUtil.toObject(shareBean, WebShareBean.class);
//            if (bean != null) {
//                if (bean.getType() == 1) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mTitle.setRightTitle("分享", new OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    new ShareAction(mActivity).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,
//                                            SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
//                                            .withTitle(bean.getShare_title())
//                                            .withText(bean.getShare_body())
//                                            .withTargetUrl(bean.getShare_url())
//                                            .withMedia(new UMImage(WebViewActivity.this, bean.getShare_logo()))
//                                            .setCallback(umShareListener).open();
//
//                                }
//                            });
//                        }
//                    });
//                } else {
//                    new ShareAction(mActivity).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,
//                            SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
//                            .withTitle(bean.getShare_title())
//                            .withText(bean.getShare_body())
//                            .withTargetUrl(bean.getShare_url())
//                            .withMedia(new UMImage(WebViewActivity.this, bean.getShare_logo()))
//                            .setCallback(umShareListener).open();
//                }
//            }
        }

        @JavascriptInterface
        public void authenticationResult(String message) {

        }
    }

    // private void shareStart(WebShareBean bean){
       /* new ShareAction(mActivity)
                .setPlatform(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,
                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)//传入平台
                .withTitle(mMoreContentBean.getShare_title())
                .withText(mMoreContentBean.getShare_title())
                .withTargetUrl(mMoreContentBean.getShare_url())
                .withMedia(new UMImage(mActivity, mMoreContentBean.getShare_logo()))
                .setCallback(umShareListener)//回调监听器
                .share();*/
       /* UMImage umImage = new UMImage(mActivity, bean.getShare_logo());
        umImage.compressFormat = Bitmap.CompressFormat.PNG;
        UMWeb web = new UMWeb(bean.getShare_url());
        web.setTitle(bean.getShare_title());//标题
        web.setThumb(umImage); //缩略图
        web.setDescription(bean.getShare_body());//描述
        new ShareAction(mActivity)
                .setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,
                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                .withMedia(web)
                .setCallback(umShareListener)
                .share();*/
    // }

//    private void showShareDialog() {
//        dialog = new Dialog(WebViewActivity.this, R.style.alert_dialog);
//        View dialogView = LayoutInflater.from(WebViewActivity.this).inflate(R.layout.dialog_share_layout, null);
//        //计算宽高
//                /*int width = DeviceUtils.getScreenWidth(activity) - 150;
//                int height = width * 870 / 600;*/
//        ImageView mImageLose = (ImageView) dialogView.findViewById(R.id.image_lose);
//        mImageLose.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        dialogView.findViewById(R.id.text_wx).setOnClickListener(shareListener);
//        dialogView.findViewById(R.id.text_wxq).setOnClickListener(shareListener);
//        dialogView.findViewById(R.id.text_qq).setOnClickListener(shareListener);
//        dialogView.findViewById(R.id.text_kongjian).setOnClickListener(shareListener);
//        dialog.setContentView(dialogView);
//        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
//        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//        lp.dimAmount = 0.75f;
//        lp.gravity = Gravity.BOTTOM;
//        dialog.getWindow().setAttributes(lp);
//        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.show();
//    }
//
//    OnClickListener shareListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.text_wx:
//                    shareStart(SHARE_MEDIA.WEIXIN);
//                    break;
//                case R.id.text_wxq:
//                    shareStart(SHARE_MEDIA.WEIXIN_CIRCLE);
//                    break;
//                case R.id.text_qq:
//                    shareStart(SHARE_MEDIA.QQ);
//                    break;
//                case R.id.text_kongjian:
//                    shareStart(SHARE_MEDIA.QZONE);
//                    break;
//            }
//            dialog.dismiss();
//        }
//    };
//
//    private void shareStart(SHARE_MEDIA var1) {
//
//       /* UMImage umImage = new UMImage(mActivity, mMoreContentBean.getShare_logo());
//        umImage.compressFormat = Bitmap.CompressFormat.PNG;
//        UMWeb  web = new UMWeb(mMoreContentBean.getShare_url());
//        web.setTitle(mMoreContentBean.getShare_title());//标题
//        web.setThumb(umImage); //缩略图
//        web.setDescription(mMoreContentBean.getShare_body());//描述
//        new ShareAction(mActivity)
//                .setPlatform(var1)
//                .withMedia(web)
//                .setCallback(umShareListener)
//                .share();*/
//        new ShareAction(mActivity)
//                .setPlatform(var1)//传入平台
//                .withTitle(mMoreContentBean.getShare_title())
//                .withText(mMoreContentBean.getShare_body())
//                .withTargetUrl(mMoreContentBean.getShare_url())
//                .withMedia(new UMImage(mActivity, R.mipmap.icon_logo))
//                .setCallback(umShareListener)//回调监听器
//                .share();
//    }

    private UMShareListener umShareListener = new UMShareListener() {

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtil.showToast("分享成功");
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtil.showToast("分享失败");
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (isZhbTitle) {
            new AlertFragmentDialog.Builder(this)
                    .setContent("返回操作将中断支付宝认证，\n确认要退出吗？")
                    .setLeftBtnText("取消认证")
                    .setRightBtnText("继续认证")
                    .setLeftCallBack(new AlertFragmentDialog.LeftClickCallBack() {
                        @Override
                        public void dialogLeftBtnClick() {
                            finish();
                        }
                    }).build();
        } else {
            if (mWebView.canGoBack() && !isFinish) {
                mWebView.goBack();
            } else {
                finish();
            }
        }
    }


    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    class MyWebViewClient extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            Log.d("tag","url-->"+url);
            if (url.getScheme().contains("xn")) {
                ARouter.getInstance().build(url)
                        .navigation(WebViewActivity.this, new NavCallback() {
                            @Override
                            public void onArrival(Postcard postcard) {
                                finish();
                            }
                        });
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //handler.cancel(); // Android默认的处理方式
            handler.proceed();  // 接受所有网站的证书  解决https拦截问题
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("tag","url-->"+url);
            if (url.startsWith("xn")) {
                ARouter.getInstance().build(Uri.parse(url))
                        .navigation(WebViewActivity.this, new NavCallback() {
                            @Override
                            public void onArrival(Postcard postcard) {
                                finish();
                            }
                        });
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            mUrl = url;
            LogUtils.loge(mUrl);
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }
            if (view.canGoBack()) { //如果当前不是初始页面则显示关闭按钮
                mTitle.showClose(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isZhbTitle) {
                            new AlertFragmentDialog.Builder(mActivity)
                                    .setContent("返回操作将中断支付宝认证，\n确认要退出吗？")
                                    .setLeftBtnText("取消认证")
                                    .setRightBtnText("继续认证")
                                    .setLeftCallBack(new AlertFragmentDialog.LeftClickCallBack() {
                                        @Override
                                        public void dialogLeftBtnClick() {
                                            finish();
                                        }
                                    }).build();
                        } else {
                            finish();
                        }
                    }
                });
            } else {
                //mTitle.hintClose();
            }
            if (url.contains("repayment/detail")) {
                isFinish = true;
            } else {
                isFinish = false;
            }
            Log.e("web", "url==" + url);
            //往当前页面插入一段JS
            if (url.contains("https://my.alipay.com/portal/i.htm") || url.contains("https://shanghu.alipay.com/i.htm")) {
                showDialog();
                String js = "var newscript = document.createElement(\"script\");";
                js += "newscript.src=\"" + App.getConfig().GET_ALIPAY_JS + "\";";
                js += "newscript.onload=function(){toDo();};";  //xxx()代表js中某方法
                js += "document.body.appendChild(newscript);";
                view.loadUrl("javascript:" + js);
            }
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (mProgressBar != null) {
                mProgressBar.setProgress(newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            //是否是支付宝认证
            if (!isZhbTitle) {
                //正常情况下，如果没有传入title，我们则使用h5的title。
                // 当是点推送消息进来的时候,getIntent().getStringExtra("title")会拿到推送传入的乱码般的title字符串。
                // 所以如果是点推送消息进来的，并且推送没有设置title,我们直接使用h5 title
                if (TextUtils.isEmpty(getIntent().getStringExtra("title"))) {
                    WebViewActivity.this.title = title;
                    mTitle.setTitle(true, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isZhbTitle) {
                                new AlertFragmentDialog.Builder(mActivity)
                                        .setContent("返回操作将中断支付宝认证，\n确认要退出吗？")
                                        .setLeftBtnText("取消认证")
                                        .setRightBtnText("继续认证")
                                        .setLeftCallBack(new AlertFragmentDialog.LeftClickCallBack() {
                                            @Override
                                            public void dialogLeftBtnClick() {
                                                finish();
                                            }
                                        }).build();
                            } else {
                                if (mWebView.canGoBack() && !isFinish) {
                                    mWebView.goBack();
                                } else {
                                    finish();
                                }
                            }
                        }
                    }, title);
                    mTitle.setRightTitle("", null);
                }
            } else {
                mTitle.setTitle("认证中");
            }

        }

    }
}
