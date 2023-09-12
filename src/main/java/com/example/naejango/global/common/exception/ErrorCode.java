package com.example.naejango.global.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    /** 400 BAD_REQUEST : 잘못된 요청 */
    FORGED_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    ALREADY_LOGGED_IN(HttpStatus.BAD_REQUEST, "이미 로그인 된 회원입니다."),
    UNIDENTIFIED_DESTINATION(HttpStatus.BAD_REQUEST, "채널 지정이 잘못되었습니다."),
    CANNOT_GENERATE_GROUP_CHANNEL(HttpStatus.BAD_REQUEST, "그룹 채널을 생성할 수 없습니다."),
    TRANSACTION_NOT_MODIFICATION(HttpStatus.BAD_REQUEST, "수정 할 수 없는 거래입니다."),
    TRANSACTION_NOT_DELETE(HttpStatus.BAD_REQUEST, "삭제 할 수 없는 거래입니다."),
    REISSUE_TOKEN_FAILURE(HttpStatus.BAD_REQUEST, "토큰 발급에 실패했습니다."),

    /** 401 UNAUTHORIZED : 권한 없음 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "회원에게 요청 권한이 없습니다."),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인 되어 있지 않은 상태입니다."),
    TOKEN_DECRYPTION_FAILURE(HttpStatus.UNAUTHORIZED, "액세스화 토큰 복호화에 실패하였습니다."),
    NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "로그인이 필요한 요청입니다. 엑세스 토큰 및 리프레시 토큰을 찾을 수 없습니다."),
    ACCESS_TOKEN_REISSUE(HttpStatus.UNAUTHORIZED, "엑세스 토큰을 재발급 합니다."),
    ACCESS_TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "엑세스 토큰을 찾을 수 없거나, 만료된 엑세스 토큰입니다."),

    /** 403 FORBIDDEN : 사용자가 콘텐츠에 접근할 권리를 가지고 있지 않음 */
    UNAUTHORIZED_MODIFICATION_REQUEST(HttpStatus.FORBIDDEN, "수정 권한이 없습니다."),
    UNAUTHORIZED_DELETE_REQUEST(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다."),
    UNAUTHORIZED_READ_REQUEST(HttpStatus.FORBIDDEN, "조회 권한이 없습니다."),
    UNAUTHORIZED_CREATE_CHANNEL(HttpStatus.FORBIDDEN, "채널 개설 권한이 없습니다."),
    UNAUTHORIZED_SUBSCRIBE_REQUEST(HttpStatus.FORBIDDEN, "채팅 채널 입장 권한이 없습니다."),
    UNAUTHORIZED_SEND_MESSAGE_REQUEST(HttpStatus.FORBIDDEN, "해당 채팅 채널에 메세지를 보낼 권한이 없습니다."),
    SIGNUP_INCOMPLETE(HttpStatus.FORBIDDEN, "회원가입 절차가 완료되지 않은 회원입니다."),

    /** 404 NOT_FOUND : 리소스를 찾을 수 없음 */
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메세지를 찾을 수 없습니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다."),
    USERPROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 프로필을 찾을 수 없습니다."),
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Chat 을 찾을 수 없습니다."),
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 User 를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Category를 찾을 수 없습니다."),
    STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Storage를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Item을 찾을 수 없습니다."),
    WISH_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Wish를 찾을 수 없습니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Follow를 찾을 수 없습니다."),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Transaction을 찾을 수 없습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 Account 를 찾을 수 없습니다."),

    /** 409 : CONFLICT : 리소스의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
    SESSION_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 진행중인 채팅이 있습니다."),
    CHANNEL_IS_FULL(HttpStatus.CONFLICT, "해당 채널의 정원이 가득 찼습니다."),
    GROUP_CHANNEL_ALREADY_EXIST(HttpStatus.CONFLICT, "해당 아이템에 할당된 그룹 채널이 이미 있습니다."),
    TOKEN_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 리프레시 토큰을 가지고 있습니다. 엑세스 토큰을 재발급 합니다."),
    WISH_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 관심 등록 되어있습니다."),
    FOLLOW_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 팔로우 등록 되어있습니다.")
    ;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    // CATEGORY_NOT_FOUND 등의 enum name
    public String getName() {
        return this.name();
    }

    // BAD_REQUEST 등의 Status 이름
    public String getHttpStatusErrorName() {
        return httpStatus.name();
    }

    // 400 등의 Status 코드
    public int getHttpStatusCode() {
        return httpStatus.value();
    }

    // 상세 메시지
    public String getMessage() {
        return message;
    }

}
