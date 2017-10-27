package com.honglu.future.ui.trade.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.ui.trade.bean.OpenTransactionListBean;
import com.honglu.future.widget.recycler.BaseRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zq on 2017/10/26.
 */

public class OpenTransactionAdapter extends BaseRecyclerAdapter<OpenTransactionAdapter.ViewHolder, OpenTransactionListBean> {
    @Override
    public ViewHolder mOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_open_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void mOnBindViewHolder(ViewHolder holder, int position) {
        holder.mTvProductName.setText(item.getProductName());
        holder.mTvNum.setText(item.getNum());
        holder.mTvRise.setText(item.getRiseNum());
        holder.mTvDown.setText(item.getDownNum());
        holder.mTvRiseRadio.setText(item.getRiseRadio());
        holder.mTvDownRadio.setText(item.getDownRadio());
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_product_name)
        TextView mTvProductName;
        @BindView(R.id.tv_num)
        TextView mTvNum;
        @BindView(R.id.tv_rise)
        TextView mTvRise;
        @BindView(R.id.tv_down)
        TextView mTvDown;
        @BindView(R.id.tv_rise_radio)
        TextView mTvRiseRadio;
        @BindView(R.id.tv_down_radio)
        TextView mTvDownRadio;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}