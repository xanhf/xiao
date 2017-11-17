package com.honglu.future.dialog.klineposition;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.config.Constant;
import com.honglu.future.ui.trade.bean.HoldPositionBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.widget.recycler.BaseRecyclerAdapter;

import java.util.List;

/**
 * Created by zq on 2017/11/2.
 */

public class KLinePositionDialogAdapter extends BaseRecyclerAdapter<KLinePositionDialogAdapter.ViewHolder, HoldPositionBean> {
    private ProductListBean mProductListBean;
    private KLinePositionDialogPresenter mPresenter;
    private KLinePositionDialog mDialog;

    private int mPosition = -1;
    private int mExpcNum = 0; //平仓手数
    private int mExPrice = 0;//价格

    private String mLowerLimitPrice; //跌停板价
    private String mUpperLimitPrice; //涨停板价
    private KLinePositionDialogAdapter.ViewHolder mViewHolder;

    public KLinePositionDialogAdapter(KLinePositionDialogPresenter mPresenter, KLinePositionDialog mDialog) {
        this.mPresenter = mPresenter;
        this.mDialog = mDialog;
    }

    public void setProductListBean(ProductListBean bean) {
        this.mExpcNum = 0;
        this.mExPrice = 0;
        this.mProductListBean = bean;
        this.mLowerLimitPrice = mProductListBean.getLowerLimitPrice();
        this.mUpperLimitPrice = mProductListBean.getUpperLimitPrice();
        notifyDataSetChanged();
    }

    /**
     * mpush 刷新对应数据
     * @param lowerLimitPrice
     * @param upperLimitPrice
     */
    public void setMpushRefreshData(String lowerLimitPrice ,String upperLimitPrice){
        this.mLowerLimitPrice = lowerLimitPrice;
        this.mUpperLimitPrice = upperLimitPrice;
        if (mViewHolder !=null)
         mViewHolder.mPriceHint.setText("≥" + getFloatNum(mLowerLimitPrice) + " 跌停价 且 ≤" + getFloatNum(mUpperLimitPrice) + "涨停价");
    }

    //当前展开的 position
    public int getMPosition() {
        return mPosition;
    }

    //重置 position
    public void clearPosition() {
        this.mPosition = -1;
    }

    //手数
    public int getExpcNum() {
        return mExpcNum;
    }

    //价格
    public int getExPrice() {
        return mExPrice;
    }

    //是否刷新mpush 消息
    public boolean isMpushRefresh(){

        return mProductListBean !=null && mPosition != -1;
    }

    //重置数据
    public void  resetData(){
        this.mProductListBean = null;
        this.mExpcNum = 0;
        this.mExPrice = 0;
         clearPosition();
    }

    public void notifyDataChanged(List<HoldPositionBean> list) {
        getData().clear();
        if (list != null) {
            addData(list);
        }
    }


    @Override
    public KLinePositionDialogAdapter.ViewHolder mOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_kline_position_dialog, parent, false);
        return new KLinePositionDialogAdapter.ViewHolder(view);
    }

    @Override
    public void mOnBindViewHolder(KLinePositionDialogAdapter.ViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.mLine.setVisibility(View.INVISIBLE);
        } else {
            holder.mLine.setVisibility(View.VISIBLE);
        }

        //1 跌  2涨
        if (item.getType() == Constant.TYPE_BUY_DOWN) {
            holder.mBuyNum.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
            holder.mBuyNum.setText("买跌" + item.getPosition() + "手");

        } else {
            holder.mBuyNum.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
            holder.mBuyNum.setText("买涨" + item.getPosition() + "手");
        }

        //持仓均价
        holder.mPriced.setText(item.getHoldAvgPrice());

        //持仓盈亏
        holder.mProfitLoss.setText(item.getTotalProfit());
        if (item.getTotalProfit().indexOf("-") != -1) {
            holder.mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
        } else {
            holder.mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
        }

         mViewHolder = holder;
        final HoldPositionBean mBean = item;

        if (position == mPosition) {
            holder.mGouxuan.setSelected(true);
            holder.mGouxuan.setEnabled(false);
            holder.mLayoutContent.setVisibility(View.VISIBLE);
            holder.mEtMaxpc.setEnabled(false);

            //平仓手数 最多
            holder.mMaxpcNum.setText("平仓手数(最多" + item.getPosition() + "手)");
            //最大输入平仓手数
            holder.mEtMaxpc.setText(item.getPosition() + "");
            //手数
            this.mExpcNum = item.getPosition();
            //设置手数事件
            setPositionListener(mViewHolder, mBean);

            if (mProductListBean != null) {

                this.mExPrice = getIntNum(mProductListBean.getLastPrice());
                holder.mEtPrice.setText(mExPrice+"");
                setPriceListener(mViewHolder);

                holder.mPriceHint.setText("≥" + getFloatNum(mLowerLimitPrice) + " 跌停价 且 ≤" + getFloatNum(mUpperLimitPrice) + "涨停价");

                //实际盈亏
                holder.mYkprice.setText("￥" + getActualProfitLoss(item.getType(), holder.mEtMaxpc, item));

                //平仓手续费
                holder.mSxprice.setText("￥"+getPCprice(item.getType(),holder.mEtMaxpc,item));

                //平仓盈亏
                mDialog.setPingcangSatte(item.getType(), getPCProfitLoss(item.getType(), holder.mEtMaxpc, item));
            }

        } else {
            holder.mGouxuan.setSelected(false);
            holder.mGouxuan.setEnabled(true);
            holder.mEtMaxpc.setEnabled(false);
            holder.mLayoutContent.setVisibility(View.GONE);
            final int mZPosition = position;
            holder.mGouxuan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPosition = mZPosition;
                    mPresenter.getProductDetail(mBean.getInstrumentId());
                }
            });
        }
    }

    //手数
    private void setPositionListener(final KLinePositionDialogAdapter.ViewHolder mHolder, final HoldPositionBean mBean) {

        //手数 -
        mHolder.mMaxpcDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textNum = getText(mHolder.mEtMaxpc);
                if (textNum > 1) {
                    textNum--;
                    mHolder.mEtMaxpc.setText(textNum + "");
                    //实际盈亏
                    mHolder.mYkprice.setText("￥" + getActualProfitLoss(item.getType(), mHolder.mEtMaxpc, item));
                    //平仓手续费
                    mHolder.mSxprice.setText("￥"+getPCprice(item.getType(),mHolder.mEtMaxpc,item));
                    //平仓盈亏
                    mDialog.setPingcangSatte(item.getType(), getPCProfitLoss(item.getType(), mHolder.mEtMaxpc, item));
                }
            }
        });

        mHolder.mMaxpcAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textNum = getText(mHolder.mEtMaxpc);
                if (textNum < mBean.getPosition()) {
                    textNum++;
                    mHolder.mEtMaxpc.setText(textNum + "");
                    //实际盈亏
                    mHolder.mYkprice.setText("￥" + getActualProfitLoss(item.getType(), mHolder.mEtMaxpc, item));
                    //平仓手续费
                    mHolder.mSxprice.setText("￥"+getPCprice(item.getType(),mHolder.mEtMaxpc,item));
                    //平仓盈亏
                    mDialog.setPingcangSatte(item.getType(), getPCProfitLoss(item.getType(), mHolder.mEtMaxpc, item));
                }
            }
        });

        mHolder.mEtMaxpc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mExpcNum = getText(mHolder.mEtMaxpc);
            }
        });
    }


    //委托价
    private void setPriceListener(final KLinePositionDialogAdapter.ViewHolder mHolder) {

        mHolder.mPriceDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textNum = getText(mHolder.mEtPrice);
                if (textNum > 1) {
                    textNum--;
                    mHolder.mEtPrice.setText(textNum + "");
                }
            }
        });

        mHolder.mPriceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double textNum = getDoubleText(mHolder.mEtPrice);
                double lastPrice = !TextUtils.isEmpty(mProductListBean.getLastPrice()) ? Double.parseDouble(mProductListBean.getLastPrice()) : 0;
                if (textNum < lastPrice) {
                    textNum++;
                    mHolder.mEtPrice.setText(textNum + "");
                }
            }
        });

        mHolder.mEtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mExPrice = getText(mHolder.mEtPrice);
            }
        });
    }

    private int getText(EditText mText) {

        return mText.getText() != null && !TextUtils.isEmpty(mText.getText().toString()) ? Integer.parseInt(mText.getText().toString()) : 0;
    }

    private double getDoubleText(EditText mText) {

        return mText.getText() != null && !TextUtils.isEmpty(mText.getText().toString()) ? Double.parseDouble(mText.getText().toString()) : 0;
    }

    /**
     * 实际盈亏
     *
     * @param type  1 跌  2涨
     * @param mText
     * @param bean
     * @return
     */
    private float getActualProfitLoss(int type, EditText mText, HoldPositionBean bean) {
        float closePrice = "2".equals(type) ? getFloatNum(mProductListBean.getBidPrice1()) : getFloatNum(mProductListBean.getAskPrice1());

        float price = closePrice - getFloatNum(bean.getOpenAvgPrice());

        float priceTick = getFloatNum(mProductListBean.getPriceTick());

        float mProfitLoss = price / priceTick * priceTick * mProductListBean.getVolumeMultiple() * getText(mText);

        return mProfitLoss;
    }

    /**
     * 平仓盈亏
     *
     * @param type  1 跌  2涨
     * @param mText
     * @param bean
     * @return
     */
    private float getPCProfitLoss(int type, EditText mText, HoldPositionBean bean) {
        float closePrice = "2".equals(type) ? getFloatNum(mProductListBean.getBidPrice1()) : getFloatNum(mProductListBean.getAskPrice1());

        float price = closePrice - getFloatNum(bean.getOpenAvgPrice());

        float priceTick = getFloatNum(mProductListBean.getPriceTick());

        float mProfitLoss = price / priceTick * priceTick * mProductListBean.getVolumeMultiple() * getText(mText);

        return mProfitLoss;
    }


    /**
     * 平仓手续费
     * @param type
     * @param mText
     * @param bean
     * @return
     */
    private float getPCprice(int type,EditText mText, HoldPositionBean bean){
        float closePrice = "2".equals(type) ? getFloatNum(mProductListBean.getBidPrice1()) : getFloatNum(mProductListBean.getAskPrice1());

        float closeRatio = getFloatNum(mProductListBean.getCloseRatioByMoney());

        int buyCount = getText(mText);

        float fee = 0;
        boolean isToday = bean.getTodayPosition() > 0;

        if (closeRatio !=0){
            if (isToday){
                closeRatio = getFloatNum(mProductListBean.getCloseTodayRatioByMoney());
            }
            fee = closePrice * mProductListBean.getVolumeMultiple() * closeRatio * buyCount;
        }else {
            closeRatio = getFloatNum(mProductListBean.getCloseRatioByVolume());
            if (isToday){
                closeRatio = getFloatNum(mProductListBean.getCloseTodayRatioByVolume());
            }
            fee = closeRatio * buyCount;
        }
        return fee;
    }


    private float getFloatNum(String num){
        if (!TextUtils.isEmpty(num)){
            return Float.parseFloat(num);
        }
        return 0;
    }

    private int getIntNum(String num){
        if (!TextUtils.isEmpty(num)){
            return Integer.parseInt(num);
        }
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mGouxuan;
        TextView mBuyNum;
        TextView mPriced;
        TextView mProfitLoss;
        ImageView mPriceDel;
        EditText mEtPrice;
        ImageView mPriceAdd;
        TextView mPriceHint;
        TextView mMaxpcNum;
        ImageView mMaxpcDel;
        EditText mEtMaxpc;
        ImageView mMaxpcAdd;
        TextView mSxprice;
        TextView mYkprice;
        LinearLayout mLayoutContent;
        //ExpandableLayout mExpandable;
        View mLine;

        ViewHolder(View view) {
            super(view);
            mGouxuan = (ImageView) view.findViewById(R.id.iv_gouxuan);
            mBuyNum = (TextView) view.findViewById(R.id.tv_buy_num);
            mPriced = (TextView) view.findViewById(R.id.tv_priced);
            mProfitLoss = (TextView) view.findViewById(R.id.tv_profit_loss);
            mPriceDel = (ImageView) view.findViewById(R.id.iv_price_del);
            mEtPrice = (EditText) view.findViewById(R.id.et_price);
            mPriceAdd = (ImageView) view.findViewById(R.id.iv_price_add);
            mPriceHint = (TextView) view.findViewById(R.id.tv_price_hint);
            mMaxpcNum = (TextView) view.findViewById(R.id.tv_maxpc_num);
            mMaxpcDel = (ImageView) view.findViewById(R.id.iv_maxpc_del);
            mEtMaxpc = (EditText) view.findViewById(R.id.et_maxpc);
            mMaxpcAdd = (ImageView) view.findViewById(R.id.iv_maxpc_add);
            mSxprice = (TextView) view.findViewById(R.id.tv_sxprice);
            mYkprice = (TextView) view.findViewById(R.id.tv_ykprice);
            mLayoutContent = (LinearLayout) view.findViewById(R.id.layout_content);
            // mExpandable = (ExpandableLayout) view.findViewById(R.id.el_expandable);
            mLine = view.findViewById(R.id.v_line);
        }
    }
}
