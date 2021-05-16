package com.yidong.recruit.service;


import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author lzc
 * @date 2021/4/25 19 43
 * discription
 */
public interface UserService {

   // void addOne(Sign sign);
    Map<String, String> addOne(Sign sign);

    String getStatus(String openid);

    String wait(String openid);

    Sign getOne(String openid);

    void updateStatus(String openid,String status);

    String ifHadSigned(String openid);

    List<Sign> findUserInfo(Sign sign);

    String[] getWaitQueue(String direction);

//    String getFirstForeUser();
//
//    String getFirstBackstageUser();

    String getNext(String direction);

    String[] getWaitQueueByOpenid(String openid);

    String cancelWait(String openid);

    String pushMessage(String openid) throws Exception;

 //   Map<String, String> checkSign(Sign sign);
}
