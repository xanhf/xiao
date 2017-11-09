package com.honglu.future.ui.trade.kchart;

import com.honglu.future.base.BaseView;
import com.honglu.future.ui.trade.bean.RealTimeBean;

/**
 * Created by zq on 2017/11/7.
 */

public interface KLineMarketContract {
    interface View extends BaseView {
        void getProductRealTimeSuccess(RealTimeBean bean);
    }

    interface Presenter {
        void getProductRealTime(String codes);
    }
}
