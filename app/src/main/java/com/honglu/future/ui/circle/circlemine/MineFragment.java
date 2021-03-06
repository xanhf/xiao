package com.honglu.future.ui.circle.circlemine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonNull;
import com.honglu.future.R;
import com.honglu.future.base.BasePresenter;
import com.honglu.future.base.CommonFragment;
import com.honglu.future.config.ConfigUtil;
import com.honglu.future.config.Constant;
import com.honglu.future.events.BBSFlownEvent;
import com.honglu.future.events.MessageController;
import com.honglu.future.http.HttpManager;
import com.honglu.future.http.HttpSubscriber;
import com.honglu.future.http.RxHelper;
import com.honglu.future.ui.circle.bean.BBS;
import com.honglu.future.ui.circle.bean.CircleMineBean;
import com.honglu.future.ui.circle.circledetail.CircleDetailActivity;
import com.honglu.future.ui.circle.circlemine.adapter.BBSMineAdapter;
import com.honglu.future.ui.circle.publish.PublishActivity;
import com.honglu.future.ui.circlefriend.MyFriendActivity;
import com.honglu.future.ui.register.activity.RegisterActivity;
import com.honglu.future.util.DeviceUtils;
import com.honglu.future.util.ImageUtil;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ToastUtil;
import com.honglu.future.widget.CircleImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;


public class MineFragment extends CommonFragment {
    @BindView(R.id.smart_refresh)
    SmartRefreshLayout mSmartRefresh;
    @BindView(R.id.listView)
    ListView mListView;
    private BBSMineAdapter mAdapter;
    private boolean isLoadingNow = false;
    private String fid = "0";
    private TextView publish;
    private LinearLayout layout_friends;
    private View empty_view;

    private View header_view;
    private ImageView header_img, iv_follow;
    private TextView flag, user_name, attention_num, endorse_num, topic_num, tv_empty;

    private LinearLayout mAttutudeUserLy;
    private OnTopicAlaph mOnTopicAlaph;
    private BasePresenter<MineFragment> mBasePresenter;
    int rows;
    private boolean isMore;
    private boolean mIsRefresh;
    private String mUserId;
    private String imgHead;
    private String nickName;
    private boolean mIsMyself;
    private boolean isFocued;
    private int mFocusNum;
    private int mFansNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        rows = 0;
        topicIndexThread(true);
    }


    @Override
    public int getLayoutId() {
        return R.layout.fragment_bbs_mine;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void loadData() {
        mUserId = mActivity.getIntent().getExtras().getString("userId", "");
        imgHead = mActivity.getIntent().getExtras().getString("imgHead", "");
        nickName = mActivity.getIntent().getExtras().getString("nickName", "");

        mIsMyself = mUserId.equals(SpUtil.getString(Constant.CACHE_TAG_UID));
        initViews();
    }

    private void initViews() {
        header_view = LayoutInflater.from(mContext).inflate(R.layout.minefragment_header, null);
        header_img = (CircleImageView) header_view.findViewById(R.id.header_img);
        flag = (TextView) header_view.findViewById(R.id.flag);
        user_name = (TextView) header_view.findViewById(R.id.user_name);
        topic_num = (TextView) header_view.findViewById(R.id.topic_num);
        endorse_num = (TextView) header_view.findViewById(R.id.endorse_num);
        attention_num = (TextView) header_view.findViewById(R.id.attention_num);


        mSmartRefresh.setEnableRefresh(true);
        mSmartRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if (!isLoadingNow) {
                    rows = 0;
                    topicIndexThread(true);
                }
            }
        });
        mSmartRefresh.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                if (!isLoadingNow && isMore) {//上拉加载更多
                    topicIndexThread(false);
                } else {
                    mSmartRefresh.finishLoadmore();
                }
            }
        });
        layout_friends = (LinearLayout) header_view.findViewById(R.id.layout_friends);
        layout_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MyFriendActivity.class);
                intent.putExtra("focusNum",mFocusNum);
                intent.putExtra("fansNum",mFansNum);
                mContext.startActivity(intent);
            }
        });

        mAttutudeUserLy = (LinearLayout) header_view.findViewById(R.id.ly_likes_user);
        iv_follow = (ImageView) header_view.findViewById(R.id.iv_follow);

        empty_view = LayoutInflater.from(mContext).inflate(R.layout.fragment_bbs_empty_me, null);
        publish = (TextView) empty_view.findViewById(R.id.publish);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtils.isFastDoubleClick()) {
                    return;
                }
                mContext.startActivity(new Intent(mContext, PublishActivity.class));
            }
        });
        tv_empty = (TextView) empty_view.findViewById(R.id.tv_empty);
        if (mIsMyself) {
            layout_friends.setVisibility(View.VISIBLE);
            iv_follow.setVisibility(View.GONE);
            publish.setVisibility(View.VISIBLE);
            tv_empty.setText("你还没有发表过话题哦");
            imgHead = ConfigUtil.baseImageUserUrl + imgHead;
        } else {
            layout_friends.setVisibility(View.GONE);
            iv_follow.setVisibility(View.VISIBLE);
            publish.setVisibility(View.GONE);
            tv_empty.setText("TA还没有发表过话题哦");
        }
        mAdapter = new BBSMineAdapter(mListView, mContext, imgHead, nickName, mUserId);
        mListView.addHeaderView(header_view);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(listener);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View view = listView.getChildAt(0);
                    if (view != null) {
                        int top = -view.getTop();
                        int headerHeight = view.getHeight();
                        if (top <= headerHeight && top >= 0) {
                            float f = (float) top / (float) headerHeight;
                            if (mOnTopicAlaph != null) {
                                mOnTopicAlaph.onAlaphValue(f);
                            }
                        }
                    }
                } else if (firstVisibleItem > 0) {
                    if (mOnTopicAlaph != null) {
                        mOnTopicAlaph.onAlaphValue(1);
                    }

                } else {
                    if (mOnTopicAlaph != null) {
                        mOnTopicAlaph.onAlaphValue(0);
                    }
                }
                boolean result = false;
//                if (mListView.getFirstVisiblePosition() == 0) {
//                    final View topChildView = mListView.getChildAt(0);
//                    result = topChildView.getTop() == 0;
//                }
//                if (result) {
//                    mSmartRefresh.setEnableRefresh(true);
//                } else {
//                    mSmartRefresh.setEnableRefresh(false);
//                }
            }
        });

        mAdapter.setAttentionCallBack(new BBSMineAdapter.AttentionCallBack() {
            @Override
            public void attention(String uid, String isFollow) {
                if (!mIsMyself) {
                    iv_follow.setImageResource(TextUtils.equals("1", isFollow) ? R.mipmap.btn_guanzhu_already : R.mipmap.btn_guanzhu);
                }
                BBSFlownEvent bbsFlownEvent = new BBSFlownEvent();
                bbsFlownEvent.follow = isFollow;
                bbsFlownEvent.uid = uid;
                EventBus.getDefault().post(bbsFlownEvent);
                if (MessageController.getInstance().getDetailFriendChange() != null) {
                    MessageController.getInstance().getDetailFriendChange().change(uid, isFollow);
                }
            }
        });
    }

    //条目点击事件侦听
    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (DeviceUtils.isFastDoubleClick()) {
                return;
            }
            BBS item = (BBS) parent.getItemAtPosition(position);
            if (item != null) {
                Intent intent = new Intent(view.getContext(), CircleDetailActivity.class);
                intent.putExtra("bbs_item", item);
                startActivity(intent);
            }
        }
    };

    private void topicIndexThread(boolean isRefresh) {
        isLoadingNow = true;
        mIsRefresh = isRefresh;
        //根据情况设置当前的topic_id请求参数
        if (mBasePresenter == null) {
            mBasePresenter = new BasePresenter<MineFragment>(this) {
                @Override
                public void getData() {
                    super.getData();
                    toSubscribe(HttpManager.getApi().loadCircleHome(mUserId, SpUtil.getString(Constant.CACHE_TAG_UID), String.valueOf(rows), "10"), new HttpSubscriber<CircleMineBean>() {
                        @Override
                        protected void _onNext(CircleMineBean o) {
                            super._onNext(o);
                            if (mIsMyself) {
                                if (o.getContactUserList() != null && o.getContactUserList().size() != 0) {
                                    updateAttutudeUser(o.getContactUserList());
                                }
                            } else {
                                isFocued = o.isFocued();
                                iv_follow.setImageResource(isFocued ? R.mipmap.btn_guanzhu_already : R.mipmap.btn_guanzhu);
                                iv_follow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        follow();
                                    }
                                });

                            }
                            ImageUtil.display(imgHead, header_img, R.mipmap.img_head);
                            user_name.setText(nickName);

                            // TODO: 2017/12/9 接口缺少用户角色
//                            if (result.user_info.user_flag.equals("1")) {
//                                flag.setVisibility(View.VISIBLE);
//                            } else {
//                                flag.setVisibility(View.INVISIBLE);
//                            }
                            mFocusNum = o.getFocusNum();
                            mFansNum = o.getBeFocusNum();
                            attention_num.setText("关注" + o.getFocusNum());
                            endorse_num.setText("粉丝" + o.getBeFocusNum());
                            topic_num.setText("发帖" + o.getPostNum());
                            if (o.getPostAndReplyBoList() != null && o.getPostAndReplyBoList().size() > 0) {
                                if (mListView.getFooterViewsCount() != 0)
                                    mListView.removeFooterView(empty_view);
                                if (mIsRefresh) {
                                    mAdapter.clearDatas();
                                    mAdapter.setDatas(o.getPostAndReplyBoList(), isFocued);
                                } else {
                                    mAdapter.setDatas(o.getPostAndReplyBoList(), isFocued);
                                }
                            } else {
                                //空布局
                                if (mListView.getFooterViewsCount() == 0 && mAdapter.getCount() == 0) {
                                    mListView.addFooterView(empty_view, null, false);
                                }
                            }
                            closeLoadingPage();
                            if (o.getPostAndReplyBoList().size() >= 10) {
                                ++rows;
                                isMore = true;
                            } else {
                                isMore = false;
                            }
                            //srl_refreshView.setEnableRefresh(true);
                            mSmartRefresh.setEnableLoadmore(isMore);
                        }


                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                        }

                        @Override
                        public void onCompleted() {
                            super.onCompleted();
                            isLoadingNow = false;
                            mSmartRefresh.finishRefresh();
                            mSmartRefresh.finishLoadmore();
                        }
                    });
                }
            };
        }
        mBasePresenter.getData();
    }

    private void follow() {
        if (DeviceUtils.isFastDoubleClick()) {
            return;
        }
        if (TextUtils.isEmpty(SpUtil.getString(Constant.CACHE_TAG_UID))) {
            startActivity(new Intent(mContext, RegisterActivity.class));
            return;
        }
        String user_id = SpUtil.getString(Constant.CACHE_TAG_UID);
        if (user_id.equals(mUserId)) {
            ToastUtil.show("自己不能关注自己");
            return;
        }
        final String foll = isFocued ? "0" : "1";
        HttpManager.getApi().focus(mUserId, SpUtil.getString(Constant.CACHE_TAG_UID), foll).compose(RxHelper.<JsonNull>handleSimplyResult()).subscribe(new HttpSubscriber<JsonNull>() {
            @Override
            protected void _onNext(JsonNull jsonNull) {
                super._onNext(jsonNull);
                BBSFlownEvent bbsFlownEvent = new BBSFlownEvent();
                bbsFlownEvent.follow = foll;
                bbsFlownEvent.uid = mUserId;
                EventBus.getDefault().post(bbsFlownEvent);
                if (!isLoadingNow) {
                    rows = 0;
                    topicIndexThread(true);
                }
            }

            @Override
            protected void _onError(String message) {
                super._onError(message);
                ToastUtil.show(message);
            }
        });
    }

    @Override
    protected void onReload(Context context) {
        super.onReload(context);
        rows = 0;
        topicIndexThread(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBasePresenter.onDestroy();
    }

    private void updateAttutudeUser(List<CircleMineBean.ContactUser> attutudeUserList) {
        mAttutudeUserLy.removeAllViews();
        if (attutudeUserList != null) {
            for (CircleMineBean.ContactUser attutudeUser : attutudeUserList) {
                CircleImageView headIV = new CircleImageView(getActivity());
                int size = getResources().getDimensionPixelSize(R.dimen.dimen_36dp);
                mAttutudeUserLy.addView(headIV, new LinearLayout.LayoutParams(size, size));
                ImageUtil.display(ConfigUtil.baseImageUserUrl + attutudeUser.avatarPic, headIV, R.mipmap.img_head);
                if (mAttutudeUserLy.getChildCount() >= 4)
                    break;
            }
        }
    }

    public interface OnTopicAlaph {
        void onAlaphValue(float value);
    }

    public void setOnTopicAlaph(OnTopicAlaph onTopicAlaph) {
        this.mOnTopicAlaph = onTopicAlaph;
    }
}
