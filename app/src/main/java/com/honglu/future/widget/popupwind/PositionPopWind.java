package com.honglu.future.widget.popupwind;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.dialog.PositionDialog;
import com.honglu.future.dialog.TradeTipDialog;
import com.honglu.future.ui.trade.adapter.OpenTransactionAdapter;
import com.honglu.future.ui.trade.bean.HoldPositionBean;
import com.honglu.future.util.ImageUtil;
import com.honglu.future.util.ViewUtil;

/**
 * Created by zhuaibing on 2017/10/27
 */

public class PositionPopWind extends PopupWindow {
    private View mRootLayout;
    private TextView mBond;
    private TextView mServiceCharge;
    private TextView mTime;
    private TextView mDetails;
    private TextView mClosePosition;

    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int mScreenHeight;
    private int mTabHeight;
    private int m31dp;
    private int m23dp;
    private int m8dp;
    private HoldPositionBean mHoldPositionBean;

    public interface OnButtonClickListener {
        void onDetailClick(HoldPositionBean holdPositionBean);
        void onCloseClick(HoldPositionBean holdPositionBean);
    }

    private OnButtonClickListener mListener;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mListener = listener;
    }

    public PositionPopWind(final Context context) {
        View rootView = View.inflate(context, R.layout.popupwind_posttion_layout, null);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mMeasuredWidth = rootView.getMeasuredWidth();
        mMeasuredHeight = rootView.getMeasuredHeight();
        mScreenHeight = ViewUtil.getScreenHeight(context);
        mTabHeight = context.getResources().getDimensionPixelSize(R.dimen.dimen_50dp);
        m31dp = context.getResources().getDimensionPixelSize(R.dimen.dimen_31dp);
        m8dp = context.getResources().getDimensionPixelSize(R.dimen.dimen_8dp);
        m23dp = context.getResources().getDimensionPixelSize(R.dimen.dimen_23dp);
        ColorDrawable drawable = new ColorDrawable(context.getResources().getColor(android.R.color.transparent));
        setBackgroundDrawable(drawable);
        setOutsideTouchable(true);
        setFocusable(true);
        setAnimationStyle(R.style.position_popupwind);
        setContentView(rootView);
        setOutsideTouchable(true);


        mRootLayout = rootView.findViewById(R.id.ll_rootLayout);
        mBond = (TextView) rootView.findViewById(R.id.tv_bond);//保证金
        mServiceCharge = (TextView) rootView.findViewById(R.id.tv_service_charge);//建仓手续费
        mTime = (TextView) rootView.findViewById(R.id.tv_time);//交割日期
        mDetails = (TextView) rootView.findViewById(R.id.tv_position_details); //持仓详情
        mClosePosition = (TextView) rootView.findViewById(R.id.tv_close_position);//平仓
        ImageView ivTip = (ImageView) rootView.findViewById(R.id.iv_position_tip);
        View mLine1 = rootView.findViewById(R.id.v_lineView1);
        View mLine2 = rootView.findViewById(R.id.v_lineView2);
        //加 mutate
        mLine1.getBackground().mutate().setAlpha(80);
        mLine2.getBackground().mutate().setAlpha(80);

        mDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDetailClick(mHoldPositionBean);
            }
        });
        mClosePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCloseClick(mHoldPositionBean);
            }
        });
        ivTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TradeTipDialog tipDialog = new TradeTipDialog(context, R.layout.layout_position_jiaoge_tip);
                tipDialog.show();
            }
        });
    }


    public void showPopupWind(View view, HoldPositionBean bean) {
        // if (Build.VERSION.SDK_INT >= 24) {
        //    Rect rect = new Rect();
        //    view.getGlobalVisibleRect(rect);
        //    int h = view.getResources().getDisplayMetrics().heightPixels - rect.bottom;
        //    setHeight(h);
        //}
        mHoldPositionBean = bean;
        mBond.setText(bean.getUseMargin());
        mServiceCharge.setText(bean.getSxf());
        mTime.setText(bean.getEndDelivDate() + "(" + bean.getDelivDateStr() + ")");

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int surplusHeight = mScreenHeight - location[1] - mMeasuredHeight - mTabHeight;
        if (surplusHeight >= 0) {
            mRootLayout.setBackgroundResource(R.mipmap.bg_top_triangle);
            mRootLayout.setPadding(0, m31dp, 0, 0);
            showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1]);
        } else {
            mRootLayout.setBackgroundResource(R.mipmap.bg_bottom_triangle);
            mRootLayout.setPadding(0, m23dp, 0, m8dp);
            showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] - mMeasuredHeight);
        }
    }
}
