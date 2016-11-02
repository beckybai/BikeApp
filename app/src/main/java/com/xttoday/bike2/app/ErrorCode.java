package com.xttoday.bike2.app;

/**
 * Created by wuyp on 15/12/24.
 * 用于记录错误代码
 */
public class ErrorCode {
    public static String pointErr = "errorcode:1 路线点集不能为空";//错误出现原因为TraceLine类jsonToPolyline函数返回一个空值
    public static String asyFail = "errorcode:2 异步网络请求失败";//错误出现原因为异步请求失败

}
