package com.yidong.recruit.service;


import com.yidong.recruit.entity.Sign;

/**
 * @author lzc
 * @date 2021/4/25 19 43
 * discription
 */
public interface UserService {

    void addOne(Sign sign);

    String getStatus(String openid);

    String wait(String openid);

    Sign getOne(String openid);

    void updateStatus(String openid,String status);

    String ifHadSigned(String openid);

}
