package userauth.service.postmessage.api;

import userauth.domain.PostMessage;
import userauth.request.postmessage.PostMessageRequest;

import java.util.List;

public interface PostMessageService {

    public void addPostMessage(PostMessageRequest postMessageRequest);

    public List<PostMessage> findPostMessageByArticleIdAndStatus(long articleId, String status);

    public PostMessage findPostMessageById(Long id);
    public void sendMessage(Long id,PostMessage message);
    public Object getMessage(Long id);
}
