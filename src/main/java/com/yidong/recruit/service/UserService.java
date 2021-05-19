package com.yidong.recruit.service;


import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.entity.vo.OrderTime;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author lzc
 * @date 2021/4/25 19 43
 * discription
 */
public interface UserService {

    /**
     * @param one
     * @return void
     * @author lzc
     * @date 2021/4/25
     * 报名
     */
    Map<String, String> addOne(Sign one);

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/4/30
     * 通过 openid 得到 用户状态
     */
    String getStatus(String openid);

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/4/30
     * 用户 排队 功能
     */
    String wait(String openid);

    /***
     * @param openid
     * @return Sign
     * @author lzc
     * @date 2021/5/6
     *  通过openid查询用户
     */
    Sign getOne(String openid);

    /***
     * @param openid
     * @param status
     * @return void
     * @author lzc
     * @date 2021/5/6
     *  更新用户状态
     */
    void updateStatus(String openid,String status);

    /**
     * @param openid
     * @return String
     * @author ly
     * @date 2021/5/16
     *  判断用户是否已报名
     */
    String ifHadSigned(String openid);

    /**
     * @param sign
     * @return List<Sign>
     * @author lzc
     * @date 2021/4/30
     * 条件查询 用户信息
     */
    List<Sign> findUserInfo(Sign sign);

    /**
     * @param direction
     * @return String
     * @author lzc
     * @date 2021/5/7
     * 得到等待队列
     */
    String[] getWaitQueue(String direction);

    String getNext(String direction);

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/5/7
     * 根据openid 查找  等待队列
     */
    String[] getWaitQueueByOpenid(String openid);

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/5/16
     *  取消排队
     */
    String cancelWait(String openid);

    /**
     * @param openid
     * @return String
     * @author ly
     * @date 2021/5/16
     * 推送消息
     */
    String pushMessage(String openid) throws Exception;

    /**
     * @param time
     * @return String
     * @author lzc
     * @date 2021/5/16
     * 预约面试时间
     */
    String orderTime(OrderTime time);

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/5/16
     *  得到面试时间
     */
    String getTime(String openid);

    Integer getOrderCount(String date,String openid);
}
