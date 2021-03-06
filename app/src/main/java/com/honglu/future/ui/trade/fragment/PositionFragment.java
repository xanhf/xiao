package com.honglu.future.ui.trade.fragment;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseFragment;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.AccountLoginDialog;
import com.honglu.future.dialog.BillConfirmDialog;
import com.honglu.future.dialog.PositionDialog;
import com.honglu.future.dialog.TradeTipDialog;
import com.honglu.future.dialog.closetransaction.CloseTransactionDialog;
import com.honglu.future.events.ChangeTabEvent;
import com.honglu.future.events.RefreshUIEvent;
import com.honglu.future.events.UIBaseEvent;
import com.honglu.future.ui.login.activity.LoginActivity;
import com.honglu.future.ui.main.contract.AccountContract;
import com.honglu.future.ui.main.presenter.AccountPresenter;
import com.honglu.future.ui.trade.adapter.PositionAdapter;
import com.honglu.future.ui.trade.bean.AccountBean;
import com.honglu.future.ui.trade.bean.HoldDetailBean;
import com.honglu.future.ui.trade.bean.HoldPositionBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.ui.trade.bean.SettlementInfoBean;
import com.honglu.future.ui.trade.contract.PositionContract;
import com.honglu.future.ui.trade.presenter.PositionPresenter;
import com.honglu.future.ui.usercenter.activity.UserAccountActivity;
import com.honglu.future.ui.usercenter.bean.AccountInfoBean;
import com.honglu.future.util.NumberUtils;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.StringUtil;
import com.honglu.future.util.ViewUtil;
import com.honglu.future.widget.popupwind.PositionPopWind;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.honglu.future.util.ToastUtil.showToast;

/**
 * Created by zq on 2017/10/26.
 */
public class PositionFragment extends BaseFragment<PositionPresenter> implements PositionContract.View, AccountContract.View,
        PositionPopWind.OnButtonClickListener, PositionAdapter.OnShowPopupClickListener, BillConfirmDialog.OnConfirmClickListener {
    @BindView(R.id.lv_listView)
    ListView mListView;
    @BindView(R.id.srl_refreshView)
    SmartRefreshLayout mRefreshView;
    @BindView(R.id.tv_remarksEmpty)
    TextView mRemarksEmpty;
    private View mFooterEmptyView;
    private TextView mDangerChance;
    private TextView mRightsInterests;
    private TextView mMoney;
    private TextView mProfitLoss;

    private PositionAdapter mAdapter;
    private AccountPresenter mAccountPresenter;
    private AccountLoginDialog mAccountLoginDialog;
    private PositionDialog mPositionDialog;
    private CloseTransactionDialog mCloseDialog;
    private BillConfirmDialog billConfirmDialog;
    private PositionPopWind mPopWind;
    private HoldPositionBean mHoldPositionBean;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            getAccountInfo();//每隔3秒刷一次
            mHandler.postDelayed(this, 3000);
        }
    };

    private Runnable mPositionRunnable = new Runnable() {
        @Override
        public void run() {
            getPositionList();
        }
    };

    private List<HoldPositionBean> mList;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_position;
    }

    @Override
    public void showLoading(String content) {
        if (!TextUtils.isEmpty(content)) {
            App.loadingContent(mActivity, content);
        }
    }

    @Override
    public void stopLoading() {
        App.hideLoading();
    }

    @Override
    public void showErrorMsg(String msg, String type) {
        showToast(msg);
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
        mAccountPresenter = new AccountPresenter();
        mAccountPresenter.init(this);
    }

    public void setEmptyView(boolean isEmpty) {
        if (isEmpty) {
            mRemarksEmpty.setVisibility(View.VISIBLE);
            mFooterEmptyView.setVisibility(View.VISIBLE);
            if (mListView.getFooterViewsCount() <= 0 && mFooterEmptyView != null) {
                mListView.addFooterView(mFooterEmptyView);
            }
        } else {
            mRemarksEmpty.setVisibility(View.GONE);
            mFooterEmptyView.setVisibility(View.GONE);
            if (mListView.getFooterViewsCount() > 0 && mFooterEmptyView != null) {
                mListView.removeFooterView(mFooterEmptyView);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (App.getConfig().getLoginStatus()) {
                if (!App.getConfig().getAccountLoginStatus()) {
                    if (isVisible()) {
                        stopRun();
                        mAccountLoginDialog = AccountLoginDialog.getInstance(mActivity, mAccountPresenter);
                        mAccountLoginDialog.show();
                    }
                } else {
                    if (isVisible()) {
                        startRun();
                    }
                }
            } else {
                EventBus.getDefault().post(new ChangeTabEvent(0));
                startActivity(LoginActivity.class);
            }
        } else {
            stopRun();
        }
    }

    public void stopRun() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacks(mPositionRunnable);
    }

    @Override
    public void loadData() {
        EventBus.getDefault().register(this);
        mPopWind = new PositionPopWind(mContext);
        mPositionDialog = new PositionDialog(mContext);
        mCloseDialog = new CloseTransactionDialog(getActivity());
        mAdapter = new PositionAdapter(PositionFragment.this);
        View headView = LayoutInflater.from(mContext).inflate(R.layout.layout_trade_position_list_header, null);
        LinearLayout tradeHeader = (LinearLayout) headView.findViewById(R.id.ll_trade_header);
        TextView tvTip = (TextView) headView.findViewById(R.id.tv_tip);
        ImageView ivTip = (ImageView) headView.findViewById(R.id.iv_trade_tip);
        TextView tvNewPrice = (TextView) headView.findViewById(R.id.tv_new_price);
        tvNewPrice.setText("最新价");

        //剩余平分
        LinearLayout rootLayoutLeft = (LinearLayout) headView.findViewById(R.id.rootLayout_left);
        int screenWidth = ViewUtil.getScreenWidth(getActivity());
        int pixelSize_60 = getResources().getDimensionPixelSize(R.dimen.dimen_60dp);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootLayoutLeft.getLayoutParams();
        params.width = (screenWidth - pixelSize_60) / 2;
        rootLayoutLeft.setLayoutParams(params);

        mDangerChance = (TextView) headView.findViewById(R.id.tv_danger_chance);
        mRightsInterests = (TextView) headView.findViewById(R.id.tv_rights_interests);
        mMoney = (TextView) headView.findViewById(R.id.tv_money);
        mProfitLoss = (TextView) headView.findViewById(R.id.tv_profit_loss);
        mFooterEmptyView = LayoutInflater.from(mContext).inflate(R.layout.layout_trade_position_emptyview, null);
        mListView.addHeaderView(headView);
        mListView.addFooterView(mFooterEmptyView);
        mListView.setAdapter(mAdapter);
        setEmptyView(true);

        mAdapter.setOnShowPopupClickListener(this);
        mPopWind.setOnButtonClickListener(this);
        mPopWind.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                startRun();
            }
        });
        mRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mRefreshView.finishRefresh();
            }

        });

        tvTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TradeTipDialog tipDialog = new TradeTipDialog(mContext, R.layout.layout_trade_heyue_tip);
                tipDialog.show();
            }
        });

        ivTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TradeTipDialog tipDialog = new TradeTipDialog(mContext, R.layout.layout_trade_tip_pop_window);
                tipDialog.show();
            }
        });

        tradeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!App.getConfig().getLoginStatus()) {
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    startActivity(intent);
                } else {
                    if (!App.getConfig().getAccountLoginStatus()) {
                        mAccountLoginDialog = new AccountLoginDialog(mContext, mAccountPresenter);
                        mAccountLoginDialog.show();
                    } else {
                        startActivity(UserAccountActivity.class);
                    }
                }
            }
        });
    }

    private void getPositionList() {
        mPresenter.getHoldPositionList(SpUtil.getString(Constant.CACHE_TAG_UID), SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN), Constant.COMPANY_CODE);
    }

    private void getAccountInfo() {
        mPresenter.getAccountInfo(SpUtil.getString(Constant.CACHE_TAG_UID), SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN), Constant.COMPANY_CODE);
    }

    @Override
    public void loginSuccess(AccountBean bean) {
        showToast("登录成功");
        if (billConfirmDialog != null && billConfirmDialog.isShowing()) {
            billConfirmDialog.dismiss();
        }
        mAccountLoginDialog.dismiss();
        startRun();
    }

    @Override
    public void showSettlementDialog(SettlementInfoBean bean) {
        billConfirmDialog = new BillConfirmDialog(mContext, bean);
        billConfirmDialog.setOnConfirmClickListenerr(this);
        billConfirmDialog.show();
    }

    /**
     * 开始刷新用户信息
     */
    public void startRun() {
        if (App.getConfig().getAccountLoginStatus()) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.post(mRunnable);
        }
    }


    @Override
    public void getHoldPositionListSuccess(List<HoldPositionBean> list) {
        mList = list;
        mAdapter.notifyDataChanged(false, list);
        mRefreshView.finishRefresh();
    }

    @Override
    public void getHoldDetailListSuccess(List<HoldDetailBean> list) {
        mPositionDialog.showPopupWind(mHoldPositionBean, list);
    }

    @Override
    public void onDetailClick(HoldPositionBean bean) {
        mHoldPositionBean = bean;
        mPresenter.getHoldPositionDetail(bean.getInstrumentId(),
                String.valueOf(bean.getType()),
                String.valueOf(bean.getTodayPosition()),
                SpUtil.getString(Constant.CACHE_TAG_UID),
                SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN));
    }

    //显示平仓
    @Override
    public void onCloseClick(HoldPositionBean bean) {
        if (bean != null && !TextUtils.isEmpty(bean.getInstrumentId())) {
            this.mHoldPositionBean = bean;
            mPresenter.getProductDetail(bean.getInstrumentId());
        }
    }

    //产品详情
    @Override
    public void getProductDetailSuccess(ProductListBean productListBean) {
        mCloseDialog.showDialog(mHoldPositionBean, productListBean);
    }


    @Override
    public void closeOrderSuccess() {
        showToast("平仓成功");
        mCloseDialog.dismiss();
        mPopWind.dismiss();
        getPositionList();
    }

    @Override
    public void getAccountInfoSuccess(AccountInfoBean bean) {
        mDangerChance.setText(bean.getCapitalProportion());
        mRightsInterests.setText(StringUtil.forNumber(new BigDecimal(bean.getRightsInterests()).doubleValue()));
        mMoney.setText(StringUtil.forNumber(new BigDecimal(bean.getAvailable()).doubleValue()));

        if (Integer.parseInt(bean.getPositionProfit()) > 0) {
            mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_opt_gt));
            mProfitLoss.setText("+" + bean.getPositionProfit());
        } else if (Integer.parseInt(bean.getPositionProfit()) < 0) {
            mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_opt_lt));
            mProfitLoss.setText(bean.getPositionProfit());
        } else {
            mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_333333));
            mProfitLoss.setText(bean.getPositionProfit());
        }
        mHandler.postDelayed(mPositionRunnable, 1000);
    }


    @Override
    public void onShowPopupClick(View view, HoldPositionBean bean) {
        stopRun();
        mPopWind.showPopupWind(view, bean);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (mCloseDialog != null) {
            mCloseDialog.onDestroy();
        }
    }

    /***********
     * eventBus 监听
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UIBaseEvent event) {
        if (event instanceof RefreshUIEvent) {
            int code = ((RefreshUIEvent) event).getType();
            if (code == UIBaseEvent.EVENT_ACCOUNT_LOGOUT) {//安全退出期货账户
                if (!App.getConfig().getAccountLoginStatus()) {
                    stopRun();
                    mDangerChance.setText("--");
                    mRightsInterests.setText("--");
                    mMoney.setText("--");
                    mProfitLoss.setText("--");
                    mList = new ArrayList<>();
                    mAdapter.notifyDataChanged(false, mList);
                    setEmptyView(true);
                }
            }
        }
    }

    @Override
    public void onConfirmClick() {
        mAccountPresenter.settlementConfirm(SpUtil.getString(Constant.CACHE_TAG_UID));
    }
}
