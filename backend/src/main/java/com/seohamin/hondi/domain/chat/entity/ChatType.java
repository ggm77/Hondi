package com.seohamin.hondi.domain.chat.entity;

public enum ChatType {
    // 기본적인 채팅 메세지
    MESSAGE,
    // 채팅방 입장 (모집글 참여)
    ENTER,
    // 채팅방 나가기
    EXIT,
    // 모집 취소 등 시스템 알림
    SYSTEM
}
