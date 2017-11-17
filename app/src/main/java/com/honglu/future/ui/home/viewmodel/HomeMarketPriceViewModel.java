package com.honglu.future.ui.home.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.honglu.future.BuildConfig;
import com.honglu.future.R;
import com.honglu.future.base.BasePresenter;
import com.honglu.future.base.IBaseView;
import com.honglu.future.config.Constant;
import com.honglu.future.http.HttpManager;
import com.honglu.future.http.HttpSubscriber;
import com.honglu.future.mpush.MPushUtil;
import com.honglu.future.ui.home.bean.HomeMarketCodeBean;
import com.honglu.future.ui.home.bean.MarketData;
import com.honglu.future.ui.trade.kchart.KLineMarketActivity;
import com.honglu.future.util.DeviceUtils;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ToastUtil;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hefei on 2017/6/6.
 * <p>
 * 新的首页组件
 *
 * 取集合的list/3+list%3为页数。
 * 页数*3为步长。再刷新
 * 首頁market組件
 */

public class HomeMarketPriceViewModel extends IBaseView<MarketData>{
    private static final String TAG = "HomeMarketPrice";

    private ViewPager mViewPager;
    private ArrayList<MarketData.MarketDataBean> arrayList ;
    private static final int PAGE_SIZE = 3;//每页显示的个数
    private IndicatorViewModel indicatorViewModel;
    private boolean isRefresh;
    private Context mContext;
    private BasePresenter marketPresenter;
    public View mView;
    public String productList;
    private ProductViewHold4Home productViewHold4Home;

    public HomeMarketPriceViewModel(Context context) {
        mContext = context;
        mView = View.inflate(context, R.layout.home_market_view, null);
        initView(mView);
        refreshData();
    }
    /**
     * 刷新数据
     */
    private void refreshData() {
        if (marketPresenter ==null) {
            marketPresenter = new BasePresenter<IBaseView<MarketData>>(this) {
                @Override
                public void getData() {
                    super.getData();
                    //拿到codes
                    toSubscribe(HttpManager.getApi().getMarketCodesData(
                            2, BuildConfig.VERSION_NAME, SpUtil.getString(Constant.CACHE_TAG_UID)
                    ), new HttpSubscriber<HomeMarketCodeBean>() {
                        @Override
                        protected void _onNext(HomeMarketCodeBean o) {
                            //拿到list
                            productList = o.productList;
                            MPushUtil.CODES_TRADE_HOME = productList;
                            String replace = productList.replace("|", "%7C");
                            toSubscribe(HttpManager.getApi().getMarketCodesData(replace,2), new HttpSubscriber<MarketData>() {
                                @Override
                                protected void _onNext(MarketData o) {
                                    super._onNext(o);
                                   mView.bindData(o);
                                }
                                @Override
                                protected void _onError(String message) {
                                    super._onError(message);
                                    ToastUtil.show(message);
                                }
                            });
                            //绑定list
                        }
                    });
                }
            };
        }
        marketPresenter.getData();
    }
    private void initView(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.vp_market);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll_indicator_view);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.dp2px(60), DensityUtil.dp2px(2));
        params.gravity = Gravity.CENTER;
        indicatorViewModel = new IndicatorViewModel(mContext, 3);
        linearLayout.addView(indicatorViewModel.mView,params);
    }
    @Override
    public void bindData(MarketData marketData) {
        if (marketData==null||marketData.list==null||marketData.list.size()<=0)return;
        MPushUtil.requestMarket(productList);
        ArrayList<MarketData.MarketDataBean> dataList = marketData.list;
        if (arrayList!=null){
            arrayList.clear();
        }else {
            arrayList = new ArrayList<>();
        }
        for (int n = 0; n < dataList.size(); n++) {
            if (dataList.get(n) != null) {
                arrayList.add(dataList.get(n));
            }
        }
        initProductViewPager();
        initProLayout(dataList);
        if (arrayList!=null&&arrayList.size()>0){
            if (!isRefresh){
                isRefresh = true;
                int sum ;
                if (arrayList.size() % PAGE_SIZE>0){
                    sum = (arrayList == null || arrayList.size() == 0) ? 0 : arrayList.size() / PAGE_SIZE + 1;
                }else {
                    sum = (arrayList == null || arrayList.size() == 0) ? 0 : arrayList.size() / PAGE_SIZE;
                }
                if (indicatorViewModel!=null){
                    indicatorViewModel.refreshNum(sum);
                }
            }
        }
    }
    public void requestMarket(){
        if (!TextUtils.isEmpty(productList))
        MPushUtil.requestMarket(productList);
    }
    /**
     * 刷新数据
     * @param dataBean
     */
    public void refreshPrice(MarketData.MarketDataBean dataBean){
        if (arrayList!=null&&arrayList.size()>0){
            int index = -1;
            MarketData.MarketDataBean marketDataBean = null;
            for (int i = 0;i<arrayList.size();i++) {
                marketDataBean = arrayList.get(i);
                if (marketDataBean.instrumentID.equals(dataBean.instrumentID)){
                    index = i;
                    break;
                }
            }
            if (index > 0){
                marketDataBean.change =   dataBean.change;
                marketDataBean.chg =  dataBean.chg;
                marketDataBean.lastPrice =  dataBean.lastPrice;
                arrayList.set(index,marketDataBean);
                productViewHold4Home.updateProductViewListDisplay(marketDataBean);
            }

        }
    }
    private HashMap<String, Double> checkChangeMap = new HashMap<String, Double>();
    /**
     * 初始化界面
     *
     * @param list
     */
    private void initProLayout(List<MarketData.MarketDataBean> list) {
        if (checkChangeMap.size() == 0) {
            for (MarketData.MarketDataBean optional : list) {
                checkChangeMap.put(optional.getCode(), Double.parseDouble(optional.lastPrice));
            }
        }
        productViewHold4Home.setCheckChangeMap(checkChangeMap);
        for (final MarketData.MarketDataBean optional : list) {
            productViewHold4Home.updateProductViewListDisplay(optional);
            // 添加点击事件
            productViewHold4Home.listProductView
                    .get(optional.getCode())
                    .rl_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, KLineMarketActivity.class);
                    intent.putExtra("excode",optional.exchangeID);
                    intent.putExtra("code",optional.instrumentID);
                    intent.putExtra("isClosed","1");
                    mContext.startActivity(intent);
                }
            });
        }
    }


    /**
     * 首页产品viewpager
     */
   private void initProductViewPager() {
        List<View> mListViews = new ArrayList<>();
        String[] allProducts = productList.split(",");
        int count = allProducts.length / 3;
        int decentCount = allProducts.length % 3;
        if (decentCount != 0) {
            count += 1;
        }
        for (int i = 0; i < count; i++) {
            mListViews.add(View.inflate(mContext, R.layout.layout_homepropage_3, null));
        }
        mViewPager.setAdapter(new MyProductViewPagerAdapter(mListViews));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
           @Override
           public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
           }

           @Override
           public void onPageSelected(int position) {
               indicatorViewModel.showIndicator(position);
           }

           @Override
           public void onPageScrollStateChanged(int state) {

           }
       });
        productViewHold4Home = new ProductViewHold4Home(mListViews, mContext, productList);
    }


    /**
     * 首页产品viewpager
     */
    private class MyProductViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        MyProductViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));//删除页卡
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {    //这个方法用来实例化页卡
            final int mPos = position % mListViews.size();
            container.addView(mListViews.get(mPos), 0);//添加页卡
            return mListViews.get(mPos);
        }

        @Override
        public int getCount() {
            return mListViews.size();//返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;//官方提示这样写
        }
    }
}
