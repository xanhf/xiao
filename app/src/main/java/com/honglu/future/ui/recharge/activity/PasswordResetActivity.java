package com.honglu.future.ui.recharge.activity;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonNull;
import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseActivity;
import com.honglu.future.base.BasePresenter;
import com.honglu.future.config.Constant;
import com.honglu.future.http.HttpManager;
import com.honglu.future.http.HttpSubscriber;
import com.honglu.future.util.AESUtils;
import com.honglu.future.util.DeviceUtils;
import com.honglu.future.util.LogUtils;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by hefei on 2017/10/24.
 * 修改资金密码
 * 修改交易密码
 */
public class PasswordResetActivity extends BaseActivity {

    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String TAG = "PasswordReset";
    private boolean mIsResetMarketPassword = true;
    @BindView(R.id.tv_back)
    ImageView mLeftIcon;
    @BindView(R.id.tv_title)
    TextView mTitleText;
    @BindView(R.id.btn_pay)
    TextView mBtn;
    @BindView(R.id.et_old_password)
    EditText mOldPassword;
    @BindView(R.id.et_new_password)
    EditText mNewPassword;
    @BindView(R.id.et_conform_password)
    EditText mConformPassword;
    private String mOldPwd;
    private String mNewPwd;
    private String mConformPwd;
    private BasePresenter<PasswordResetActivity> mBasePresenter;

    public static void startPasswordResetActivity(Context context, boolean isResetMarketPassword) {
        Intent intent = new Intent(context, PasswordResetActivity.class);
        intent.putExtra(KEY_TYPE, isResetMarketPassword);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_reset_pwd_asses_market;
    }


    @Override
    public void initPresenter() {
        mBasePresenter = new BasePresenter<PasswordResetActivity>(this) {
            @Override
            public void getData() {
                super.getData();
                int flag = -1;
                String account = SpUtil.getString(Constant.CACHE_ACCOUNT_USER_NAME);
                if (TextUtils.isEmpty(account)) {
                    ToastUtil.show("请登录期货账户后重试");
                    return;
                }
                String account_token = SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN);
                if (TextUtils.isEmpty(account_token)) {
                    flag = 1;
                }
                String userId = SpUtil.getString(Constant.CACHE_TAG_UID);
                if (TextUtils.isEmpty(userId)) {
                    ToastUtil.show("请登录后重试");
                    return;
                }
                LogUtils.logd(TAG, "account-->" + account + "userID--->" + userId + "token-->" + account_token);
                if (mIsResetMarketPassword) {
                    toSubscribe(HttpManager.getApi().resetMarketPwd(
                            account, AESUtils.encrypt(mOldPwd),
                            account_token,
                            AESUtils.encrypt(mNewPwd),flag,
                            userId),
                            new HttpSubscriber<JsonNull>() {
                                @Override
                                protected void _onNext(JsonNull o) {
                                    super._onNext(o);
                                    mBtn.setEnabled(true);
                                    ToastUtil.show("修改密码成功");
                                    finish();
                                }

                                @Override
                                protected void _onError(String message) {
                                    super._onError(message);
                                     mBtn.setEnabled(true);
                                    if (!TextUtils.isEmpty(message)) {
                                        ToastUtil.show(message);
                                    }
                                }
                            });
                } else {
                    toSubscribe(HttpManager.getApi().resetAssesPwd(
                            account, AESUtils.encrypt(mOldPwd),
                            account_token, AESUtils.encrypt(mNewPwd),
                            userId),
                            new HttpSubscriber<JsonNull>() {
                                @Override
                                protected void _onNext(JsonNull o) {
                                    super._onNext(o);
                                    mBtn.setEnabled(true);
                                    ToastUtil.show("修改密码成功");
                                    finish();
                                }

                                @Override
                                protected void _onError(String message) {
                                    super._onError(message);
                                    mBtn.setEnabled(true);
                                    if (!TextUtils.isEmpty(message)) {
                                        ToastUtil.show(message);
                                    }
                                }
                            });
                }
            }
        };
    }

    @Override
    public void loadData() {
        initView();
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mOldPwd = mOldPassword.getText().toString();
                mNewPwd = mNewPassword.getText().toString();
                mConformPwd = mConformPassword.getText().toString();
                if (TextUtils.isEmpty(mOldPwd) || TextUtils.isEmpty(mNewPwd)
                        || TextUtils.isEmpty(mConformPwd)
                        || mOldPwd.length() < 6 || mNewPwd.length() < 6
                        || mConformPwd.length() < 6) {
                    mBtn.setEnabled(false);
                } else {
                    mBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        mOldPassword.addTextChangedListener(textWatcher);
        mNewPassword.addTextChangedListener(textWatcher);
        mConformPassword.addTextChangedListener(textWatcher);
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            mIsResetMarketPassword = intent.getBooleanExtra(KEY_TYPE, true);
        }
        if (mIsResetMarketPassword) {
            mTitleText.setText("修改交易密码");
        } else {
            mTitleText.setText("修改资金密码");
        }
        mLeftIcon.setVisibility(View.VISIBLE);
        mTitle.getFlBack().setVisibility(View.VISIBLE);
        mLeftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @OnClick({R.id.btn_pay})
    public void onClick(View view) {
        if (DeviceUtils.isFastDoubleClick()){
            return;
        }
        if (mNewPwd.length()<8){
            ToastUtil.show("数字和英文字母组合，8-16位");
            return;
        }
        if (!(mConformPwd.equals(mNewPwd))){
            ToastUtil.show("两次密码输入不一致请重新输入");
            return;
        }
        mBtn.setEnabled(false);
        mBasePresenter.getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBasePresenter.onDestroy();
    }
}
