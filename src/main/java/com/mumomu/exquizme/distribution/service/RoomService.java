package com.mumomu.exquizme.distribution.service;

import com.mumomu.exquizme.distribution.domain.Participant;
import com.mumomu.exquizme.distribution.domain.Room;
import com.mumomu.exquizme.distribution.domain.RoomState;
import com.mumomu.exquizme.distribution.exception.*;
import com.mumomu.exquizme.distribution.repository.ParticipantRepository;
import com.mumomu.exquizme.distribution.repository.RoomRepository;
import com.mumomu.exquizme.distribution.web.dto.ParticipantDto;
import com.mumomu.exquizme.distribution.web.model.ParticipantCreateForm;
import com.mumomu.exquizme.common.formatter.SimpleDateFormatter;
import com.mumomu.exquizme.distribution.web.model.RoomCreateForm;
import com.mumomu.exquizme.production.domain.Problem;
import com.mumomu.exquizme.production.domain.Problemset;
import com.mumomu.exquizme.production.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final ProblemService problemService;

    // application-XXX.yml 파일 local-100000 prod-100000, test-100000
    @Value("${min.pin.value}")
    private String MIN_PIN_VALUE;

    // application-XXX.yml 파일 local-100500 prod-999999, test-100005
    @Value("${max.pin.value}")
    private String MAX_PIN_VALUE;

    @Transactional
    public Participant joinParticipant(ParticipantCreateForm participateForm, String roomPin, String sessionId) throws IllegalAccessException {
        Optional<Room> optRoom = roomRepository.findRoomByPin(roomPin);

        if (optRoom.isEmpty())
            throw new InvalidRoomAccessException("존재하지 않는 방입니다. Pin : " + roomPin);
        if(optRoom.get().getCurrentState() == RoomState.FINISH)
            throw new InvalidRoomAccessException("종료된 방입니다. Pin : " + roomPin);

        Room targetRoom = optRoom.get();
        checkRoomState(targetRoom);

        Participant participant =
                Participant.ByBasicBuilder()
                        .name(participateForm.getName())
                        .nickname(participateForm.getNickname())
                        .room(targetRoom)
                        .sessionId(sessionId)
                        .build();

        participant.setImageNumber(participateForm.getImageNumber());
        participant.setColorNumber(participateForm.getColorNumber());

        Optional<Participant> findOptParticipant = participantRepository.findBySessionIdAndRoomPin(participant.getSessionId(), roomPin);

        for (Participant p : targetRoom.getParticipants()) {
            if (!p.getSessionId().equals(participant.getSessionId())) {
                if (p.getNickname().equals(participateForm.getNickname()))
                    throw new RoomNotReachableException("이미 존재하는 닉네임입니다. 재설정 해주세요.");
                else if (p.getName().equals(participateForm.getName()))
                    throw new RoomNotReachableException("이미 존재하는 이름입니다. 재설정 해주세요.");
            }
        }

        // TODO 닉네임 구분하여 입장하도록 설정
        if (findOptParticipant.isEmpty() || !findOptParticipant.get().getRoom().getPin().equals(roomPin)) {
            participantRepository.save(participant);
            addParticipant(participant.getRoom(), participant);
        } else {
            Participant targetParticipant = findOptParticipant.get();
            targetParticipant.setName(participateForm.getName());
            targetParticipant.setNickname(participateForm.getNickname());
            targetParticipant.setImageNumber(participateForm.getImageNumber());
            targetParticipant.setColorNumber(participateForm.getColorNumber());
        }

        participantRepository.flush();

        return participant;
    }

    @Transactional
    public Room newRoom(Long problemsetId, int maxParticipantCount) {
        Problemset roomProblemset = problemService.getProblemsetById(problemsetId);
        return newRoomLogic(roomProblemset, maxParticipantCount, "임시 방 생성");
    }

    @Transactional
    public Room newRoom(Problemset roomProblemset, RoomCreateForm roomCreateForm) {
        return newRoomLogic(roomProblemset, roomCreateForm.getMaxParticipantCount(), roomCreateForm.getRoomName());
    }

    private String getRandomPin() {
        int max = Integer.valueOf(MAX_PIN_VALUE);
        int min = Integer.valueOf(MIN_PIN_VALUE);

        return Integer.toString((int) (Math.random() * (max - min)) + min);
    }

    @Transactional(readOnly = true)
    public Participant findParticipantBySessionId(String sessionId, String roomPin) throws SessionNotExistException {
        Optional<Participant> optParticipant = participantRepository.findBySessionIdAndRoomPin(sessionId, roomPin);

        if (optParticipant.isEmpty())
            throw new SessionNotExistException("기존 입장 정보가 존재하지 않습니다.");

        Participant participant = optParticipant.get();

        if (!participant.getRoom().getPin().equals(roomPin)) {
            // 방이 다르다면 참여자 정보 제거
            deleteParticipantUserDataBySessionId(sessionId);
            throw new SessionNotExistException("기존에 입장한 방과 다른 방입니다.");
        }

        return optParticipant.get();
    }

    @Transactional(readOnly = true)
    public Room findRoomById(Long roomId) {
        Optional<Room> optRoom = roomRepository.findRoomById(roomId);

        if (optRoom.isEmpty())
            throw new InvalidRoomAccessException("존재하지 않는 방입니다.");

        return roomRepository.findRoomById(roomId).get();
    }

    @Transactional(readOnly = true)
    public Room findRoomByPin(String roomPin) {
        Optional<Room> optRoom = roomRepository.findRoomByPin(roomPin);

        if (optRoom.isEmpty())
            throw new InvalidRoomAccessException("존재하지 않는 방입니다. Pin : " + roomPin);
        if(optRoom.get().getCurrentState() == RoomState.FINISH)
            throw new InvalidRoomAccessException("종료된 방입니다. Pin : " + roomPin);

        return optRoom.get();
    }

    @Transactional
    public Room closeRoomByPin(String roomPin) {
        Optional<Room> optRoom = roomRepository.findRoomByPin(roomPin);

        if (optRoom.isEmpty())
            throw new InvalidRoomAccessException("존재하지 않는 방입니다. Pin : " + roomPin);
        if(optRoom.get().getCurrentState() == RoomState.FINISH)
            throw new InvalidRoomAccessException("종료된 방입니다. Pin : " + roomPin);

        Room targetRoom = optRoom.get();

        targetRoom.setEndDate(new Date());
        targetRoom.setPin(SimpleDateFormatter.formatDateToString(targetRoom.getEndDate()) + targetRoom.getPin());
        targetRoom.setCurrentState(RoomState.FINISH);

        // 방이 닫힐 시 사용자 정보 DB에서 제거
        participantRepository.deleteAll(targetRoom.getParticipants());

        return targetRoom;
    }

    @Transactional(readOnly = true)
    public List<Participant> findParticipantsByRoomPin(String roomPin) {
        Optional<Room> optRoom = roomRepository.findRoomByPin(roomPin);

        if (optRoom.isEmpty())
            throw new InvalidRoomAccessException("존재하지 않는 방입니다. Pin : " + roomPin);
        if(optRoom.get().getCurrentState() == RoomState.FINISH)
            throw new InvalidRoomAccessException("종료된 방입니다. Pin : " + roomPin);

        return optRoom.get().getParticipants();
    }

    @Transactional(readOnly = true)
    public List<ParticipantDto> findParticipantDtosByRoomPin(String roomPin) {
        Optional<Room> optRoom = roomRepository.findRoomByPin(roomPin);

        if (optRoom.isEmpty())
            throw new InvalidRoomAccessException("존재하지 않는 방입니다. Pin : " + roomPin);
        if(optRoom.get().getCurrentState() == RoomState.FINISH)
            throw new InvalidRoomAccessException("종료된 방입니다. Pin : " + roomPin);

        return optRoom.get().getParticipants().stream().map(ParticipantDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void deleteParticipantUserDataBySessionId(String sessionId) {
//        Optional<Participant> targetParticipant = participantRepository.findBySessionIdAndRoomPin(sessionId, roomPin);
//
//        if (targetParticipant.isEmpty())
//            throw new InvalidParticipantAccessException("존재하지 않는 참여자입니다.");
//
//        // 이건 굳이 데이터 남길 필요 없을듯..?
//        participantRepository.delete(targetParticipant.get());
    }

    private Room newRoomLogic(Problemset roomProblemset, int maxParticipantCount, String roomName) {
        String randomPin;
        Optional<Room> targetRoom;
        int retryCount = 10; // 최대 try 횟수, 무한 루프 방지

        do {
            randomPin = getRandomPin(); //MIN_PIN_RANGE ~ MAX_PIN_RANGE 랜덤 숫자 생성
            targetRoom = roomRepository.findRoomByPin(randomPin);
            retryCount--;
        }
        while (!targetRoom.isEmpty() && retryCount > 0);

        if (retryCount == 0) {
            throw new CreateRandomPinFailureException("다시 시도 해주세요.");
        }

        Room room = Room.ByBasicBuilder().pin(randomPin).problemset(roomProblemset).maxParticipantCount(maxParticipantCount).roomName(roomName).build();
        log.info("random Pin is {}", randomPin);

        Room savedRoom = roomRepository.save(room);
        roomRepository.flush();

        return savedRoom;
    }

    @Transactional(readOnly = true)
    public void checkRoomState(String roomPin) throws IllegalAccessException {
        Room targetRoom = findRoomByPin(roomPin);
        if (targetRoom.getMaxParticipantCount() <= targetRoom.getParticipants().size())
            throw new IllegalAccessException("방 입장 최대 인원을 초과했습니다.");
    }

    @Transactional(readOnly = false)
    public boolean checkRoomState(Room targetRoom) {
        if (targetRoom.getMaxParticipantCount() <= targetRoom.getParticipants().size())
            return false;
        return true;
    }

    @Transactional
    public void addParticipant(Room room, Participant participant) {
        room.addParticipant(participant);

        roomRepository.flush();
        participantRepository.flush();
    }
}
