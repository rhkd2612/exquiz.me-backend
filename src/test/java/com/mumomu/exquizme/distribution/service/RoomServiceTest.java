package com.mumomu.exquizme.distribution.service;

import com.mumomu.exquizme.distribution.domain.Participant;
import com.mumomu.exquizme.distribution.domain.Room;
import com.mumomu.exquizme.distribution.exception.ClosedRoomAccessException;
import com.mumomu.exquizme.distribution.exception.DuplicateSignUpException;
import com.mumomu.exquizme.distribution.exception.InvalidRoomAccessException;
import com.mumomu.exquizme.distribution.repository.RoomRepository;
import com.mumomu.exquizme.distribution.web.model.ParticipantCreateForm;
import com.mumomu.exquizme.production.domain.Problemset;
import com.mumomu.exquizme.production.service.ProblemService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.Part;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoomServiceTest {
    @Autowired
    EntityManager em;

    @Autowired
    RoomService roomService;
    @Autowired
    ProblemService problemService;

    Room room;
    Room room2;

    String roomPin;
    String roomPin2;

    @BeforeEach
    void setUp(){
        room = roomService.newRoom(1L,5);
        room2 = roomService.newRoom(1L,2);

        roomPin = room.getPin();
        roomPin2 = room2.getPin();
    }

    @AfterEach
    void setDown(){
        try {
            roomService.closeRoomByPin(room.getPin());
            roomService.closeRoomByPin(room2.getPin());
        } catch (Exception ignored){

        }
    }

    @Test
    @Transactional
    @DisplayName("방생성성공")
    void createRoom() throws Exception{
        em.persist(room);
        Room savedRoom = em.find(Room.class, room.getId());

        assertThat(room).isEqualTo(savedRoom);
    }

    @Test
    @Transactional
    @DisplayName("방핀번호조회")
    void findRoomByRoomPin(){
        Room findRoomByPin = roomService.findRoomByPin(room.getPin());

        assertThat(room).isEqualTo(findRoomByPin);
    }

    @Test
    @Transactional
    @DisplayName("방아이디조회")
    void findRoomByRoomId(){
        Room findRoomById= roomService.findRoomById(room.getId());

        assertThat(room).isEqualTo(findRoomById);
    }

    @Test
    @Transactional
    @DisplayName("방생성실패")
    void createRoomFailure() throws Exception{
        assertThrows(RuntimeException.class, ()->{
            int maxCount = 51;
            while(maxCount-- != 0){
                roomService.newRoom(1L,5);
            }
        });
    }

    @Test
    @Transactional
    @DisplayName("존재하지않는방조회")
    void findInvalidRoom(){
        assertThrows(InvalidRoomAccessException.class, ()-> {
            roomService.findRoomByPin("111111");
        });
    }

    @Test
    @Transactional
    @DisplayName("방참여")
    void participateRoom() throws IllegalAccessException {
        Participant participant = Participant.ByBasicBuilder().nickname("userA").sessionId(UUID.randomUUID().toString()).room(room).build();
        Participant savedParticipant = roomService.joinParticipant(new ParticipantCreateForm(participant), roomPin, participant.getSessionId());

        assertThat(room).isEqualTo(savedParticipant.getRoom());
    }

    @Test
    @Transactional
    @DisplayName("방에참여한참여자목록조회")
    void findParticipantsListInRoom() throws IllegalAccessException {
        Participant participant = Participant.ByBasicBuilder().name("a").nickname("userA").sessionId(UUID.randomUUID().toString()).room(room).build();
        Participant participant2 = Participant.ByBasicBuilder().name("b").nickname("userB").sessionId(UUID.randomUUID().toString()).room(room).build();
        Participant participant3 = Participant.ByBasicBuilder().name("c").nickname("userC").sessionId(UUID.randomUUID().toString()).room(room2).build();

        roomService.joinParticipant(new ParticipantCreateForm(participant), roomPin, participant.getSessionId());
        roomService.joinParticipant(new ParticipantCreateForm(participant2), roomPin, participant2.getSessionId());
        roomService.joinParticipant(new ParticipantCreateForm(participant3), roomPin2, participant3.getSessionId());

        assertThat(roomService.findParticipantsByRoomPin(room.getPin()).size()).isEqualTo(2);
        assertThat(roomService.findParticipantsByRoomPin(room2.getPin()).size()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("방삭제")
    void closeRoom(){
        roomService.closeRoomByPin(roomPin);
    }

    @Test
    @Transactional
    @DisplayName("방삭제후재삭제")
    void closeSameRoomTwice(){
        String pin = room.getPin();

        Room roomByPin = roomService.closeRoomByPin(pin);

        assertThrows(InvalidRoomAccessException.class, ()-> {
            Room roomByPin2 = roomService.findRoomByPin(pin);
        });
    }

    @Test
    @Transactional
    @DisplayName("익명사용자입장")
    public void joinAnonymousUser() throws IllegalAccessException {
        ParticipantCreateForm pcForm1 =
                ParticipantCreateForm.builder().name("test").nickname("tester").build();
        Participant participant = roomService.joinParticipant(pcForm1, roomPin, UUID.randomUUID().toString());
        Participant anonymous = roomService.findParticipantBySessionId(participant.getSessionId(), roomPin);

        assertThat(anonymous).isEqualTo(participant);
    }

    @Test
    @Transactional
    @DisplayName("익명사용자재가입")
    public void joinSameAnonymousUserTwice() throws IllegalAccessException {
        Participant participant =
                Participant.ByBasicBuilder().name("test").nickname("tester").sessionId(UUID.randomUUID().toString()).build();
        Participant participant2 =
                Participant.ByBasicBuilder().name("nani").nickname("nanida").sessionId(participant.getSessionId()).build();

        Participant anonymous = roomService.joinParticipant(new ParticipantCreateForm(participant), roomPin, participant.getSessionId());
        Participant anonymous2 = roomService.joinParticipant(new ParticipantCreateForm(participant2), roomPin, participant2.getSessionId());

        assertThat(anonymous.getSessionId()).isEqualTo(anonymous2.getSessionId());
        assertThat(anonymous2.getNickname()).isEqualTo(participant2.getNickname());
        assertThat(anonymous2.getName()).isEqualTo(participant2.getName());
    }

    @Test
    @Transactional
    @DisplayName("두익명사용자입장")
    public void joinTwoAnonymousUser() throws IllegalAccessException {
        ParticipantCreateForm pcForm1 =
                ParticipantCreateForm.builder().name("test").nickname("tester").build();
        ParticipantCreateForm pcForm2 =
                ParticipantCreateForm.builder().name("test2").nickname("tester2").build();

        Participant participant = roomService.joinParticipant(pcForm1, roomPin, UUID.randomUUID().toString());
        Participant participant2 = roomService.joinParticipant(pcForm2, roomPin, UUID.randomUUID().toString());

        Participant anonymous = roomService.findParticipantBySessionId(participant.getSessionId(), roomPin);
        Participant anonymous2 = roomService.findParticipantBySessionId(participant2.getSessionId(), roomPin);

        assertThat(anonymous).isEqualTo(participant);
        assertThat(anonymous2).isEqualTo(participant2);
        assertThat(anonymous).isNotEqualTo(anonymous2);
    }
}