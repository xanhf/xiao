package com.honglu.future.config;

public class Constant {

    //提额H5
    public static String LIMIT_URL = "http://app.souyijie.com/web/web/gotoRegisterNew/555";

    public static final String DEBUG_TAG = "logger";// LogCat的标记

    //sharedpren里面的常量
    public static final String CACHE_TAG_USERNAME = "username"; //用户名
    public static final String CACHE_TAG_SESSIONID = "sessionid";//sessionId
    public static final String CACHE_TAG_UID = "uid";//uid

    public static final String CACHE_USERLIST_KEY = "userList";

    public static final String CACHE_USER_INFO = "userinfo";//用户信息
    public static final String CACHE_USER_AVATAR= "useravatar";//用户信息
    public static final String CACHE_USER_ASSES= "userasses";//用户资产

    public static final String CACHE_ACCOUNT_TOKEN = "CACHE_ACCOUNT_TOKEN"; //期货账号token
    public static final String CACHE_ACCOUNT_USER_NAME= "CACHE_ACCOUNT_USER_NAME"; //期货账号

    public static final String CUSTOMER_PHONE = "4001150707";//客服电话
    public static final String COMPANY_CODE = "GUOFU";//公司代码

    public static final String CACHE_TAG_COUNT_DOWN = "showCountDown";    //显示倒计时页面
    public static final String CACHE_TAG_REPAY_COUNT_DOWN = "showRepaymentCountDown";    //显示还款倒计时页面
    public static final String CACHE_TAG_NEXT_LOAN = "showNextLoanCountDown";    //显示下次申请借款倒计时页面

    public static final String CACHE_CALENDAR_PERMISSIONS = "calendarPermissions";//是否允许往日历中插入数据

    public static final String CACHE_CALENDAR_LOAN_DATE = "loanStartTime";    //日历中插入的下次可借款时间
    public static final String CACHE_CALENDAR_REPAY_DATE = "loanEndTime";    //日历中插入的还款时间
    public static final String CACHE_CALENDAR_REPAY_MONEY = "loanEndMoney";    //日历中插入的还款金额

    public static final String CACHE_TAG_MOBILE = "mobileNumber"; //手机号码

    public static final String URL_KEY = "baseUrlKey";

    public static final String ZX_MARKET_KEY = "zx_market_key";//行情自选保存key
    //判断是不是第一次进app 是的话暂时引导页
    public static final String CACHE_IS_FIRST_LOGIN = "FirstLogin";//key
    public static final int HAS_ALREADY_LOGIN = 1;//首次
    public static final int NOT_FIRST_LOGIN = -1;

    /*方向 1.跌 2.涨*/
    public static final int TYPE_BUY_DOWN = 1;
    public static final int TYPE_BUY_UP = 2;

    public static final String CODE_SHFE = "SHFE";//上海期货交易所

    //支付结果
    public static final String PAY_RESULT_LEND_FAILED = "PAY_RESULT_LEND_FAILED";


    //智齿机器人对应的key
    //public static       String SOBOT_KEY          = "0588ea10f6d34312a468606ac430d12f";
    public static String SOBOT_KEY = "dcdd2819c8c045d680e279e1c1ac4073";
    /************
     * 提升额度配置
     */
    public static final int TAG_QUOTA_PERSONAL = 1;//个人
    public static final int TAG_QUOTA_WORK = 2;//工作
    public static final int TAG_QUOTA_CONTACT = 3;//联系人
    public static final int TAG_QUOTA_BANK = 4;//银行卡信息
    public static final int TAG_QUOTA_PHONE = 5;//手机运营商
    public static final int TAG_QUOTA_MORE = 7;//更多
    public static final int TAG_QUOTA_ZMXY = 8;//芝麻信用
    public static final int TAG_QUOTA_ALIPAY = 9;//芝麻信用

    //位置信息上传间隔时间
    public static final int INTERVAL_TIME = 30 * 60 * 1000;
    public static final int RETRIVE_SERVICE_COUNT = 50;
    public static final int BROADCAST_ELAPSED_TIME_DELAY = 2 * 60 * 1000;

    public static final String POI_SERVICE = "com.coder80.timer.service.UploadPOIService";
    //Activity转场动画Key
    public static final String TRANSITION_ANIMATION_SHOW_PIC = "showView";

    public static final String CACHE_TAG_ACTIVITY_IMGURL = "activityImgUrl";
    public static final String CACHE_TAG_ACTIVITY_URL = "activityUrl";

    public static final int OptionalQuotesLITEPALl = 0023;

    public static final String CLOSE_TIME_COMMON = "10:15@10:30,11:30@13:30,23:00@09:00";
    public static final String CLOSE_TIME_BEGIN_NINE = "10:15@10:30,11:30@13:30";
}
