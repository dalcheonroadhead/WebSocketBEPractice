# Spring websocket chatting server 만들기 2단계 - 고도화 

> 본 프로젝트는 [스프링 웹소켓 채팅서버 만들기](https://www.daddyprogrammer.org/post/4691/spring-websocket-chatting-server-stomp-server/) 를 실습하며 공부하기 위한 프로젝트 입니다. branch 별로 총 6단계 모두 진행할 예정입니다. 

# 0. STOMP란 무엇인가?

Stomp란 SimpleTextOrientedMessagingProtocol의 약자로, 텍스트 기반 양방향 통신을 효율적으로 하기 위한 통신 규약이다. 

Stomp의 원리는 다음과 같다. Stomp에는 3가지 개념이 존재하는데, Topic, Publisher, Subscriber가 그것이다. 
<img src=".\Images\image2_1.PNG" alt="image2_1" style="zoom:60%;" />

Topic은 우체통이라고 생각하면 된다. publisher는 메세지 발행자이다.  Publisher가 어떤 Topic을 EndPoint로 잡고 메세지를 보내면 해당 우체통으로 메세지가 들어간다. 이때 해당 Topic을 구독하고 있는 Subscriber들에게 해당 메세지가 전부 배달되게 된다. 
우리 웹 소켓에서는  

## 1. 전개도 & 사전 설정 

### 1-1 Server Logic



### 1-2 사전 설정

해당 프로젝트는 따로 프론트 엔드 프로젝트를 두지 않고, SpringBoot 내에서 프론트 엔드 또한 구현할 것이기 때문에 플러그인이 조금더 추가되었다. Sock.js 플러그인 등이 해당 작업을 위한 플러그인이다. 설정 파일 목록은 밑을 참고하라.
```xml
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.2.1'
	id 'io.spring.dependency-management' version '1.1.4'
}

group = 'org.websocket'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '21'
}

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-websocket'
	implementation 'org.springframework.boot:spring-boot-starter-freemarker'
	implementation 'org.springframework.boot:spring-boot-devtools'
	implementation 'org.webjars.bower:bootstrap:4.3.1'
	implementation 'org.webjars.bower:vue:2.5.16'
	implementation 'org.webjars:sockjs-client:1.1.2'
	implementation 'org.webjars.bower:axios:0.17.1'
	implementation 'org.webjars:stomp-websocket:2.3.3-1'
	implementation 'com.google.code.gson:gson:2.8.0'

	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'

}

tasks.named('test') {
	useJUnitPlatform()
}
```

# 2. 코드 분석

### 2_1 계층 확인

<img src=".\Images\image2_2.PNG" alt="image2_2" style="zoom:60%;" align="left" />

### 2_2 WebSockConfig

```java
package org.websocket.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// 이제 STOMP를 이용하여 WebSocket을 구현할 것이기 때문에 따로 WebSockHandler가 필요 없다.
// 왜냐하면 STOMP가 웹 소켓에서 메세지를 다루는 방법에 대한 규약이기 때문이다.

@Configuration
@EnableWebSocketMessageBroker // 웹소켓에서 STOMP를 활성화 하기 위한 어노테이션
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {


    // 메세지 송 수신에 대한 설정을 등록한 매소드이다.
    // 메세지 송신과 발신은 해당 접두어로 구분한다. 
    // 사용자의 메세지 수신주소는 sub/room/{방 번호}로 해당 방 번호로 온 메세지만 수신하도록 설정
    // 메세지 발신의 경우에는 pub/room 으로 보내고 메세지 보낼 방 번호는 responseBody 속 메타데이터로 저장한다.
    @Override
    public void configureMessageBroker (MessageBrokerRegistry config){

        // pub로 시작이 설정된 녀석은 발신하는 녀석이라고 알려주는 것
        config.setApplicationDestinationPrefixes("/pub");

        // sub로 시작하는 녀석은 현재 topic (주소에 같이 줌)에 올라온 메세지를 구독하는 녀석이라고 알려주는 것 
        config.enableSimpleBroker("/sub");

    }


    // Stomp 소켓을 쓸 주소를 특정한다. 
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){

        // stomp websocket으로 연결하는 주소의 endpoint는 /ws-stomp로 설정
        // 따라서 전체 주소는 ws://localhost:8080/ws-stomp
        // 소켓을 해당 주소로 등록 
        registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns("*")
                .withSockJS();
    }

}
```


이제 STOMP의 원리대로 메세지를 발신하고 수신한다. 따라서 우리는 STOMP를 수입하여 쓸 것이므로, 따로 WebSocketHandler를 써서 요청 처리 방안을 정의할 필요가 없다.




### 2_3 Controller

이제 컨트롤러에 대하여 알아보겠다. 컨트롤러는 채팅 자체에 대한 Chat Controller와 채팅방에 대한 ChatRoomController가 있다. 

#### 2_3_1 Chat Controller 

메세지를 송신을 어떻게 처리하는지에 대한 설명을 적은 Controller이다. 자세한 설명은 같이 적어두었다.

```java
// WebSocket으로 들어오는 메세지를 처리하는 컨트롤러
// 발행과 구독은 config에서 설정했던 prefix로 구분
//      -> /pub/chat/message == pub 뒤에 주소로 메세지를 발행하는 요청
//      -> /sub/chat/message == sub 뒤에 주소로부터 발행되는 메세지를 받아오겠다는 요청

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;

    // 메세지가 목적지로 전송되는 경우, 밑의 매소드가 실행된다.
    @MessageMapping("/chat/message")
    public void message (ChatMessage message) {
        // 만약 메세지 타입이 ENTER 이면 입장 메세지를 내용물로 첨부
        if(ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        // 메세지를 채팅방 안에 존재하는 모든 사람들에게 보낸다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
```

##### 2_3_2_a @MessageMapping 어노테이션에 대하여 

@MessageMapping("url")의 경우, 클라이언트로부터 메세지가 해당 목적지로 들어왔을 경우, 어노테이션된 매소드를 실행 시키는 어노테이션이다.  

#### 2_3_2 chat Room Controller

채팅방 생성하고, 해당 채팅방에 입장하거나, 조회하는 Method이다. 

```java
// 웹 소켓 내용 아니고, 채팅 화면 View 구성을 위해 필요한 Controller
@RequiredArgsConstructor
@Controller
@RequestMapping ("/chat")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    // 채팅 리스트 화면 반환
    @GetMapping("/room")
    public String rooms (Model model) {
        return "/chat/room";
    }

    // 모든 채팅방 목록 반환
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        return chatRoomRepository.findAllRoom();
    }

    // 채팅방 생성 -> 하나의 Topic을 생성
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name){
        return chatRoomRepository.createChatRoom(name);
    }

    // 채팅방의 입장 ->  해당 토픽을 구독한다는 뜻
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail (Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }


    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }

}

```

### 3. Model

#### 3_1 ChatRoom

chatMessage는 원래의 DTO 명세와 같아서 따로 기술하지 않았다. 내용이 조금 바뀐 ChatRoom을 이용해서 설명하겠다. 
```java
/* 이전과 달라진 사항
*  1. 이제 stomp의 pub/sub 방식을 이용하므로, 메세지를 받는 이가 누구인가 식별이 가능하다. 
      따라서 chatRoom에서 따로 구독자들의 세션을 들고 있을 필요가 없다.
*  2. 또한 발송의 구현도 stomp의 pub/sub 방식이 사용되므로, 일일히 클라이언트에게 메세지를 발송하는 구현이 필요 없어진다.
      따라서 sendMessage 매소드도 사라져도 된다. 
* */

@Getter
@Setter
public class ChatRoom {

    // 채팅방의 방 번호와 이름
    private String roomId;
    private String name;

    // 밑에 내용은 클래스 매소드로서 클래스 생성부터 존재하는 매소드이고, 하나의 chatRoom을 만들어서 반환한다.
    public static ChatRoom create(String name) {
       ChatRoom chatRoom = new ChatRoom();
       chatRoom.roomId = UUID.randomUUID().toString();
       chatRoom.name = name;

       return chatRoom;
    }
}
```

##### 3_1_a UUID가 무엇인가? 

UUID는 Univarsally Unique IDentifier의 약자로 네크워크 상에 고유한 ID를 만들기 위한 규약이다. 주로 분산 컴퓨터 환경에서 사용된다. 중앙 관리형 네크워크 환경에서는 굳이 쓸 필요가 없는데, 중앙 관리형에서는 종속된 모든 세션에 일련번호를 부여해서 유일성을 보장하면 된다. 중앙 관리 시스템이 모든 상황을 통제 가능하기 때문이다. 하지만 분산 환경일 경우 로컬 id가 다른 네크워크에서 중복되는 경우도 있으므로, 이를 위해서 고유성을 보장할 id가 하나 더 필요 하다. 

### 4. Repository

저번 1강에서의 Service는 지우고 Repository를 새로 만들었다. 아직 DB와의 연결은 안하지만, 다음 강을 위해서 만든 것인 거 같다. DB 역할의 채팅방 정보 저장은 HashMap을 통해 대신한다. 

 ```java
 @Repository
 public class ChatRoomRepository {
 
     // 채팅방 정보는 DB를 안 거치고 간단하게 저장할 것이므로, Map으로 저장해둔다.
     private Map<String, ChatRoom> chatRoomMap;
 
 
     // ChatRoomRepository가 빈 객체로 만들어진 이후에, 딱 한번 실행되는 함수이다.
     @PostConstruct
     private void init() {
         chatRoomMap = new LinkedHashMap<>();
     }
 
     // 채팅방 전체 조회
     public List<ChatRoom> findAllRoom() {
         // 저장고로부터 모든 채팅룸을 받아 List에 넣는다.
         List<ChatRoom> chatRooms = new ArrayList<>(chatRoomMap.values());
         // 채팅방을 생성 순서대로 반환하기 위해 한번 뒤집는다.
         Collections.reverse(chatRooms);
         return chatRooms;
     }
 
     // 방 번호에 맞는 녀석 하나만 찾기
     public  ChatRoom findRoomById(String id) {
         return chatRoomMap.get(id);
     }
 
     // 방 생성
     public ChatRoom createChatRoom(String name){
         // DTO에 적어놨던 채팅방 생성 로직을 써서 채팅방을 만든다.
         ChatRoom chatRoom = ChatRoom.create(name);
         // 해당 채팅방을 채팅방의 방 번호를 index로 저장고에 저장한다.
         chatRoomMap.put(chatRoom.getRoomId(), chatRoom);
         return chatRoom;
     }
 
 }
 ```

### 5. 프론트 엔드 부분

#### 5.0 .ftl 확장자에 대하여 

.ftl은 FreeMarker file을 의미하며, 자바 서블릿을 위한 오픈 소스 HTML이다. 프리마커는 템플릿 객체로 컴파일 되는데, 이 객체는 서블릿에서 제공하는 데이터가 바뀔 떄마다 HTML을 동적으로 생성한다. 
우리는 소켓이 사용되는 모습을 보여주기 위해 .ftl 확장자를 사용하였다. 

#### 5.1 room.ftl

해당 파일에서 할 수 있는 일은 다음과 같다. 

1. 방을 생성한다. 
2. 생성된 방의 리스트를 볼 수 있다. 
3. 방 중 하나로 입장할 수 있다. (여기서 입장하게 되면 roomDetail.ftl로 이동한다.)

```html
<!doctype html>
<html lang="en">
<head>
    <title>Websocket Chat</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <!-- CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/dist/css/bootstrap.min.css">
    <style>
        [v-cloak] {
            display: none;
        }
    </style>
</head>
<body>
<div class="container" id="app" v-cloak>
    <div class="row">
        <div class="col-md-12">
            <h3>채팅방 리스트</h3>
        </div>
    </div>
    <div class="input-group">
        <div class="input-group-prepend">
            <label class="input-group-text">방제목</label>
        </div>
        <input type="text" class="form-control" v-model="room_name" @keyup.enter="createRoom">
        <div class="input-group-append">
            <button class="btn btn-primary" type="button" @click="createRoom">채팅방 개설</button>
        </div>
    </div>
    <ul class="list-group">
        <li class="list-group-item list-group-item-action" v-for="item in chatrooms" v-bind:key="item.roomId" v-on:click="enterRoom(item.roomId)">
            {{item.name}}
        </li>
    </ul>
</div>
<!-- JavaScript -->
<script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
<script src="/webjars/axios/0.17.1/dist/axios.min.js"></script>
<script src="/webjars/bootstrap/4.3.1/dist/js/bootstrap.min.js"></script>
<script src="/webjars/sockjs-client/1.1.2/sockjs.min.js"></script>
<script>
    var vm = new Vue({
        el: '#app',
        data: {
            room_name : '',
            chatrooms: [
            ]
        },
        created() {
            this.findAllRoom();
        },
        methods: {
            findAllRoom: function() {
                axios.get('/chat/rooms').then(response => { this.chatrooms = response.data; });
            },
            createRoom: function() {
                if("" === this.room_name) {
                    alert("방 제목을 입력해 주십시요.");
                    return;
                } else {
                    var params = new URLSearchParams();
                    params.append("name",this.room_name);
                    axios.post('/chat/room', params)
                        .then(
                            response => {
                                alert(response.data.name+"방 개설에 성공하였습니다.")
                                this.room_name = '';
                                this.findAllRoom();
                            }
                        )
                        .catch( response => { alert("채팅방 개설에 실패하였습니다."); } );
                }
            },
            enterRoom: function(roomId) {
                var sender = prompt('대화명을 입력해 주세요.');
                localStorage.setItem('wschat.sender',sender);
                localStorage.setItem('wschat.roomId',roomId);
                location.href="/chat/room/enter/"+roomId;
            }
        }
    });
</script>
</body>
</html>
```

#### 5.2 roomdetail.ftl

```html
<!doctype html>
<html lang="en">
<head>
    <title>Websocket ChatRoom</title>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/dist/css/bootstrap.min.css">
    <style>
        [v-cloak] {
            display: none;
        }
    </style>
</head>
<body>
<div class="container" id="app" v-cloak>
    <div>
        <h2>{{room.name}}</h2>
    </div>
    <div class="input-group">
        <div class="input-group-prepend">
            <label class="input-group-text">내용</label>
        </div>
        <input type="text" class="form-control" v-model="message" @keyup.enter="sendMessage">
        <div class="input-group-append">
            <button class="btn btn-primary" type="button" @click="sendMessage">보내기</button>
        </div>
    </div>
    <ul class="list-group">
        <li class="list-group-item" v-for="message in messages">
            {{message.sender}} - {{message.message}}</a>
        </li>
    </ul>
    <div></div>
</div>
<!-- JavaScript -->
<script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
<script src="/webjars/axios/0.17.1/dist/axios.min.js"></script>
<script src="/webjars/bootstrap/4.3.1/dist/js/bootstrap.min.js"></script>
<script src="/webjars/sockjs-client/1.1.2/sockjs.min.js"></script>
<script src="/webjars/stomp-websocket/2.3.3-1/stomp.min.js"></script>
<script>
    // websocket & stomp initialize
    var sock = new SockJS("/ws-stomp");
    var ws = Stomp.over(sock);
    // vue.js
    var vm = new Vue({
        el: '#app',
        data: {
            roomId: '',
            room: {},
            sender: '',
            message: '',
            messages: []
        },
        created() {
            this.roomId = localStorage.getItem('wschat.roomId');
            this.sender = localStorage.getItem('wschat.sender');
            this.findRoom();
        },
        methods: {
            findRoom: function() {
                axios.get('/chat/room/'+this.roomId).then(response => { this.room = response.data; });
            },
            sendMessage: function() {
                ws.send("/pub/chat/message", {}, JSON.stringify({type:'TALK', roomId:this.roomId, sender:this.sender, message:this.message}));
                this.message = '';
            },
            recvMessage: function(recv) {
                this.messages.unshift({"type":recv.type,"sender":recv.type=='ENTER'?'[알림]':recv.sender,"message":recv.message})
            }
        }
    });
    // pub/sub event
    ws.connect({}, function(frame) {
        ws.subscribe("/sub/chat/room/"+vm.$data.roomId, function(message) {
            var recv = JSON.parse(message.body);
            vm.recvMessage(recv);
        });
        ws.send("/pub/chat/message", {}, JSON.stringify({type:'ENTER', roomId:vm.$data.roomId, sender:vm.$data.sender}));
    }, function(error) {
        alert("error "+error);
    });
</script>
</body>
</html>
```


# 3. 시연 


### (1) SpringBoot 실행 후 room.ftl 화면으로 이동

일단 양방향 통신을 보여주기 위해 화면을 두 개 띄우겠다. 
![2_3](.\Images\2_3.PNG)

### (2) 왼쪽에서 채팅방 만들기, 오른쪽에서 확인


###  ![2_4](C:\Users\SSAFY\IdeaProjects\WebSocketDemo\Images\2_4.PNG)


### ![2_5](.\Images\2_5.PNG)(3) 양쪽에서 채팅방에 들어가기

![2_6](.\Images\2_6.PNG)

이름은 그냥 임의로 넣었다.

![2_7](.\Images\2_7.PNG)


### (4) 오른쪽에서 채팅 쳐보기, 왼쪽에서도 뜨는지 확인 

![2_8](.\Images\2_8.PNG)


다음 장으로 가볼껴?


