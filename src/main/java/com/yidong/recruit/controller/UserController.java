package com.yidong.recruit.controller;

import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.exception.MyException;
import com.yidong.recruit.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author lzc
 * @date 2021/4/25 13 24
 * discription
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    private static final String BASE_Url = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String APP_ID = "wx77bdfd88a7951579";
    private static final String APP_SECRET = "b011b325a41dc892205b5a231da6f0b3";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String CONNECT_REDIRECT = "1";

    @GetMapping("getUserInfo/{code}")
    @ApiOperation("登录")
    public ResultBean<String> getUserInfo(@PathVariable String code) {
        String url = BASE_Url + "?appid=" + APP_ID + "&secret=" + APP_SECRET + "&js_code=" + code + "&grant_type=" + GRANT_TYPE;

        System.out.println(url);

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

        JSONObject jo = JSON.parseObject(res);
        String openid = jo.getString("openid");
        log.info("登录成功，openid为:{}",openid);
//        System.out.println("openid：" + openid);

        return new ResultBean<>(ResultBean.SUCCESS_CODE,openid);
    }

    /**
     * @param sign
     * @return ResultBean<String>
     * @author lzc
     * @date 2021/4/25
     *  报名
     */
    @PostMapping("sign")
    @ApiOperation("报名")
    public ResultBean<String> sign(@RequestBody Sign sign) {
        userService.addOne(sign);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,"报名成功！");
    }


    @GetMapping("getStatus/{openid}")
    @ApiOperation("得到用户状态")
    public ResultBean<String> getStatus(@PathVariable String openid) {
        String status = userService.getStatus(openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,status);
    }

    @GetMapping("wait/{openid}")
    @ApiOperation("用户排队面试")
    public ResultBean<String> wait(@PathVariable String openid) {
        String message = userService.wait(openid);

        return new ResultBean<>(ResultBean.SUCCESS_CODE, message);
    }

    /**
     * @param openid
     * @return ResultBean<String>
     * @author ly
     * @date 2021/4/29
     *  查找是否重复报名
     */
    @GetMapping("ifHadSigned/{openid}")
    @ApiOperation("查看是否重复报名")
    public ResultBean<String> ifHadSigned(@PathVariable String openid) {
        String message = userService.ifHadSigned(openid);

        return new ResultBean<>(ResultBean.SUCCESS_CODE,message);
    }

    @GetMapping("getWaitQueueByOpenid/{openid}")
    @ApiOperation("根据openid查找等待队列")
    public ResultBean<String[]> getWaitQueueByOpenid(@PathVariable String openid) {
        String[] waitQueue = userService.getWaitQueueByOpenid(openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,waitQueue);
    }

    @GetMapping("pushMessage")
    @ApiOperation("推送消息")
    // 通过排号编号 推送消息
    public ResultBean<String> pushMessage(/*@PathVariable*/ Integer id) throws Exception {
        String message = userService.pushMessage(id);

        return new ResultBean<>(ResultBean.SUCCESS_CODE,message);
    }

}
