package com.honglu.future.ui.usercenter.activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.honglu.future.R;
import com.honglu.future.base.BaseActivity;
import com.honglu.future.config.ConfigUtil;
import com.honglu.future.config.Constant;
import com.honglu.future.ui.main.activity.WebViewActivity;
import com.honglu.future.ui.usercenter.adapter.BindCardAdapter;
import com.honglu.future.ui.usercenter.bean.BindCardBean;
import com.honglu.future.ui.usercenter.contract.BindCardContract;
import com.honglu.future.ui.usercenter.presenter.BindCardPresenter;
import com.honglu.future.util.SpUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 绑卡列表（银行卡列表）
 * Created by zhuaibing on 2017/11/11
 */

public class BindCardActivity extends BaseActivity<BindCardPresenter> implements BindCardContract.View {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bindcard_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.bindcard_smart_view)
    SmartRefreshLayout mRefreshView;
    private static final String KEY_LIST = "KEY_LIST";

    private BindCardAdapter mAdapter;

    public static void startBindCardActivity(Context context,List<BindCardBean> json){
        Intent intent = new Intent(context, BindCardActivity.class);
        if (json !=null){
            String string = new Gson().toJson(json);
            intent.putExtra(KEY_LIST,string);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_card;
    }

    @Override
    public void initPresenter() {
          mPresenter.init(this);
    }

    @Override
    public void loadData() {
        mTitle.setTitle(true, R.mipmap.ic_back_black, R.color.white, "银行卡");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setFocusable(false);
        mAdapter = new BindCardAdapter();
        mRecyclerView.setAdapter(mAdapter);
        Intent intent = getIntent();
        if (intent!=null){
            String stringExtra = intent.getStringExtra(KEY_LIST);
            if (!TextUtils.isEmpty(stringExtra)){
                Type type = new TypeToken<List<BindCardBean>>() {
                }.getType();
                List<BindCardBean> list = new Gson().fromJson(stringExtra, type);
                mAdapter.addData(list);
            }
        }
        mRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mPresenter.getBindCardInfo(SpUtil.getString(Constant.CACHE_TAG_UID),SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN));
                mRefreshView.finishRefresh();
            }
        });

        mPresenter.getBindCardInfo(SpUtil.getString(Constant.CACHE_TAG_UID),SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN));
    }

    @OnClick({R.id.tv_add_card})
    public void onClick(View view) {
        Intent intent = new Intent(mActivity, WebViewActivity.class);
        intent.putExtra("url", ConfigUtil.BIND_CARD_TEACH);
        startActivity(intent);
    }


    @Override
    public void getBindCardInfo(List<BindCardBean> list) {
        if (list !=null){
            mAdapter.getData().clear();
            mAdapter.addData(list);
        }
    }
}
