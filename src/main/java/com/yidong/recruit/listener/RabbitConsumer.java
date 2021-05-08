//package com.yidong.recruit.listener;
//
//import com.rabbitmq.client.Channel;
//import com.yidong.recruit.entity.Sign;
//import com.yidong.recruit.service.UserService;
//import com.yidong.recruit.util.RedisUtil;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author lzc
// * @date 2021/4/29 11 53
// * discription
// */
//@Component
//public class RabbitConsumer implements ChannelAwareMessageListener {
//
//
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private RedisUtil redisUtil;
//
//    // 判断该 用户 是否面试结束
//    public static boolean isFinish = false;
//
//    @Override
//    public void onMessage(Message message, Channel channel) throws Exception {
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//        try {
//            //因为传递消息的时候用的map传递,所以将Map从Message内取出需要做些处理
//            String msg = message.toString();
//            String[] msgArray = msg.split("'");//可以点进Message里面看源码,单引号直接的数据就是我们的map消息数据
//            Map<String, String> msgMap = mapStringToMap(msgArray[1].trim(), 3);
//            String openid = msgMap.get("openid");
//            String time = msgMap.get("time");
//            String direction = msgMap.get("direction");
//
//            // 保留当前面试者openid
//            redisUtil.set("firstUser",openid);
//
//            // 空循环模拟面试过程，等待前台通知面试结束
//            while (!isFinish){
//
//            }
//            // 更新用户状态
//            userService.updateStatus(openid,"1");
//            isFinish = false;
//
//            // 更新等待队列
//            String waitQueue = (String) redisUtil.get("waitQueue");
//            int index = waitQueue.indexOf("$");
//            if (index != -1) {
//                redisUtil.set("waitQueue",waitQueue.substring(index));
//            } else {
//                redisUtil.del("waitQueue");
//            }
//
//            // 删除 该用户 排队信息
//            redisUtil.del(openid);
//            // 更新 后台/前端 排队人数
//            if("后台".equals(direction)) {
//                redisUtil.incr("backstageCount",-1);
//            }else{
//                redisUtil.incr("foreCount",-1);
//            }
//            // 根据 方向 查找 用户 openid
//            Sign chooseSign = new Sign();
//            chooseSign.setDirection(direction);
//            chooseSign.setStatus("2");
//            List<Sign> userList = userService.findUserInfo(chooseSign);
//            // 更新 用户 还需 等待人数
//            for(Sign sign : userList){
//                String userOpenid = sign.getOpenid();
//                Integer waitCount = (Integer) redisUtil.get(userOpenid);
//                if (waitCount != null){
//                    redisUtil.incr(userOpenid,-1);
//                }
//            }
//
//            System.out.println("  MyAckReceiver  openid:" + openid + "  time:" + time);
//            System.out.println("消费的主题消息来自：" + message.getMessageProperties().getConsumerQueue());
//            System.out.println("处理消息成功！");
//
//            channel.basicAck(deliveryTag, true); //第二个参数，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
////			channel.basicReject(deliveryTag, true);//第二个参数，true会重新放回队列，所以需要自己根据业务逻辑判断什么时候使用拒绝
//        } catch (Exception e) {
//            channel.basicReject(deliveryTag, false);
//            e.printStackTrace();
//        }
//    }
//
//    //{key=value,key=value,key=value} 格式转换成map
//    private Map<String, String> mapStringToMap(String str, int entryNum) {
//        str = str.substring(1, str.length() - 1);
//        String[] strs = str.split(",", entryNum);
//        Map<String, String> map = new HashMap<String, String>();
//        for (String string : strs) {
//            String key = string.split("=")[0].trim();
//            String value = string.split("=")[1];
//            map.put(key, value);
//        }
//        return map;
//    }
//}
