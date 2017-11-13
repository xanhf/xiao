package com.honglu.future.ui.trade.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseFragment;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.AccountLoginDialog;
import com.honglu.future.dialog.TradeTipDialog;
import com.honglu.future.events.ChangeTabEvent;
import com.honglu.future.events.RefreshUIEvent;
import com.honglu.future.events.UIBaseEvent;
import com.honglu.future.ui.login.activity.LoginActivity;
import com.honglu.future.ui.main.contract.AccountContract;
import com.honglu.future.ui.main.presenter.AccountPresenter;
import com.honglu.future.ui.trade.activity.TradeRecordActivity;
import com.honglu.future.ui.trade.adapter.ClosePositionAdapter;
import com.honglu.future.ui.trade.bean.AccountBean;
import com.honglu.future.ui.trade.bean.ClosePositionListBean;
import com.honglu.future.ui.trade.contract.ClosePositionContract;
import com.honglu.future.ui.trade.presenter.ClosePositionPresenter;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.Tool;
import com.honglu.future.widget.loading.LoadingLayout;
import com.honglu.future.widget.recycler.DividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.honglu.future.util.ToastUtil.showToast;

/**
 * Created by zq on 2017/10/26.
 */

public class ClosePositionFragment extends BaseFragment<ClosePositionPresenter> implements ClosePositionContract.View, AccountContract.View {
    @BindView(R.id.rv_position)
    RecyclerView mPositionListView;
    @BindView(R.id.tv_tip)
    TextView mTvTip;
    @BindView(R.id.loading_layout)
    LoadingLayout mLoadingLayout;
    private ClosePositionAdapter mClosePositionAdapter;
    private AccountLoginDialog mAccountLoginDialog;
    private AccountPresenter mAccountPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_close_position;
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
        mAccountPresenter = new AccountPresenter();
        mAccountPresenter.init(this);
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
    public void loadData() {
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        mPositionListView.setLayoutManager(new LinearLayoutManager(mContext));
        mPositionListView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mClosePositionAdapter = new ClosePositionAdapter();
        mPositionListView.setAdapter(mClosePositionAdapter);
    }

    @OnClick({R.id.tv_tip, R.id.ll_see_all})
    public void onClick(View view) {
        if (Tool.isFastDoubleClick()) return;
        switch (view.getId()) {
            case R.id.tv_tip:
                TradeTipDialog tipDialog = new TradeTipDialog(mContext, R.layout.layout_trade_heyue_closed_tip);
                tipDialog.show();
                break;
            case R.id.ll_see_all:
                startActivity(TradeRecordActivity.class);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (App.getConfig().getLoginStatus()) {
                if (!App.getConfig().getAccountLoginStatus()) {
                    if (isVisible()) {
                        mAccountLoginDialog = new AccountLoginDialog(mActivity, mAccountPresenter);
                        mAccountLoginDialog.show();
                    }
                } else {
                    if (isVisible()) {
                        getClosePositionList();
                    }
                }
            } else {
                EventBus.getDefault().post(new ChangeTabEvent(0));
                startActivity(LoginActivity.class);
            }
        }
    }

    private void getClosePositionList() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateNowStr = sdf.format(d);
        mPresenter.getCloseList("", dateNowStr, SpUtil.getString(Constant.CACHE_TAG_UID), SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN), "", "");
    }

    @Override
    public void loginSuccess(AccountBean bean) {
        mAccountLoginDialog.dismiss();
        getClosePositionList();
    }

    @Override
    public void getCloseListSuccess(List<ClosePositionListBean> list) {
        if (list == null || list.size() == 0) {
            mLoadingLayout.setStatus(LoadingLayout.Empty);
            return;
        }
        mLoadingLayout.setStatus(LoadingLayout.Success);
        mClosePositionAdapter.clearData();
        mClosePositionAdapter.addData(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
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
                    mLoadingLayout.setStatus(LoadingLayout.Empty);
                    mClosePositionAdapter.clearData();
                }
            }
        }
    }
}
