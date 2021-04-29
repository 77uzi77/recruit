package com.yidong.recruit.conf;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author lzc
 * @date 2021/4/29 10 53
 * discription
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Queue waitQueue(){
        return new Queue("waitQueue",true);
    }

    @Bean
    DirectExchange waitExchange(){
        return new DirectExchange("waitExchange",true,false);
    }

    @Bean
    Binding bindingWaitQueue(){
        return BindingBuilder.bind(waitQueue()).to(waitExchange()).with("waitRouting");
    }


}
