package com.honglu.future.widget.kchart.listener;


import com.xulu.mpush.message.KCandleObj;

/**
 * Created by fangzhu
 * touch出现十字线的Listener
 *
 */
public interface OnKCrossLineMoveListener {
    /*十字线拖动*/
    void onCrossLineMove(KCandleObj object);
    /*十字线隐藏*/
    void onCrossLineHide();
}
