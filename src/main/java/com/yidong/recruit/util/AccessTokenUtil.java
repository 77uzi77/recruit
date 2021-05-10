package com.yidong.recruit.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yidong.recruit.exception.MyException;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


public class AccessTokenUtil {

    private static final String BASE_Url = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String Token_Url = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String APP_ID = "wx77bdfd88a7951579";
    private static final String APP_SECRET = "b011b325a41dc892205b5a231da6f0b3";
    private static final String TOKEN_GRANT_TYPE = "client_credential";
    private static final String LOGIN_GRANT_TYPE = "authorization_code";

    public static String getAccessToken() {

        String url = Token_Url + "?&grant_type=" + TOKEN_GRANT_TYPE + "&appid=" + APP_ID + "&secret=" + APP_SECRET;

        String res = getResponseData(url);

        JSONObject jo = JSON.parseObject(res);
        String access_token = jo.getString("access_token");
        System.out.println("access_token：" + access_token);

        return  access_token;
    }

    public static String getOpenid(String code) {
        String url = BASE_Url + "?appid=" + APP_ID + "&secret=" + APP_SECRET + "&js_code=" + code + "&grant_type=" + LOGIN_GRANT_TYPE;

        String res = getResponseData(url);
        JSONObject jo = JSON.parseObject(res);

        return jo.getString("openid");
    }

    private static String getResponseData(String url) {
        String res = null;
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            // DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);    //GET方式
            CloseableHttpResponse response = null;
            // 配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(false).build();
            // 将上面的配置信息 运用到这个Get请求里
            httpget.setConfig(requestConfig);
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpget);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
//            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                res = EntityUtils.toString(responseEntity);
//                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
//                System.out.println("响应内容为:" + res);
            }
            // 释放资源
            httpClient.close();
            response.close();
        } catch (Exception e) {
            throw new MyException("获取用户openId失败！");
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        String access_token = AccessTokenUtil.getAccessToken();
        System.out.println(access_token);
    }
}
