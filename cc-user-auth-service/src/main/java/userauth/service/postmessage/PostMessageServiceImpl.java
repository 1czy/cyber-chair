package userauth.service.postmessage;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import userauth.domain.PostMessage;
import userauth.repository.PostRepository;
import userauth.request.postmessage.PostMessageRequest;
import userauth.service.postmessage.api.PostMessageService;
import userauth.utility.response.ResponseGenerator;
import userauth.utility.response.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;



@Service
public class PostMessageServiceImpl implements PostMessageService {
    // 队列名
    public static final String FANOUT_QUEUE_NAME = "fanout_queue";
    //交换机名
    public static final String TEST_FANOUT_EXCHANGE = "fanout_exchange";

    public static final String DIRECT_QUEUE_NAME = "direct_queue";
    public static final String TEST_DIRECT_EXCHANGE = "direct_exchange";
    public static final String DIRECT_ROUTINGKEY = "test";
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private RabbitTemplate template;
    // 创建队列

    @Override
    public void addPostMessage(PostMessageRequest request) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        PostMessage post = new PostMessage(
                request.getPosterId(),
                request.getArticleId(),
                request.getTargetId(),
                request.getContent(),
                request.getStatus(),
                timestamp.toString()
        );
        sendMessage(request.getPosterId(),post);
        postRepository.save(post);
    }

    @Override
    public void sendMessage(Long id, PostMessage message) {
        //如果没有配置默认交换机,直接传入queue的name
        template.convertAndSend("member"+id, message);

    }

    @Override
    public Object getMessage(Long id) {
        return template.receiveAndConvert("number"+id);
    }

    @Override
    public List<PostMessage> findPostMessageByArticleIdAndStatus(long articleId, String status) {
        List<PostMessage> postList = postRepository.findByArticleIdAndStatus(articleId, status);
        return postList;
    }

    @Override
    public PostMessage findPostMessageById(Long id) {
//        PostMessage message = postRepository.findById(id.longValue());
//        return message;
        return (PostMessage) getMessage(id);
    }
}
