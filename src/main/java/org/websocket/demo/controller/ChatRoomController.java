package org.websocket.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.websocket.demo.model.ChatRoom;
import org.websocket.demo.repo.ChatRoomRepository;

import java.util.List;


// 웹 소켓 내용 아니고, 채팅 화면 View 구성을 위해 필요한 Controller
@RequiredArgsConstructor
@Controller
@RequestMapping ("/chat")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

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
        return chatRoomRepository.findAllRoom();
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

}
