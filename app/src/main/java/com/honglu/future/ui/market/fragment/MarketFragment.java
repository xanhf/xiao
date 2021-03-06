package com.honglu.future.ui.market.fragment;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseFragment;
import com.honglu.future.config.Constant;
import com.honglu.future.events.ChangeTabEvent;
import com.honglu.future.events.ReceiverMarketMessageEvent;
import com.honglu.future.events.RefreshUIEvent;
import com.honglu.future.events.UIBaseEvent;
import com.honglu.future.mpush.MPushUtil;
import com.honglu.future.ui.login.activity.LoginActivity;
import com.honglu.future.ui.market.activity.OptionalQuotesActivity;
import com.honglu.future.ui.market.bean.MarketnalysisBean;
import com.honglu.future.ui.market.contract.MarketContract;
import com.honglu.future.ui.market.presenter.MarketPresenter;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ToastUtil;
import com.honglu.future.widget.tab.CustomTabEntity;
import com.honglu.future.widget.tab.HorizontalTabLayout;
import com.honglu.future.widget.tab.SimpleOnTabSelectListener;
import com.honglu.future.widget.tab.TabEntity;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

/**
 * Created by zhuaibing on 2017/10/25
 */

public class MarketFragment extends BaseFragment<MarketPresenter> implements MarketContract.View, MarketItemFragment.OnAddAptionalListener, MarketItemFragment.OnMPushCodeRefreshListener, MarketItemFragment.OnZXMarketListListener {
    public static final String ZXHQ_TYPE = "ZiXuan";//自选行情
    public static final String ZLHY_TYPE = "Zhuli";//主力合约
    @BindView(R.id.market_common_tab_layout)
    HorizontalTabLayout mCommonTab;


    //除自选外全部数据
    private List<MarketnalysisBean.ListBean> mAllMarketList;
    //tab 数据
    private ArrayList<CustomTabEntity> mTabList = new ArrayList<>();
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String mPushCode;
    private String mTabSelectType;
    private int mPosition = 0;
    private int mHttpState = 0; //0  1中  2 成功

    public static MarketFragment marketFragment;
    private MarketItemFragment mZxFragment;


    public static MarketFragment getInstance() {
        if (marketFragment == null) {
            marketFragment = new MarketFragment();
        }
        return marketFragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_market_layout;
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
    }


    @Override
    public void showLoading(String content) {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorMsg(String msg, String type) {

    }

    @Override
    public void initHttpState(int httpState) {
        this.mHttpState = httpState;
    }

    @Override
    public void onPause() {
        super.onPause();
        MPushUtil.pauseRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            requestMarket(mPushCode);
            if (mHttpState == 0 && mPresenter != null) {
                mHttpState = 1;
                mPresenter.getMarketData();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            MPushUtil.pauseRequest();
        } else {
            requestMarket(mPushCode);
            if (mHttpState == 0 && mPresenter != null) {
                mHttpState = 1;
                mPresenter.getMarketData();
            }
        }
    }

    /*******
     * 将事件交给事件派发controller处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMarketEventMainThread(ReceiverMarketMessageEvent event) {
        if (!TextUtils.isEmpty(mPushCode) && mPushCode.equals(MPushUtil.requestCodes) && !(isHidden())) {
            if (mFragments != null && mFragments.size() > 0 && mFragments.size() > mPosition) {
                MarketItemFragment fragment = (MarketItemFragment) mFragments.get(mPosition);
                fragment.mPushRefresh(event.marketMessage.getInstrumentID(), event.marketMessage.getLastPrice(), event.marketMessage.getChg(), event.marketMessage.getOpenInterest(), event.marketMessage.getChange());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UIBaseEvent event) {
        if (event instanceof RefreshUIEvent) {
            int code = ((RefreshUIEvent) event).getType();
            if (code == UIBaseEvent.EVENT_HOME_TO_MARKET_ZHULI) {//首页跳转主力合约
                mCommonTab.setCurrentTab(1);
            }
        }
    }

    //MPush
    public void requestMarket(String productList) {
        if (!TextUtils.isEmpty(productList)) {
            MPushUtil.requestMarket(productList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAddAptional() {
        String one ="行情_添加自选";
        String two ="hangqing_add_zixuan_click";
        MobclickAgent.onEvent(mContext,two, one);
        if (mAllMarketList != null && mAllMarketList.size() > 0) {
            if (mZxFragment != null) {
                if (!TextUtils.isEmpty(SpUtil.getString(Constant.CACHE_TAG_UID))) {
                    List<MarketnalysisBean.ListBean.QuotationDataListBean> zxList = mZxFragment.getList();
                    Intent intent = new Intent(getActivity(), OptionalQuotesActivity.class);
                    intent.putExtra("allmarketlist", (Serializable) mAllMarketList);
                    intent.putExtra("zxmarketlist", (Serializable) zxList);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        }
    }

    @Override
    public void onMPushCodeRefresh(String mpushCode) {
        if (mFragments != null && mFragments.size() > 0 && mFragments.size() > mPosition) {
            MarketItemFragment fragment = (MarketItemFragment) mFragments.get(mPosition);
            fragment.setOnAddAptionalListener(MarketFragment.this);
            mPushCode = fragment.getPushCode();
            mTabSelectType = fragment.getTabSelectType();
            if (ZXHQ_TYPE.equals(mTabSelectType)) {
                requestMarket(mPushCode);
            }
        }
    }


    @Override
    public List<MarketnalysisBean.ListBean.QuotationDataListBean> onZXMarketList() {
        return mZxFragment != null ? mZxFragment.getList() : null;
    }

    @Override
    public void loadData() {
        EventBus.getDefault().register(this);
        //tab 切换
        mCommonTab.setOnTabSelectListener(new SimpleOnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mPosition = position;
                clickTab(position);
                if (mFragments != null && mFragments.size() > 0) {
                    MarketItemFragment fragment = (MarketItemFragment) mFragments.get(position);
                    fragment.setOnAddAptionalListener(MarketFragment.this);
                    mPushCode = fragment.getPushCode();
                    mTabSelectType = fragment.getTabSelectType();
                    requestMarket(mPushCode);
                    fragment.getRealTimeData();
                }
            }
        });
        if (mHttpState == 0 && mPresenter != null) {
            mHttpState = 1;
            mPresenter.getMarketData();
        }
    }

    private void clickTab(int position){
        CustomTabEntity customTabEntity = mTabList.get(position);
        String one ="hangqing_"+customTabEntity.getTabType()+"_click";
        String two ="行情_"+customTabEntity.getTabTitle();
        MobclickAgent.onEvent(mContext,one, two);
    }

    /**
     * 接口请求返回数据
     *
     * @param alysisBean
     */
    @Override
    public void getMarketData(MarketnalysisBean alysisBean) {
        if (mTabList != null && mTabList.size() > 0) {
            return;
        }
        //自选行情
        List<MarketnalysisBean.ListBean.QuotationDataListBean> zxMarketList = getZxMarketList();
        this.mAllMarketList = addItemDataExcode(alysisBean.getList(), zxMarketList);
        //主力合约
        List<MarketnalysisBean.ListBean.QuotationDataListBean> zlhyMarketList = getZlhyMarketList(mAllMarketList);
        mTabList.clear();
        mFragments.clear();
        mTabList.add(new TabEntity("自选", ZXHQ_TYPE));
        mTabList.add(new TabEntity("主力合约", ZLHY_TYPE));
        mZxFragment = new MarketItemFragment();
        mZxFragment.setArguments(mZxFragment.setArgumentData(ZXHQ_TYPE, zxMarketList,"自选"));
        MarketItemFragment zlhyFragment = new MarketItemFragment();
        zlhyFragment.setArguments(zlhyFragment.setArgumentData(ZLHY_TYPE, zlhyMarketList,"主力合约"));
        zlhyFragment.setOnZXMarketListListener(this);
        mZxFragment.setOnAddAptionalListener(this);
        mZxFragment.setOnMPushCodeRefreshListener(this);
        mFragments.add(mZxFragment);
        mFragments.add(zlhyFragment);
        for (MarketnalysisBean.ListBean bean : mAllMarketList) {
            mTabList.add(new TabEntity(bean.getExchangeName(), bean.getExcode()));
            MarketItemFragment fragment = new MarketItemFragment();
            fragment.setArguments(fragment.setArgumentData(bean.getExcode(), bean.getQuotationDataList(),bean.getExchangeName()));
            fragment.setOnZXMarketListListener(this);
            mFragments.add(fragment);
        }

        mCommonTab.setTabData(mTabList, (FragmentActivity) mContext, R.id.market_fragment_container, mFragments);
        if (zxMarketList !=null && zxMarketList.size() > 0){
            mPosition = 0;
            mPushCode = mosaicMPushCode(zxMarketList);
        }else {
            mPosition = 1;
            mPushCode = mosaicMPushCode(zlhyMarketList);
            mCommonTab.setCurrentTab(1);
        }
        requestMarket(mPushCode);
    }


    //获取自选数据
    private List<MarketnalysisBean.ListBean.QuotationDataListBean> getZxMarketList() {
        String zxMarketJson = SpUtil.getString(Constant.ZX_MARKET_KEY);
        if (!TextUtils.isEmpty(zxMarketJson)) {
            Gson gson = new Gson();
            try {
                List<MarketnalysisBean.ListBean.QuotationDataListBean> mZxMarketList = gson.fromJson(zxMarketJson,
                        new TypeToken<List<MarketnalysisBean.ListBean.QuotationDataListBean>>() {
                        }.getType());
                return mZxMarketList;
            } catch (JsonSyntaxException e) {
            }
        }
        return null;
    }

    //获取主力合约
    private List<MarketnalysisBean.ListBean.QuotationDataListBean> getZlhyMarketList(List<MarketnalysisBean.ListBean> list) {
        List<MarketnalysisBean.ListBean.QuotationDataListBean> mZlhyMarketList = new ArrayList<>();
        for (MarketnalysisBean.ListBean bean : list) {
            if (bean.getQuotationDataList() != null && bean.getQuotationDataList().size() > 0) {
                mZlhyMarketList.addAll(bean.getQuotationDataList());
            }
        }
        return mZlhyMarketList;
    }


    //拼接 MPush code
    private String mosaicMPushCode(List<MarketnalysisBean.ListBean.QuotationDataListBean> list) {
        StringBuilder builder = new StringBuilder();
        if (list != null && list.size() > 0) {

            for (MarketnalysisBean.ListBean.QuotationDataListBean bean : list) {
                if (!TextUtils.isEmpty(bean.getExchangeID()) && !TextUtils.isEmpty(bean.getInstrumentID())) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(bean.getExchangeID());
                    builder.append("|");
                    builder.append(bean.getInstrumentID());
                }
            }
        }
        return builder.toString();
    }


    //给每条数据添加 Excode
    private List<MarketnalysisBean.ListBean> addItemDataExcode(List<MarketnalysisBean.ListBean> list, List<MarketnalysisBean.ListBean.QuotationDataListBean> mZxMarketList) {
        if (mZxMarketList != null && mZxMarketList.size() > 0) {
            for (MarketnalysisBean.ListBean listBean : list) {
                if (!TextUtils.isEmpty(listBean.getExcode())
                        && listBean.getQuotationDataList() != null
                        && listBean.getQuotationDataList().size() > 0) {

                    for (MarketnalysisBean.ListBean.QuotationDataListBean mBean : listBean.getQuotationDataList()) {
                        for (MarketnalysisBean.ListBean.QuotationDataListBean zxBean : mZxMarketList) {
                            if (!TextUtils.isEmpty(mBean.getInstrumentID()) && mBean.getInstrumentID().equals(zxBean.getInstrumentID())) {
                                //已经存在自选 img 显示删除
                                mBean.setIcAdd("1");
                                zxBean.setChg(mBean.getChg());
                                zxBean.setLastPrice(mBean.getLastPrice());
                                zxBean.setOpenInterest(mBean.getOpenInterest());
                                zxBean.setChange(mBean.getChange());
                            } else {
                                //不存在自选 img 显示添加
                                if (!"1".equals(mBean.getIcAdd())) {
                                    mBean.setIcAdd("0");
                                }
                            }

                        }
                    }
                }
            }
        } else {
            for (MarketnalysisBean.ListBean listBean : list) {
                if (!TextUtils.isEmpty(listBean.getExcode())
                        && listBean.getQuotationDataList() != null
                        && listBean.getQuotationDataList().size() > 0) {

                    for (MarketnalysisBean.ListBean.QuotationDataListBean mBean : listBean.getQuotationDataList()) {
                        mBean.setIcAdd("0");
                    }
                }
            }
        }
        return list;
    }
}
