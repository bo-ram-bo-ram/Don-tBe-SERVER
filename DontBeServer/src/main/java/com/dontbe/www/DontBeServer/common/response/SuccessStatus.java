package com.dontbe.www.DontBeServer.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /**
     * auth
     */
    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
    SIGNIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    GET_NEW_TOKEN_SUCCESS(HttpStatus.OK,"토큰 재발급 성공"),
    WITHDRAWAL_SUCCESS(HttpStatus.OK,"유저 탈퇴 성공"),

    /**
     * content
     */
    CONTENT_LIKE_SUCCESS(HttpStatus.CREATED, "게시물 좋아요 성공"),
    GET_CONTENT_DETAIL_SUCCESS(HttpStatus.OK, "게시물 상세 조회 성공"),
    POST_CONTENT_SUCCESS(HttpStatus.CREATED,"게시글 작성 성공"),
    DELETE_CONTENT_SUCCESS(HttpStatus.OK,"게시글 삭제 성공"),
    CONTENT_UNLIKE_SUCCESS(HttpStatus.OK,"게시글 좋아요 취소 성공"),

    /**
     * comment
     */
    POST_COMMENT_SUCCESS(HttpStatus.CREATED,"답글 작성 성공"),
    DELETE_COMMENT_SUCCESS(HttpStatus.OK, "답글 삭제 성공"),

    /**
     * member
     */
    GET_MEMBER_DETAIL(HttpStatus.OK,"계정 정보 조회 성공"),
    GET_PROFILE_SUCCESS(HttpStatus.OK,"유저 프로필 조회 성공"),
    CLICK_MEMBER_GHOST_SUCCESS(HttpStatus.CREATED,"투명도 낮추기 성공"),
    PATCH_MEMBER_PROFILE(HttpStatus.OK, "프로필 수정 완료")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}

