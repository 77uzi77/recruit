package com.yidong.recruit.service.impl;

import com.yidong.recruit.entity.Message;
import com.yidong.recruit.entity.Queue;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.entity.TemplateData;
import com.yidong.recruit.mapper.QueueMapper;
import com.yidong.recruit.mapper.UserMapper;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.AccessTokenUtil;
import com.yidong.recruit.util.TimeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lzc
 * @date 2021/4/25 19 57
 * discription
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @param one
     * @return void
     * @author lzc
     * @date 2021/4/25
     *  报名
     */
    @Override
    public void addOne(Sign one) {
        // 通过 openId 判断用户 是否 报名过
        Sign checkSign = new Sign();
        checkSign.setOpenid(one.getOpenid());
        checkSign.setId(null);
        Sign checkOne = userMapper.selectOne(checkSign);
        // 报名过则修改
        if (checkOne != null) {
            one.setId(checkOne.getId());
            userMapper.updateByPrimaryKeySelective(one);
        // 没报名则新增
        }else{
            userMapper.insertSelective(one);
        }
    }

    @Override
    public String getStatus(String openid) {
        // 通过 openId 得到用户状态
        Sign one = new Sign();
        one.setOpenid(openid);
        Sign res = userMapper.selectOne(one);
        if (res != null){
            // 已报名，则返回用户 状态
            return res.getStatus();
        }else{
            // 未报名，则返回默认值 0
            return "0";
        }
    }

    @Override
    public String wait(String openid) {
        String message;

        Sign one = new Sign();
        one.setOpenid(openid);
        Sign res = userMapper.selectOne(one);
        if (res != null){
            Map<String,String> data = new HashMap<>();
            data.put("openid",openid);
            data.put("time", TimeUtil.getCurrentTime());
            rabbitTemplate.convertAndSend("waitExchange","waitRouting",data);
            message = "排队成功！";
        }else{
            message = "排队失败！";
        }
        return message;
    }

    @Override
    public Sign getOne(String openid) {
        Sign one = new Sign();
        one.setOpenid(openid);
        return userMapper.selectOne(one);
    }

    @Override
    public void updateStatus(String openid,String status) {
        Sign one = new Sign();
        one.setOpenid(openid);
        one.setStatus(status);
        Example example = new Example(Sign.class);
        example.createCriteria().andEqualTo("openid",openid);
        userMapper.updateByExampleSelective(one,example);
    }

    @Override
    public String ifHadSigned(String openid) {
        // 通过 openId 判断用户 是否 报名过
        Sign checkSign = new Sign();
        checkSign.setOpenid(openid);
        String result;

        if (userMapper.selectOne(checkSign) != null){
            // 已报名则返回提示：您已报名，确定重复报名？
            result = "您已报名，确定重复报名？";
        }else{
            result = "是否确定报名？";
            // 未报名则新增: 调用addOne接口
        }
        return result;
    }


    @Override
    public String pushMessage(Integer id) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String access_token = AccessTokenUtil.getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;

        // 通过id查找对应排队的人的openid
        Queue queue = new Queue();
        queue.setId(id);
        Queue resQueue = queueMapper.selectOne(queue);
        String openid = resQueue.getOpenid();
        System.out.println(openid);

        // 封装推送消息的模板内容
        Map<String, TemplateData> data = new HashMap<>();
        data.put("面试通知",new TemplateData("您可以面试啦"));
        data.put("面试地点",new TemplateData("教五创客C区"));

        // 拼接推送的模板
        Message message = new Message();
        message.setId(id);
        message.setTouser(openid);
        message.setTemplate_id("KvBGv6vFbfxUvryDC1XQlpyHVzz3E5V8Q1Z0D86u47Q");
        //    message.setPage("/pages/index");
        message.setData(data);

        // 发送
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url,message,String.class);
        System.out.println("推送返回的信息是：" + responseEntity.getBody());
        return responseEntity.getBody();
    }

   /* @Override
    public Queue getQueueById(Integer id) {
        return  userMapper.getQueueById(id);
    }*/

}
