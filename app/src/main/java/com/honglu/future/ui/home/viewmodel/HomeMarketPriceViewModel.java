//package com.honglu.future.ui.home.viewmodel;
//
//import android.animation.ObjectAnimator;
//import android.content.Context;
//import android.support.v4.view.PagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.honglu.future.R;
//import com.honglu.future.ui.home.bean.MarketData;
//import com.honglu.future.util.DeviceUtils;
//import com.honglu.future.util.NumberUtils;
//import com.scwang.smartrefresh.layout.util.DensityUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by hefei on 2017/6/6.
// * <p>
// * 新的首页组件
// *
// * 取集合的list/3+list%3为页数。
// * 页数*3为步长。再刷新
// * 首頁market組件
// */
//
//public class HomeMarketPriceViewModel implements View.OnClickListener {
//
//    private ViewPager mViewPager;
//    private ArrayList<MarketData.DataBeanX.DataBean.DataListBean> arrayList ;
//    ArrayList<Double> oldPriceList = null;//上一秒价格集合
//    private static final int PAGE_SIZE = 3;//每页显示的个数
//    private PagerAdapter pagerAdapter;
//    private IndicatorViewModel indicatorViewModel;
//    private boolean isRefresh;
//    private int deviceWidth;//设备宽度
//    private Context mContext;
//
//    public HomeMarketPriceViewModel(Context context) {
//        mContext = context;
//    }
//
//
//    public void bindView(MarketData data) {
//        if (data==null||data.getData()==null||data.getData().getData()==null)return;
//        List<MarketData.DataBeanX.DataBean.DataListBean> dataList = data.getData().getData().getDataList();
//        if (arrayList!=null){
//            arrayList.clear();
//        }else {
//            arrayList = new ArrayList<>();
//        }
//        for (int n = 0; n < dataList.size(); n++) {
//            if (dataList.get(n) != null) {
//                arrayList.add(dataList.get(n));
//            }
//        }
//        initOldPriceList();
//        PagerAdapter adapter = mViewPager.getAdapter();
//        if (adapter == null){
//            adapter =  pagerAdapter;
//            mViewPager.setAdapter(pagerAdapter);
//        }
//        if (arrayList!=null&&arrayList.size()>0){
//            if (!isRefresh){
//                isRefresh = true;
//                int sum ;
//                if (arrayList.size() % PAGE_SIZE>0){
//                    sum = (arrayList == null || arrayList.size() == 0) ? 0 : arrayList.size() / PAGE_SIZE + 1;
//                }else {
//                    sum = (arrayList == null || arrayList.size() == 0) ? 0 : arrayList.size() / PAGE_SIZE;
//                }
//                if (indicatorViewModel!=null){
//                    indicatorViewModel.refreshNum(sum);
//                }
//            }
//            adapter.notifyDataSetChanged();
//        }
//    }
//
//    public void initView(View view) {
//        deviceWidth = DeviceUtils.getScreenWidth(mContext);
//        mViewPager = (ViewPager) view.findViewById(R.id.vp_market);
//        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll_indicator_view);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.dp2px(30), DensityUtil.dp2px(2));
//        params.gravity = Gravity.CENTER;
//        indicatorViewModel = new IndicatorViewModel(mContext, 3);
//        linearLayout.addView(indicatorViewModel.mView,params);
//        initViewPage();
//    }
//    /**
//     * 初始化viewpage
//     */
//    private void initViewPage(){
//        if (pagerAdapter == null){
//            pagerAdapter = new PagerAdapter() {
//                private int mChildCount = 0;
//                @Override
//                public void notifyDataSetChanged() {
//                    mChildCount = getCount();
//                    super.notifyDataSetChanged();
//                }
//                @Override
//                public int getItemPosition(Object object)   {
//                    if ( mChildCount > 0) {
//                        mChildCount --;
//                        return POSITION_NONE;
//                    }
//                    return super.getItemPosition(object);
//                }
//                @Override
//                public int getCount() {
//                    if (arrayList.size() % PAGE_SIZE>0){
//                        return (arrayList == null || arrayList.size() == 0) ? 0 : arrayList.size() / PAGE_SIZE + 1;
//                    }else {
//                        return (arrayList == null || arrayList.size() == 0) ? 0 : arrayList.size() / PAGE_SIZE;
//                    }
//                }
//                @Override
//                public Object instantiateItem(ViewGroup container, int position) {
//                    View view = View.inflate(container.getContext(), R.layout.new_home_item2, null);
//                    LinearLayout llContainer = (LinearLayout) view.findViewById(R.id.line_ggy);
//                    addViewToPager(llContainer,position,arrayList);
//                    container.addView(llContainer);
//                    return llContainer;
//                }
//                @Override
//                public void destroyItem(ViewGroup container, int position, Object object) {
//                    container.removeView((View) object);
//                }
//                @Override
//                public boolean isViewFromObject(View view, Object object) {
//                    return view == object;
//                }
//            };
//        }
//        mViewPager.setOffscreenPageLimit(1);
//        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if (indicatorViewModel != null){
//                    indicatorViewModel.showIndicator(position);
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//    }
//    /**
//     * 添加view到pager中
//     * @param llContainer 当前的布局
//     * @param position 当前的位置
//     * @param arrayListBean 当前的数据
//     */
//    private void addViewToPager(LinearLayout llContainer,int position,ArrayList<MarketData.DataBeanX.DataBean.DataListBean> arrayListBean){
//        if (arrayListBean ==null||arrayListBean.size()==0){
//            return;
//        }
//        int pageCount = arrayListBean.size() / PAGE_SIZE;//有的整页的页数
//        int lastPageCount = arrayListBean.size() % PAGE_SIZE;//最后一页省的个数
//        LinearLayout childAt = (LinearLayout) llContainer.getChildAt(0);
//        int childCount = 0;
//        if (childAt!=null){
//            childCount = childAt.getChildCount();
//        }
//        if (pageCount-1>=position){//完整的一页
//            if (childCount == PAGE_SIZE){//说明view已经添加
//                for (int i = position*PAGE_SIZE; i < (position+1)*PAGE_SIZE; i++) {//需要加一
//                    bindGoodsItem(childAt.getChildAt(i-position*PAGE_SIZE),arrayListBean.get(i),oldPriceList.get(i),i);
//                }
//            }else {//添加View
//                llContainer.removeAllViews();
//                LinearLayout viewThr = (LinearLayout) View.inflate(mContext, R.layout.homegoods_item_thr, null);
//                viewThr.removeAllViews();
//                for (int i = position*PAGE_SIZE; i < (position+1)*PAGE_SIZE; i++) {//需要加一
//                    View goodsItem = View.inflate(mContext, R.layout.new_homegoods_item, null);
//                    goodsItem.setTag(arrayListBean.get(i).getRemark());
//                    goodsItem.setOnClickListener(this);//设置点击事件
//                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
//                    params1.width = deviceWidth / 3;
//                    viewThr.addView(goodsItem, params1);
//                    try {
//                        if (arrayListBean !=null && oldPriceList !=null && arrayListBean.size() > i && oldPriceList.size() > i){
//                            bindGoodsItem(goodsItem,arrayListBean.get(i),oldPriceList.get(i),i);
//                        }
//                    }catch (Exception e){
//
//                    }
//                }
//                llContainer.addView(viewThr);
//            }
//        }else {
//            if (childCount == lastPageCount){//说明view已经添加
//                for (int i = position*PAGE_SIZE; i < arrayList.size(); i++) {//需要加一
//                    bindGoodsItem(childAt.getChildAt(i-position*PAGE_SIZE),arrayListBean.get(i),oldPriceList.get(i),i);
//                }
//            }else {//添加View
//                llContainer.removeAllViews();
//                LinearLayout viewThr = (LinearLayout) View.inflate(mContext, R.layout.homegoods_item_thr, null);
//                viewThr.removeAllViews();
//                for (int i = position*PAGE_SIZE; i < arrayList.size(); i++) {//需要加一
//                    View goodsItem = View.inflate(mContext, R.layout.new_homegoods_item, null);
//                    goodsItem.setTag(arrayListBean.get(i).getRemark());
//                    goodsItem.setOnClickListener(this);//设置点击事件
//                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
//                    params1.width = deviceWidth/3;
//                    viewThr.addView(goodsItem, params1);
//                    try {
//                        bindGoodsItem(goodsItem,arrayListBean.get(i),oldPriceList.get(i),i);
//                    }catch (Exception e){}
//                }
//                llContainer.addView(viewThr);
//            }
//        }
//    }
//    /**
//     * 初始化添加老的集合
//     */
//    private void initOldPriceList(){
//        //生成上一秒报价的集合
//        if (oldPriceList == null) {
//            oldPriceList = new ArrayList<>();
//            for (int i = 0; i < arrayList.size(); i++) {
//                oldPriceList.add(0.0);
//            }
//        }
//    }
//    /**
//     * 刷新item控件的数据
//     * @param goodsItem
//     */
//    private void bindGoodsItem(View goodsItem ,MarketData.DataBeanX.DataBean.DataListBean data,double oldPrice,int position){
//        TextView mTvItemName = (TextView) goodsItem.findViewById(R.id.tvitemname);
//        TextView mTvItemPrice = (TextView) goodsItem.findViewById(R.id.tvitemprice);
//        TextView mTvItemRise = (TextView) goodsItem.findViewById(R.id.tvitemrise);
//        ImageView mImgItemClose = (ImageView) goodsItem.findViewById(R.id.imgitemclose);
//        ImageView mImgGreen = (ImageView) goodsItem.findViewById(R.id.imggreen);
//        ImageView mImgRed = (ImageView) goodsItem.findViewById(R.id.imgred);
//        final ImageView mIvConpon = (ImageView) goodsItem.findViewById(R.id.ivConpon);
//        double newPrice = NumberUtils.getDouble(data.getTodayPrice());
//        double closePrice = NumberUtils.getDouble(data.getClosingPrice());
//        //当当前价小于昨收价时，价格颜色应变更为绿色
//        if (newPrice - oldPrice > 0){
//            mTvItemRise.setTextColor(mContext.getResources().getColor(R.color.color_ff5376));
//            mTvItemPrice.setTextColor(mContext.getResources().getColor(R.color.color_ff5376));
//        }else if (newPrice - oldPrice < 0){
//            mTvItemRise.setTextColor(mContext.getResources().getColor(R.color.color_00ce64));
//            mTvItemPrice.setTextColor(mContext.getResources().getColor(R.color.color_00ce64));
//        }
//        //设置字体颜色和动画
//        if (newPrice - oldPrice > 0) {//这一秒价格上涨展示出红色背景并开启渐变动画
//            oldPriceList.set(position, newPrice);
//            mImgRed.setVisibility(View.VISIBLE);
//            ObjectAnimator alpha = ObjectAnimator.ofFloat(mImgRed, "alpha", 0f, 1f, 0f);
//            alpha.setDuration(500);
//            alpha.setRepeatCount(1);
//            alpha.start();
//            mTvItemRise.setTextColor(mContext.getResources().getColor(R.color.color_ff5376));
//            mTvItemPrice.setTextColor(mContext.getResources().getColor(R.color.color_ff5376));
//        } else if (newPrice - oldPrice < 0) {//这一秒价格下降展示出绿色背景并开启渐变动画
//            oldPriceList.set(position, newPrice);
//            mImgGreen.setVisibility(View.VISIBLE);
//            ObjectAnimator alpha = ObjectAnimator.ofFloat(mImgGreen, "alpha", 0f, 1f, 0f);
//            alpha.setDuration(500);
//            alpha.setRepeatCount(1);
//            alpha.start();
//            mTvItemRise.setTextColor(mContext.getResources().getColor(R.color.color_00ce64));
//            mTvItemPrice.setTextColor(mContext.getResources().getColor(R.color.color_00ce64));
//        }
//        //判断是否休市,0休市，1开市
//        if (data.getState().equals("1")) {
//            //开市
//            mImgItemClose.setVisibility(View.GONE);
//        } else {
//            mImgItemClose.setVisibility(View.VISIBLE);
//        }
//
//        //设置产品名称
//        mTvItemName.setText(data.getInvestProductName());
//        //设置上涨下跌比例
//        if ("HGNI".equals(data.getInvestProductId())) {//处理哈贵镍展示小数问题
//            if (newPrice - closePrice >= 0) {
//                mTvItemRise.setText("+" + NumberUtils.getFloatStr2(newPrice - closePrice) + "   " + "+" + NumberUtils.getFloatStr2((newPrice - closePrice) * 100.0 / closePrice) + "%");
//            } else if (newPrice - closePrice < 0) {
//                mTvItemRise.setText(NumberUtils.getFloatStr2(newPrice - closePrice) + "   " + NumberUtils.getFloatStr2((newPrice - closePrice) * 100.0 / closePrice) + "%");
//            }
//            mTvItemPrice.setText(newPrice + "");
//        } else {
//            if (newPrice - closePrice >= 0) {
//                mTvItemRise.setText("+" + NumberUtils.getIntegerStr(newPrice - closePrice) + "   " + "+" + NumberUtils.getFloatStr2((newPrice - closePrice) * 100.0 / closePrice) + "%");
//            } else if (newPrice - closePrice < 0) {
//                mTvItemRise.setText(NumberUtils.getIntegerStr(newPrice - closePrice) + "   " + NumberUtils.getFloatStr2((newPrice - closePrice) * 100.0 / closePrice) + "%");
//            }
//            mTvItemPrice.setText(NumberUtils.getIntegerStr(newPrice));
//        }
//    }
//    /**
//     * 点击事件
//     * @param view
//     */
//    @Override
//    public void onClick(View view) {
//
//    }
//}
