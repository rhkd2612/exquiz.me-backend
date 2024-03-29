package com.mumomu.exquizme;

import com.mumomu.exquizme.common.dto.GoogleLoginDto;
import com.mumomu.exquizme.common.entity.OAuth2Account;
import com.mumomu.exquizme.common.entity.Role;
import com.mumomu.exquizme.common.repository.OAuth2AccountRepository;
import com.mumomu.exquizme.common.service.OAuth2AccountService;
import com.mumomu.exquizme.distribution.domain.Participant;
import com.mumomu.exquizme.distribution.domain.Room;
import com.mumomu.exquizme.distribution.repository.ParticipantRepository;
import com.mumomu.exquizme.distribution.repository.RoomRepository;
import com.mumomu.exquizme.distribution.service.RoomService;
import com.mumomu.exquizme.production.domain.Host;
import com.mumomu.exquizme.production.domain.Problem;
import com.mumomu.exquizme.production.domain.ProblemOption;
import com.mumomu.exquizme.production.domain.Problemset;
import com.mumomu.exquizme.production.repository.HostRepository;
import com.mumomu.exquizme.production.repository.ProblemsetRepository;
import com.mumomu.exquizme.production.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

//
@Component
@Slf4j
@RequiredArgsConstructor
public class TestDataInit {
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final ParticipantRepository participantRepository;
    private final HostRepository hostRepository;
    private final ProblemService problemService;
    private final OAuth2AccountRepository oAuth2AccountRepository;
    private final ProblemsetRepository problemsetRepository;
    private final PasswordEncoder passwordEncoder;

    // 테스트용 데이터 추가
    @PostConstruct
    @Transactional
    public void init() throws Exception{
//        oAuth2AccountRepository.save(OAuth2Account.builder()
//                .email("rhkd2612@gmail.com")
//                .nickname("이상빈")
//                .username("rhkd2612@gmail.com=google")
//                .activated(true)
//                .password(passwordEncoder.encode("NO_PASSWORD"))
//                .role(Role.ADMIN)
//                .build()
//        );
//
//        oAuth2AccountRepository.save(OAuth2Account.builder()
//                .email("netcopjr@gmail.com")
//                .nickname("김민겸")
//                .username("netcopjr@gmail.com=google")
//                .activated(true)
//                .password(passwordEncoder.encode("NO_PASSWORD"))
//                .role(Role.ADMIN)
//                .build()
//        );
//
//        oAuth2AccountRepository.save(OAuth2Account.builder()
//                .email("wnsgus821@gmail.com")
//                .nickname("임준현")
//                .username("wnsgus821@gmail.com=google")
//                .activated(true)
//                .password(passwordEncoder.encode("NO_PASSWORD"))
//                .role(Role.ADMIN)
//                .build()
//        );

        Room room1 = roomRepository.save(Room.ByBasicBuilder().pin("100000").maxParticipantCount(5).build());
        Room room2 = roomRepository.save(Room.ByBasicBuilder().pin("200000").maxParticipantCount(5).build());

        Participant p1 = participantRepository.save(Participant.ByBasicBuilder().name("공길동").nickname("공길동무새").sessionId("0aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p2 = participantRepository.save(Participant.ByBasicBuilder().name("농길동").nickname("농길동무새").sessionId("1aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p3 = participantRepository.save(Participant.ByBasicBuilder().name("동길동").nickname("동길동무새").sessionId("2aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p4 = participantRepository.save(Participant.ByBasicBuilder().name("롱길동").nickname("롱길동무새").sessionId("3aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p5 = participantRepository.save(Participant.ByBasicBuilder().name("몽길동").nickname("몽길동무새").sessionId("4aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p6 = participantRepository.save(Participant.ByBasicBuilder().name("봉길동").nickname("봉길동무새").sessionId("5aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p7 = participantRepository.save(Participant.ByBasicBuilder().name("송길동").nickname("송길동무새").sessionId("6aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p8 = participantRepository.save(Participant.ByBasicBuilder().name("옹길동").nickname("옹길동무새").sessionId("7aed126c-9b08-4581-b3d3-9630b45c3989").room(room1).build());
        Participant p9 = participantRepository.save(Participant.ByBasicBuilder().name("곽두팔").nickname("곽두팔무새").sessionId("9aed126c-9b08-4581-b3d3-9630b45c3989").room(room2).build());

        roomService.addParticipant(room1, p1);
        roomService.addParticipant(room1, p2);
        roomService.addParticipant(room1, p3);
        roomService.addParticipant(room1, p4);
        roomService.addParticipant(room1, p5);
        roomService.addParticipant(room1, p6);
        roomService.addParticipant(room1, p7);
        roomService.addParticipant(room1, p8);
        roomService.addParticipant(room2, p9);

        Host host = hostRepository.save(Host.builder().name("호스트").nickname("Mumomu").build());
        Problemset problemset = problemService.makeProblemset(host.getId(),"tempTitle","tempDescription","Goodbye Command", 1, 1, 1);
        Problem problem = problemService.makeProblem(problemset.getId(), "MultipleChoiceProblem",0,"tempTitle","tempDescription",30,100,null,"1");

        ProblemOption problemOption = problemService.makeProblemOption(problem.getId(),0,"im first",null);
        ProblemOption problemOption2 = problemService.makeProblemOption(problem.getId(),1,"im second",null);
        ProblemOption problemOption3 = problemService.makeProblemOption(problem.getId(),2,"im third",null);
        ProblemOption problemOption4 = problemService.makeProblemOption(problem.getId(),3,"im fourth",null);

        Problem problem2 = problemService.makeProblem(problemset.getId(), "MultipleChoiceProblem",1,"tempTitle2","tempDescription2",30,100,null,"1");

        ProblemOption problemOption11 = problemService.makeProblemOption(problem2.getId(),0,"im first2",null);
        ProblemOption problemOption22 = problemService.makeProblemOption(problem2.getId(),1,"im second2",null);
        ProblemOption problemOption33 = problemService.makeProblemOption(problem2.getId(),2,"im third2",null);
        ProblemOption problemOption44 = problemService.makeProblemOption(problem2.getId(),3,"im fourth2",null);

        Problem problem3 = problemService.makeProblem(problemset.getId(), "OXProblem",2,"tempTitle3","tempDescription3",30,200,null,"1");

        ProblemOption problemOption111 = problemService.makeProblemOption(problem3.getId(),0,"im first2",null);
        ProblemOption problemOption222 = problemService.makeProblemOption(problem3.getId(),1,"im second2",null);

        // 제출 파트 빌더 미수정으로 작동되지 않음
//        problemset.getProblems().add(problem);
//        problemset.getProblems().add(problem2);
//        problemset.getProblems().add(problem3);
//
//        room1.setProblemset(problemset);
//        room2.setProblemset(problemset);

        log.info("Test Data Init Complete");
    }
}
