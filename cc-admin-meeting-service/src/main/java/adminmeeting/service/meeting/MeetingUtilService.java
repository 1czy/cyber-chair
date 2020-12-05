package adminmeeting.service.meeting;

import adminmeeting.api.*;
import adminmeeting.domain.Article;
import adminmeeting.domain.Meeting;
import adminmeeting.domain.PCMemberRelation;
import adminmeeting.domain.User;
import adminmeeting.exception.*;
import adminmeeting.repository.*;
import adminmeeting.request.meeting.MeetingApplicationRequest;
import adminmeeting.request.meeting.PCMemberInvitationRequest;
import adminmeeting.request.meeting.ResultPublishRequest;
import adminmeeting.utility.contract.MeetingStatus;
import adminmeeting.utility.contract.PCmemberRelationStatus;
import adminmeeting.utility.response.ResponseGenerator;
import adminmeeting.utility.response.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MeetingUtilService {

    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private PCMemberRelationRepository pcMemberRelationRepository;

    private UserApi userApi = new UserApi();
    private ArticleApi articleApi = new ArticleApi();

    @Autowired
    public MeetingUtilService(MeetingRepository meetingRepository, PCMemberRelationRepository pcMemberRelationRepository) {
        this.meetingRepository = meetingRepository;
        this.pcMemberRelationRepository = pcMemberRelationRepository;
    }

    public ResponseWrapper<?> meetingApplication(MeetingApplicationRequest request, String token){
        if (userApi.findByUsername(request.getChairName(), token) == null) {
            throw new UserNamedidntExistException(request.getChairName());
        }//chair是否是一个用户

        if (meetingRepository.findByMeetingName(request.getMeetingName()) != null) {
            throw new MeetingNameHasbeenregisteredException(request.getMeetingName());
        }//会议名称是否可用
        if(request.getTopic().isEmpty()){
            throw new AtLeastOneTopicException();
        }

        Meeting meeting = new Meeting(request.getChairName(), request.getMeetingName(), request.getAcronym(), request.getRegion(), request.getCity(), request.getVenue(), request.getTopic(), request.getOrganizer(), request.getWebPage(), request.getSubmissionDeadlineDate(), request.getNotificationOfAcceptanceDate(), request.getConferenceDate(), MeetingStatus.unprocessed);
        meetingRepository.save(meeting);

        User chair = userApi.findByUsername(request.getChairName(), token);
        PCMemberRelation pcMemberRelationForChair = new PCMemberRelation(chair.getId(),meeting.getId(), PCmemberRelationStatus.accepted,request.getTopic());
        pcMemberRelationRepository.save(pcMemberRelationForChair);
        return new ResponseWrapper<>(200, ResponseGenerator.success, null);
    }

    @Transactional
    public ResponseWrapper<?> getmeetingInfo(String meetingName, String token){
        Meeting meeting = meetingRepository.findByMeetingName(meetingName);
        if (meeting == null) {
            throw new MeetingOfNoExistenceException(meetingName);
        }

        return ResponseGenerator.injectObjectFromObjectToResponse("meetingInfo",meeting,new String[]{"chairName","meetingName","acronym","region","city","venue","topic","organizer","webPage","submissionDeadlineDate","notificationOfAcceptanceDate","conferenceDate","status"}, null);
    }

    public ResponseWrapper<?> pcmInvitation(PCMemberInvitationRequest request, String token){
        String meetingName = request.getMeetingName();
        Meeting meeting = meetingRepository.findByMeetingName(meetingName);

        if (meeting == null) {
            throw new MeetingOfNoExistenceException(meetingName);
        }//会议是否存在

        if(meeting.getChairName().equals(request.getPcMemberName())){
            throw new InvitationTargetIsForbiddenException(meetingName);
        }//邀请对象不能是chair本人

        String meetingStatus = meeting.getStatus();
        if(meetingStatus.equals(MeetingStatus.applyFailed)||meetingStatus.equals(MeetingStatus.unprocessed)||meetingStatus.equals(MeetingStatus.reviewing)||meetingStatus.equals(MeetingStatus.reviewCompleted)){
            throw new MeetingStatusUnavailableForPCMemberInvitationException(meetingName);
        }//会议状态是否允许进行成员邀请

        User user = userApi.findByUsername(request.getPcMemberName(), token);
        if (user == null) {
            throw new UserNamedidntExistException(request.getPcMemberName());
        }//邀请对象是否存在邀请

        PCMemberRelation pcMemberRelation = new PCMemberRelation(user.getId(),meeting.getId(), PCmemberRelationStatus.undealed,null);

        pcMemberRelationRepository.save(pcMemberRelation);
        return new ResponseWrapper<>(200, ResponseGenerator.success, null);
    }
    @Transactional
    public ResponseWrapper<?> getInvitationStatus(String meetingName, String token) {
        Meeting meeting = meetingRepository.findByMeetingName(meetingName);
        if (meeting == null) {
            throw new MeetingOfNoExistenceException(meetingName);
        }//会议是否存在
        List<PCMemberRelation> pcMemberRelations = pcMemberRelationRepository.findByMeetingId(meeting.getId());
        Set<HashMap<String, Object>> responseSet = new HashSet<>();
        for (PCMemberRelation x: pcMemberRelations) {
            User user = userApi.findById((long)x.getPcmemberId(), token);
            HashMap<String, Object> map = ResponseGenerator.generate(user,new String[]{"username","fullname","email","institution","region"},null);
            map.put("status",x.getStatus());
            responseSet.add(map);
        }
        HashMap<String, Set<HashMap<String, Object>>> body = new HashMap<>();
        body.put("invitationStatus", responseSet);
        return new ResponseWrapper<>(200, ResponseGenerator.success, body);
    }

    @Transactional
    public ResponseWrapper<?> getSubmissionList(String authorName, String meetingName, String token) {
        Meeting meeting = meetingRepository.findByMeetingName(meetingName);
        if (meeting == null) {
            throw new MeetingOfNoExistenceException(meetingName);
        }//会议是否存在
        List<Article> articles = articleApi.findByContributorNameAndMeetingName(authorName,meetingName, token);
        return ResponseGenerator.injectObjectFromListToResponse("articles",articles,new String[]{"id","title","topic","status"},null);
    }

    public ResponseWrapper<?> reviewPublish(ResultPublishRequest request, String token) {
        Meeting meeting = meetingRepository.findByMeetingName(request.getMeetingName());
        if (meeting == null) {
            throw new MeetingOfNoExistenceException(request.getMeetingName());
        }//会议是否存在
        if(!meeting.getStatus().equals(MeetingStatus.reviewCompleted)){
            throw new MeetingStatusUnAvailableToReviewException();
        }
        meeting.setStatus(MeetingStatus.resultPublished);
        meetingRepository.save(meeting);
        return new ResponseWrapper<>(200, ResponseGenerator.success, null);
    }
}
