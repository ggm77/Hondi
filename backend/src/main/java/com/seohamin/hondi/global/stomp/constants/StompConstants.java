package com.seohamin.hondi.global.stomp.constants;

public final class StompConstants {

    private StompConstants() {}

    public static final String AUTH_HEADER = "Authorization";
    public static final String PREFIX_BEARER = "Bearer ";

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_AUTHORITIES = "authorities";
    public static final String ATTR_EXP = "exp";

    //채팅방 구독 주소, 뒤에 모집글 ID가 붙음 (ex. /topic/chat.room.1)
    public static final String DEST_CHAT_ROOM_PREFIX = "/topic/chat.room.";
    public static final String DEST_ERROR_MESSAGE = "/queue/error";
}
