package org.websocket.demo.config.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.websocket.demo.model.ChatMessage;
import org.websocket.demo.repo.ChatRoomRepository;
import org.websocket.demo.service.ChatService;
import org.websocket.demo.service.JwtTokenProvider;

import java.security.Principal;
import java.util.Optional;

// WebSocket 연결 시 요청 header의 jwtToekn 유효성을 검증하는 코드
// 유효하지 않은 Jwt 토큰이 세팅될 경우 webSocket 연결을 하지 않고 예외 처리한다.
@Slf4j
@RequiredArgsConstructor // JwtTokenProvider에 대한 생성자 주입이 일어난다.
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    // webSocket을 통해 들어온 메세지의 전송 요청이 처리 되기 전에 실행된다.
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // STOMP는 HTTP와는 다른 자신만의 규격인 STOMP FRAME 을 가지고 있다.
        // StompHeaderAccessor 는 해당 STOMP FRAME 에서 메세지를 추출하거나, 메세지를 STOMP FRAME 으로 만드는데 사용
        // StompHeaderAccessor 객체는 존재하는 메세지를 wrap 이란 매소드로 감싸서 바로 사용할 수 있다.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 1) webSocket 연결 요청시 헤더의 jwt Token 검증
        if(StompCommand.CONNECT == accessor.getCommand()){
            String jwtToken = accessor.getFirstNativeHeader("token"); // 요청의 Header 속 token 값을 가져옴
            log.info("CONNECT {}", jwtToken);
            jwtTokenProvider.validateToken(jwtToken); //유효성 검증
        }
        //2) 특정 채팅방에 들어가겠다는 요청시
        // + sessionId - roomId 맵핑, 인원수 늘리기, 입장 메세지 발송
        // + 퇴장 요청 시 sessionId - roomId 맵핑 삭제 및 인원수 삭제
        else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {

            System.out.println("메세지 Header에 든 것들: "+message.getHeaders().entrySet());

            // 2-1) 헤더에서 구독 Destination 정보를 얻고, 거기서 roomId를 추출한다.
                // Optional: 값이 Null이여도 NPE 에러를 뱉지 않고, 코드 실행을 계속 이어갈 수 있도록 하는 클래스
                // ofNullable: 값이 Null인 경우에도 Optional 객체가 생성되도록 한다.
                // 여기서는 simpDestination란 Key의 값이 없다면 값을 InvalidHeader로 대체한다.
            String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));

            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다. (나중에 특정 세션이 어떤 채팅방에 들어가 있는지 확인하기 위함.)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            chatRoomRepository.setUserEnterInfo(sessionId, roomId);

            // 2-2) 채팅방에 누가 들어왔음으로, 채팅방 인원 수를 하나 늘린다.
            chatRoomRepository.plusUserCount(roomId);

            // 2-3) 클라이언트 입장 메세지를 채팅방에 발송한다.
                // 사용자 이름 얻기
                // Principal 객체는 자바의 표쥰 시큐리티 기술로 로그인 된 상태라면 계정 정보를 담고 있고, 아니라면 아무것도 담고 있지 않다.
                // 메세지를 Principal 객체로 형변환 -> 이 메세지의 유저 이름과 principal에 저장된 사용자의 이름이 같은지 확인
                // 만약 같지 않으면 map 함수에서 null이 배출될 것이고, 그러면 orElse문의 UnKnownUser가 출력될 것이다.

                // 입장과 퇴장 메세지에서 이름이 유효한지에 대한 유효성 체크를 하는 것은 아니다.
                // 메세지에서 보낸 이가 없이 들어오는 경우, 어떤 특정 오류 때문에 사용자와 매칭이 안되는 경우도, 소켓 통신이 에러 없이 진행되도록 하기 위함이다.

            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");

                // chatService의 sendMessage를 이용해 메세지 보내기

            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(name).build());

            log.info("SUBSCRIBE {}, {}", name, roomId);

        }
        // 3) 특정 방에서 나가겠다는 요청 시
        else if (StompCommand.DISCONNECT == accessor.getCommand()) {
          // 연결을 끊길 원하는 클라이언트의 sessionId를 가지고 매핑되어있던 roomId를 찾는다.
          String sessionId = (String) message.getHeaders().get("simpSessionId");
          String roomId = chatRoomRepository.getUserEnterRoomId(sessionId);

          // 채팅방의 인원 수를 -1한다.
          chatRoomRepository.minusUserCount(roomId);

          // 클라이언트의 퇴장 메세지를 채팅방에 발송한다.
            // 위에서 다룬 로직 -> 이름이 사용자 정보에 들어맞는 이름인지 확인
          String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            // 퇴장 메세지 전송
          chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(name).build());

          // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
          chatRoomRepository.removeUserEnterInfo(sessionId);
          log.info("DISCONNECTED {}, {}", sessionId, roomId);
        }

        return message;
    }

}
