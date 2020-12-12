package chair.service.meeting;

import chair.request.meeting.PCMemberInvitationRequest;
import chair.request.meeting.*;

//import chair.service.user.ChairInvitationService;
//import chair.service.user.UserMeetingService;
import chair.utility.response.ResponseGenerator;
import chair.utility.response.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@Service
@RestController
public class ChairMeetingService {

    Logger logger = LoggerFactory.getLogger(ChairMeetingService.class);


    // 消息发送队列名
    public static final String FANOUT_QUEUE_NAME = "fanout_queue";
    public static final String DIRECT_QUEUE_NAME = "direct_queue";
    ///chair
    private ChairMeetingServiceImpl chairMeetingServiceImpl;
    private static String fetched = " have been fetched";
    private static String requested = " have been requested";
    private static String forArticle = "for Article ";

    @Transactional
    public ResponseWrapper<?> pcmInvitation(PCMemberInvitationRequest request) {
        ResponseWrapper<?> ret = chairMeetingServiceImpl.pcmInvitation(request);
        if(ret.getResponseMessage().equals(ResponseGenerator.success)){
            logger.info("Invitation of Meeting named "+ request.getMeetingName() +" to "+"User named " + request.getPcMemberName() + " has been added");
        }
        return  ret;
    }

    @Transactional
    public  ResponseWrapper<?> beginSubmission(BeginSubmissionRequest request) {
        ResponseWrapper<?> ret = chairMeetingServiceImpl.beginSubmission(request);
        if(ret.getResponseMessage().equals(ResponseGenerator.success)){
            logger.info("Submission begin for Meeting named " + request.getMeetingName());
        }
        return ret;
    }
    @Transactional
    public ResponseWrapper<?> reviewPublish(ResultPublishRequest request) {
        ResponseWrapper<?> ret = chairMeetingServiceImpl.reviewPublish(request);
        if(ret.getResponseMessage().equals(ResponseGenerator.success)){
            logger.info("Meeting named " + request.getMeetingName() + " have published reviews");
        }
        return ret;
    }
    @Transactional
    public ResponseWrapper<?> finalPublish(FinalPublishRequest request) {
        ResponseWrapper<?> ret = chairMeetingServiceImpl.finalPublish(request);
        if(ret.getResponseMessage().equals(ResponseGenerator.success)){
            logger.info("Final Publish: " + request.toString());
        }
        return ret;
    }

}
