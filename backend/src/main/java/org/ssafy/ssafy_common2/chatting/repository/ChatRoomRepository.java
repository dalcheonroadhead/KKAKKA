package org.ssafy.ssafy_common2.chatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.ssafy.ssafy_common2.chatting.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {


    // 1) 해당 이메일 명의로 삭제 되지 않은 중계방이 생성되었는지 확인
    Optional<ChatRoom> findChatRoomByChatRoomTypeAndChatOwnerEmailAndDeletedAtIsNull(ChatRoom.ChatRoomType chatRoomType, String email);

    // 2) 해당 이메일로 만들어진 채팅방 리스트 조회
    List<ChatRoom> findAllByChatOwnerEmailAndDeletedAtIsNull(String chat_owner_email);

    // 3) 채팅방을 유저 중 한 명이 나갔을 때, 채팅방 수정일자를 업데이트 한다.
    @Modifying
    @Transactional
    @Query(value = "UPDATE chat_room cr set cr.updated_at = :now where cr.chat_room_id = :roomId", nativeQuery = true)
    void updateModifiedAt(LocalDateTime now, long roomId);


    // 4) 방의 인원수를 줄이거나 늘림
    @Modifying
    @Transactional
    @Query(value = "UPDATE chat_room cr set cr.user_cnt =:cnt where cr.id = :roomId", nativeQuery = true)
    void updateUserCnt(int cnt, long roomId);


    // 5) 중계방이면서, 10분이 안 지났고, Delete 되지 않은 함수
    Optional<List<ChatRoom>> findAllByChatRoomTypeAndTenMinuteIsFalseAndDeletedAtIsNull(ChatRoom.ChatRoomType chatRoomType);

    // 6) 특정 채팅방의 10분 지났는지 여부를 true로 변경!
    @Modifying
    @Transactional
    @Query(value = "UPDATE chat_room cr set cr.ten_minute = true where cr.id = :roomId", nativeQuery = true)
    void updateIsTenMinute(long roomId);

    // 7) 채팅방 이긴다 Point 최신화
    @Modifying
    @Query(value = "UPDATE chat_room cr SET cr.win_point = :betPrice WHERE cr.id = :roomId ", nativeQuery = true)
    void updateWinPoint(long roomId, int betPrice);


    // 8) 채팅방 진다 Point 최신화
    @Modifying
    @Query(value = "UPDATE chat_room cr SET cr.lose_point =:betPrice WHERE cr.id = :roomId", nativeQuery = true)
    void updateLosePoint(long roomId, int betPrice);

    // 9) userEmail과 채팅방 타입이 MANY인 걸 특정하여 하나의 채팅방을 반환한다. (친구의 라이브 중계방 찾기용)


}
