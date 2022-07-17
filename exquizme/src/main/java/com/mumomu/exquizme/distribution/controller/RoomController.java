package com.mumomu.exquizme.distribution.controller;

import com.mumomu.exquizme.distribution.domain.Participant;
import com.mumomu.exquizme.distribution.domain.Room;
import com.mumomu.exquizme.distribution.service.RoomService;
import com.mumomu.exquizme.distribution.web.model.ParticipateForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"퀴즈방 참여에 이용되는 컨트롤러"})
public class RoomController {
    private final RoomService roomService;

    // 퀴즈방 입장
    // TODO 임시로 defaultValue를 넣어둠 -> 나중에 지워야함
    // TODO Cookie에 관한 수정이 필요함 + WebSocket 사용 시 쿠키가 필요없어질수도..?
    // TODO 다른 방 코드에 들어갔을 경우 존재하는 방인지에 대한 인증 + 기존에 있던 쿠키 삭제 필요
    @GetMapping("/room/{roomId}")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "방의 핀번호(Path)", required = true, dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "nickname", value = "익명사용자 닉네임", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "익명사용자 이름", required = true, dataType = "String", paramType = "query"),
    })
    @Operation(summary = "익명사용자 방 입장", description = "닉네임(nickname)과 이름(name) 입력 후 방에 입장합니다.")
    @ApiResponse(responseCode = "200", description = "방 입장 성공")
    public Participant participateRoom(@PathVariable long roomId, Model model, HttpServletResponse response,
                                       @ModelAttribute @Valid ParticipateForm participateForm, BindingResult bindingResult,
                                       @CookieValue(name = "anonymousCode", defaultValue = "") String anonymousCode) {
        Room targetRoom = roomService.findRoom(roomId);

        // 1. Validation
        // 현재는 방 등록이 없으므로 주석
//        if(targetRoom == null)
//            return null; // TODO 오류 메시지 창 필요

        // 2. Business Logic
        Participant participant =
                Participant.builder().name(participateForm.getName()).nickname(participateForm.getNickname()).build();

        if (anonymousCode.equals("")) {
            Cookie anonymousCookie = Room.setAnonymousCookie();
            response.addCookie(anonymousCookie);
            participant.setUuid(UUID.fromString(anonymousCookie.getValue()).toString());
        } else
            participant.setUuid(anonymousCode);


        // 3. Make Response
        model.addAttribute("roomId", roomId);

        return roomService.join(participant);
    }

    // 퀴즈방 생성
//    @PostMapping("/room")
//    @ResponseBody
//    public RoomDto createRoom(@ModelAttribute UserDto user) {
//
//    }
}