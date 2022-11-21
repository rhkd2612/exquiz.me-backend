package com.mumomu.exquizme.distribution.service;

import com.mumomu.exquizme.distribution.domain.Answer;
import com.mumomu.exquizme.distribution.domain.Participant;
import com.mumomu.exquizme.distribution.domain.Room;
import com.mumomu.exquizme.distribution.web.dto.AnswerListDto;
import com.mumomu.exquizme.distribution.web.dto.ParticipantDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerService {
    private final RoomService roomService;

    @Transactional
    public AnswerListDto findAnswerListByProblemIdx(String roomPin, int problemIdx){
        AnswerListDto answerListDto = new AnswerListDto();
        Room targetRoom = roomService.findRoomByPin(roomPin);
        String answer = targetRoom.getProblemset().getProblems().get(targetRoom.getCurrentProblemNum()).getAnswer();
        List<Participant> pariticipants = roomService.findParticipantsByRoomPin(roomPin);

        List<Answer> submits = new ArrayList<>();
        for (Participant p : pariticipants) {
            Answer curAnswer = p.getAnswers().stream().filter(
                    a -> a.getProblemIdx() == problemIdx
            ).findFirst().get();

            answerListDto.addParticipant(new ParticipantDto(p),curAnswer.getAnswerText().equals(answer));
        }

        return answerListDto;
    }
}
