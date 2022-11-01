package com.wh.wenniao.event;

import com.alibaba.fastjson.JSONObject;
import com.wh.wenniao.entity.DiscussPost;
import com.wh.wenniao.entity.Event;
import com.wh.wenniao.entity.User;
import com.wh.wenniao.mapper.UserMapper;
import com.wh.wenniao.service.DiscussPostService;
import com.wh.wenniao.service.impl.ElasticsearchService;
import com.wh.wenniao.util.CommunityConstant;
import com.wh.wenniao.util.MailClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

/**
 * 事件消费者
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

//    @Autowired
//    private MessageService messageService;
//
    @Resource
    private DiscussPostService discussPostService;
//
    @Resource
    private ElasticsearchService elasticsearchService;
//
//    @Autowired
//    private HostHolder hostHolder;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    // 项目名(访问路径)
    @Value("${server.servlet.context-path}")
    private String contextPath;

    //域名
    @Value("${domain}")
    private String domain;

    @Resource
    private TemplateEngine templateEngine;

    @KafkaListener(topics = {TOPIC_MAIL})
    public void handleMailMessage(ConsumerRecord record){
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        User user = userMapper.selectById(event.getUserId());
        // 给注册用户发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8081/wenniao/activation/用户id/激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活 文鸟 账号", content);


    }

//    /**
//     * 消费评论、点赞、关注事件
//     * @param record
//     */
//    @KafkaListener(topics = {TOPIC_COMMNET, TOPIC_LIKE, TOPIC_FOLLOW})
//    public void handleMessage(ConsumerRecord record) {
//        if (record == null || record.value() == null) {
//            logger.error("消息的内容为空");
//            return ;
//        }
//        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
//        if (event == null) {
//            logger.error("消息格式错误");
//            return ;
//        }
//
//        // 发送系统通知
//        Message message = new Message();
//        message.setFromId(SYSTEM_USER_ID);
//        message.setToId(event.getEntityUserId());
//        message.setConversationId(event.getTopic());
//        message.setCreateTime(new Date());
//
//        Map<String, Object> content = new HashMap<>();
//        content.put("userId", event.getUserId());
//        content.put("entityType", event.getEntityType());
//        content.put("entityId", event.getEntityId());
//        if (!event.getData().isEmpty()) { // 存储 Event 中的 Data
//            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
//                content.put(entry.getKey(), entry.getValue());
//            }
//        }
//        message.setContent(JSONObject.toJSONString(content));
//
//        messageService.addMessage(message);
//
//    }
//
    /**
     * 消费发帖事件
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return ;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscusspost(post);

    }
//
//    /**
//     * 消费删帖事件
//     */
//    @KafkaListener(topics = {TOPIC_DELETE})
//    public void handleDeleteMessage(ConsumerRecord record) {
//        if (record == null || record.value() == null) {
//            logger.error("消息的内容为空");
//            return ;
//        }
//        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
//        if (event == null) {
//            logger.error("消息格式错误");
//            return ;
//        }
//
//        elasticsearchService.deleteDiscusspost(event.getEntityId());
//
//    }

}
