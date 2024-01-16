package com.dontbe.www.DontBeServer.api.comment.service;

import com.dontbe.www.DontBeServer.api.comment.domain.Comment;
import com.dontbe.www.DontBeServer.api.comment.domain.CommentLiked;
import com.dontbe.www.DontBeServer.api.comment.dto.request.CommentLikedRequestDto;
import com.dontbe.www.DontBeServer.api.comment.dto.request.CommentPostRequestDto;
import com.dontbe.www.DontBeServer.api.comment.repository.CommentLikedRepository;
import com.dontbe.www.DontBeServer.api.comment.repository.CommentRepository;
import com.dontbe.www.DontBeServer.api.content.domain.Content;
import com.dontbe.www.DontBeServer.api.content.repository.ContentRepository;
import com.dontbe.www.DontBeServer.api.member.domain.Member;
import com.dontbe.www.DontBeServer.api.member.repository.MemberRepository;
import com.dontbe.www.DontBeServer.api.notification.domain.Notification;
import com.dontbe.www.DontBeServer.api.notification.repository.NotificationRepository;
import com.dontbe.www.DontBeServer.common.exception.BadRequestException;
import com.dontbe.www.DontBeServer.common.exception.UnAuthorizedException;
import com.dontbe.www.DontBeServer.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommendService {
    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;
    private final MemberRepository memberRepository;
    private final CommentLikedRepository commentLikedRepository;
    private final NotificationRepository notificationRepository;

    public void postComment(Long memberId, Long contentId, CommentPostRequestDto commentPostRequestDto){
        Content content = contentRepository.findContentByIdOrThrow(contentId); // 게시물id 잘못됐을 때 에러
        Member member = memberRepository.findMemberByIdOrThrow(memberId);
        Comment comment = Comment.builder()
                .member(member)
                .content(content)
                .commentText(commentPostRequestDto.commentText())
                .build();
        Comment savedComment = commentRepository.save(comment);
    }

    public void deleteComment(Long memberId, Long commentId) {
        deleteValidate(memberId, commentId);
        commentRepository.deleteById(commentId);
    }

    public void deleteValidate(Long memberId, Long commentId){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new IllegalArgumentException(ErrorStatus.NOT_FOUND_COMMENT.getMessage()));

        Long commentMemberId = comment.getMember().getId();
        System.out.println(commentId);
        if (!commentMemberId.equals(memberId)) {    //답글작성자 != 현재 유저 >> 권한error
            throw new BadRequestException(ErrorStatus.UNAUTHORIZED_MEMBER.getMessage());
        }
    }

    public void likeComment(Long memberId, Long commentId, CommentLikedRequestDto commentLikedRequestDto){

        Member triggerMember = memberRepository.findMemberByIdOrThrow(memberId);
        Comment comment = commentRepository.findCommentByIdOrThrow(commentId);
        Long contentId = comment.getContent().getId();


        isDuplicateCommentLike(comment, triggerMember);

        Member targetMember = memberRepository.findMemberByIdOrThrow(comment.getMember().getId());
        CommentLiked commentLiked =  CommentLiked.builder()
                .comment(comment)
                .member(triggerMember)
                .build();
        CommentLiked savedCommentLiked = commentLikedRepository.save(commentLiked);

        if(triggerMember != targetMember){  ////자신 게시물에 대한 좋아요 누르면 알림 발생 x
            //노티 엔티티와 연결
            Notification notification = Notification.builder()
                    .notificationTargetMember(targetMember)
                    .notificationTriggerMemberId(triggerMember.getId())
                    .notificationTriggerType(commentLikedRequestDto.notificationTriggerType())
                    .notificationTriggerId(commentId)   //에러수정을 위한 notificationTriggerId에 답글id 저장, 알림 조회시 답글id로 게시글id 반환하도록하기
                    .isNotificationChecked(false)
                    .notificationText(comment.getCommentText())
                    .build();
            Notification savedNotification = notificationRepository.save(notification);
        }
    }

    public void unlikeComment(Long memberId, Long commentId){
        Member triggerMember = memberRepository.findMemberByIdOrThrow(memberId);
        Comment comment = commentRepository.findCommentByIdOrThrow(commentId);
        Member targetMember = commentRepository.findCommentByIdOrThrow(commentId).getMember();

        //로그인 유저가 trigger유저이고 답글id가 triggerId일때
        if(commentLikedRepository.existsByCommentAndMember(comment,triggerMember)){

            commentLikedRepository.deleteByCommentAndMember(comment,triggerMember);

            notificationRepository.deleteByNotificationTargetMemberAndNotificationTriggerMemberIdAndNotificationTriggerTypeAndNotificationTriggerId
                    (targetMember, memberId,"commentLiked", comment.getId());

        }else{ //댓글좋아요 엔티티에 존재하지 않을 경우 예외처리
            throw new BadRequestException(ErrorStatus.UNEXITST_COMENT_LIKE.getMessage());
        }
    }

    private void isDuplicateCommentLike(Comment comment, Member member) {
        if (commentLikedRepository.existsByCommentAndMember(comment, member)) {
            throw new BadRequestException(ErrorStatus.DUPLICATION_COMMENT_LIKE.getMessage());
        }
    }
}
