package org.websocket.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.websocket.demo.model.ChatRoom;
import org.websocket.demo.model.LoginInfo;
import org.websocket.demo.repo.ChatRoomRepository;
import org.websocket.demo.service.JwtTokenProvider;

import java.util.List;


// 웹 소켓 내용 아니고, 채팅 화면 View 구성을 위해 필요한 Controller
@RequiredArgsConstructor
@Controller
@RequestMapping ("/chat")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 1) 채팅 리스트 화면 반환
    @GetMapping("/room")
    public String rooms (Model model) {
        return "/chat/room";
    }

    // 2) 채팅방 생성 -> 하나의 Topic을 생성 (RestAPI )
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name){
        return chatRoomRepository.createChatRoom(name);
    }



    // 3) 모든 채팅방 목록 반환 (RestAPI)
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        // 현재 저장고에 존재하는 방을 모두 찾고
        List<ChatRoom> chatRooms = chatRoomRepository.findAllRoom();

        // 방을 하나하나씩 순회하면서, 해당 방의 인원 수를 최신화
        chatRooms.stream().forEach(room -> room.setUserCount(chatRoomRepository.getUserCount(room.getRoomId())));
        return chatRooms;
    }


    // 4) 채팅방의 입장 ->  해당 토픽을 구독한다는 뜻
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail (Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }


    // 5) 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }


    // 6) 로그인한 회원의 id 및 Jwt 토큰 정보를 조회할 수 있도록 하는 RESTFUL API
    @GetMapping("/user")
    @ResponseBody
    public LoginInfo getUserInfo() {
        /* 인증된 사용자 정보인 Principal을 Authentication에서 관리하고,
         * Authentication을 SecurityContext에서 관리하고,
         * SecurityContext는 SecurityContextHolder가 관리한다.
        */

        // 여기서는 인증된 사용자 정보를 꺼내서, 볼 수 있게 하는 로직이다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        return LoginInfo.builder().name(name).token(jwtTokenProvider.generateToken(name)).build();
    }
}
