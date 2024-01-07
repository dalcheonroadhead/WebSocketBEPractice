# Spring websocket chatting server 만들기 1단계 - 기본기 

> 본 프로젝트는 [스프링 웹소켓 채팅서버 만들기](https://www.daddyprogrammer.org/post/4077/spring-websocket-chatting/) 를 실습하며 공부하기 위한 프로젝트 입니다. branch 별로 총 6단계 모두 진행할 예정입니다. 

## 1. 전개도 

### 1-1 Server Logic

해당 블로그의 Basic WebSocket의 전개도 이다. 
아주 간단하게 처리했기 때문에 아직 DB와 연결하지 않고, 서비스와 컨트롤러, 모델, 웹소켓 설정 디렉토리만 필요했다. 

<img src=".\Images\React Logic-21.jpg" alt="React Logic-21" style="zoom: 50%;" />

### 1-2 Server와 Client 전체 Logic

<img src=".\Images\React Logic-22.jpg" alt="React Logic-22" style="zoom:50%;" />

먼저 내가 헷갈렸던 부분은 Rest API와 소켓이 다른 장소에서 mapping 된다는 점이다. 나는 웹소켓 또한 클라이언트와 통신하기 위해서는 컨트롤러에서 정의를 하여야 하는 줄 알았다. 하지만, 웹 소켓의 경우  Web Socket Config 에서 주소를 따로 특정하여 mapping 하는 듯 하다.  따라서 해당 예제에서는 우리가 소위 알던 컨트롤러 같은 녀석이 둘이다. 

# 2. 코드 분석

### 2-1 Sever 디렉토리 계층 구조 

<img src=".\Images\directoryhieracy.PNG" alt="directoryhieracy" align="left" />

다음은 내가 만든 프로젝트의 계층 구조이고, 해당 사진의 맨 위에서부터 코드 분석을 시작하겠다. 

### (1) WebSockChatHandler

웹소켓이 해야할 일에 대한 정의를 한 클래스이다. 잘 보면, 해당 Handler로 chatService의 기능들을 사용해서 일을 처리하고 있다. 해당 Handler는 WebConfig에 객체로 포함되는 녀석이다. WebConfig 가 컨트롤러 같은 성격을 가졌음을 한번 더 인지했다. 

```java
@Slf4j	// 로그 찍기 위한 용도 
@RequiredArgsConstructor // 밑에서 설명 
@Component
public class WebSockChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Override
    protected void handleTextMessage (WebSocketSession session, TextMessage message ) throws Exception{

        // payLoad는 데이터 자체를 의미하는 말이다. 택배 박스가 왔을 때, 
        // 내가 시킨 물건이 payLoad이고 뽁뽁이, 테이프 등은 payLoad가 아니다.
        String payLoad = message.getPayload();
        log.info("payload {}", payLoad);


        // 1. 웹소켓 클라이언트로부터 메세지 내용을 전달받아 채팅 메세지 객체로 변환
        ChatMessage chatMessage = objectMapper.readValue(payLoad, ChatMessage.class);

        // 2. 전달 받은 메세지에 담긴 채팅방 ID로 내 메세지를 전송해야 하는 채팅방 객체를 불러온다.
        ChatRoom room = chatService.findRoomById(chatMessage.getRoomId());

        // 3. 해당 채팅방에 입장해있는 모든 클라이언트들(Websocket session)에게 타입에 따른 메세지 발송
        room.handleActions(session, chatMessage, chatService);



    }
}
```

#### ① @RequestArgsConstructor란?

기존의 Spring에서는 @AutoWired 와 @Component 조합을 활용하여,  의존성을 외부에서 주입을 하였다.  의존성 주입이란, 클래스 안에 다른 클래스, 그 클래스 안에 다른 클래스가 들어가는 형태의 결합도가 높은 로직을 지양하고자 나온 개념으로, 해당 의존성을 외부 관제탑에서 주관해서 주입하겠다는 뜻이다. 따라서 클래스와 객체들은 자신이 무슨 클래스 안에 존재하는지 알지 못하고, 모든 의존성 주입은 Spring의 설정파일에 의해 결정된다. 원래는 root-context안에 내가 외부에서 주입하기 위해 사용할 클래스를 일일히 적어서 root-context에게 알려줘야 했지만, 어노테이션이 나온 이유로 이런 과정들이 간소화 되었다. 

1. @Component라고 적힌 클래스는 Spring에서 자동으로 빈 객체로 등록 시킨다. 

2. @AutoWired라고 적힌 멤버 변수, 생성자, setter 등은 등록된 빈 객체 중에 type이 맞는 녀석이 있으면, 자동으로 해당 멤버 변수, 생성자, setter는 그 빈객체로 초기화 된다. 

 2번에서 알 수 있듯이 AutoWired로 빈 객체와 연결하려면, 멤버 변수, 생성자, setter 등에 @AutoWried를 적어줘야 한다. (만약 생성자가 하나 뿐이라면, @AutoWired는 생략해도 된다.) 

  이러한 작업을 한번 더 간소화 하기 위해 사용하는 어노테이션이 바로 @RequestArgsConstructor이다! 해당 어노테이션을 클래스에 써주면 생성자 코드가 없어도 해당 클래스에 관해 생성자 주입이 자동으로 이루어진다. 이 말은 모든 멤버변수가 해당 type의 bean 객체가 있다면 자동으로 연결된다는 소리이다.  생략된 코드를 다시 써보자면, 이렇게 될 것이다. 

```java
public WebSockChatHandler ( ObjectMapper objectMapper, ChatService chatService){
    this.objectMapper = objectMapper;
    this.chatService = chatService; 
}
```

#### ② ObjectMapper란?

JSON 형식의 객체를 직렬화 하거나 역직렬화 할 때, 사용하는 기술이다. 먼저 앞에 나온 **JSON**과 **직렬화**, **역직렬화**에 대해 알아보자. 

##### ② - ⓐ 용어정리

JSON: JavaSript Object Notation의 약자로, "키 : 값" 쌍으로 이루어진, 데이터 전달용 Text형 객체 포맷이다. 
직렬화: 데이터를 전송하거나 저장할 때, 데이터의 형태는 바이트 형 문자열이어야 해서, Object -> String 문자열로 바꾸는 작업
역직렬화: 수신 받은 데이터는 String 문자열 형태일텐데, 이걸 다시 쓸 수 있는 객체로 바꾸는 작업이다. (String -> Object)

objectMapper는 말 그대로 객체를 String으로 혹은 String을 객체로 mapping 해주는 녀석이라는 의미이다.
그럼 컨트롤러는 항상 객체를 String으로 받을 텐데 어떻게 우리는 ObjectMapper 없이도 객체 형태로 바로 쓸 수 있을까? SpringBoot의 경우, spring-boot-starter-web이라는 의존성을 수입해서 사용하고 있는데, 이 안에 JSON 라이브러리가 포함되어 있어서 기본적으로 Object <-> String을 자동으로 변환해준다. (JSON 라이브러리는 JAVA에서 JSON을 고수준으로 처리해주는 처리기 이다.) 하지만 현재 우리가 쓰는 소켓의 경우, SpringBoot가 컨트롤러 처럼 자동으로 변환 처리를 해주지 않기 때문에 ObjectMapper라는 객체를 사용하여 우리가 직접 처리 해야한다. (위에서 봤듯이 소켓은 컨트롤러와 다른 새로운 창구를 여는 녀석이었다.)

이제 objectMapper를 어떻게 써야하는지 알아보자. 

② - ⓑ 사용법 
먼저 사전 설정이 있어야 한다. ObjectMapper도 String 문자열을 다시 객체로 바꿀 수 있는 설명서가 있어야 할 것이다. (설명서 없이 조립을 어떻게 하겠는가!) 여기서 설명서는 우리가 역 직렬화를 통해 만드려는 객체의 클래스이다. 이때 클래스 안의 필수로 존재해야하는 구성요소는 생성자와 Getter이다. 

> ObjectMapper는 클래스의 생성자를 통해서 직렬화된 문자열을 객체로 바꾸고, Getter를 통해서 객체를 문자열로 바꾼다! 

다음은 예시이다. 

```java

@Getter  // Object -> String 문자열로 바꿀 때 필요! --> 여기서는 lombok으로 바로 처리했다.
class Car {
 private String name;
 private String color;
 
    
// String 문자열 => Object로 바꿀 때 필요!    
 public Car(String name, String color) {
  this.name = name;
  this.color = color;
 }
 
 public Car() {
  this.name = null;
  this.color = null;
 } 
}
```

**직렬화 시** : mapper.writeValueAsString(car)

위에 존재하는 Car라는 클래스의 객체를 문자열로 바꾸는 작업이다. 

```java
ObjectMapper mapper = new ObjectMapper();
Car car = new Car("K5", "gray");

String text = mapper.WriteValueAsString(car); //{"name":"K5","color":"gray"}
```

**역직렬화 시: mapper.readValue("문자열", 클래스이름.class) **

class.class는 해당 클래스의 메타정보가 담긴 Reflection API이다. 우리는 이 정보를 사용하여 받은 문자열을 다시 객체로 변환한다. 

```java 
Car carObject = mapper.readValue(text, Car.class); //Car{name='k5',color='gary'}
```

##### 주의점! 

위에서 ObjectMapper를 사용하기 위해서는 클래스에 꼭 들어가야 하는 필수 요소가 있었다. 이때 **getter 이외에 get이라는 이름으로 시작하는 매소드가 없어야 한다!** 왜냐하면, objectMapper는 get으로 시작하는 매소드를 getter라고 인식하고, 그 get 뒤에 이루어진 이름과 같은 이름의 문자열 내 문자를 mapping 해버리기 때문이다.  

 

### (2) WebSockConfig

아까 최초로 만든 handler는 웹 소켓이 어떻게 일 해야하는지 처리 방법을 알려주는 java class 파일이라고 생각하면 된다. 이번에 알아볼 WebSockConfig는 webSocket 전체에 대한 설정 파일이다. 여기서는

1. handler 객체를 만들어서 웹소켓에 등록한다. 이제 웹소켓은 등록된 handler를 이용해 클라이언트의 요구에 따른 일 처리를 진행한다.
2. 클라이언트가 웹 소켓을 사용하기 위해서 어디로 접속해야 하는지 주소를 알려준다. (이때 보안 처리를 막기위해 AllowedOrigin 설정을 해야한다.)

```java
// BasicHandler를 이용하여 WebSocket을 활성화 하기 위한 Config 파일

@RequiredArgsConstructor
@Configuration
@EnableWebSocket // 웹 소켓 활성화 하는 어노테이션
public class WebSockConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    // 웹소켓에 Handler를 등록하는 매소드 
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 웹 소켓에 접속하기 위한 endpoint를 "ws/chat"으로 설정
        // 다른 서버에서도 접속 가능하도록 CORS : setAllowedOrigins("*")을 설정
        registry.addHandler(webSocketHandler, "ws/chat").setAllowedOrigins("*");
    }
}
```

### ① @Configuration 이란? 

@Configuration이란 어노테이션이 달린 클래스 또한 Bean Factory에 자동으로 등록 된다. @Configuration이란 어노테이션 안에도 @Conponent라는 태그가 포함되어 있기 때문이다. 그냥 @Component와 다른 점은, @Configuration 어노테이션의 경우 그 안에 들어있는 Bean 객체들도 싱글톤으로 사용되도록 보장한다. (하나의 클래스 당 하나의 객체만 존재하고, 필요할 때마다 그것을 재활용 하도록) 
예를 들어 설명해보겠다. 

![image1](.\Images\image1.PNG)

예를 들어 이런 식으로 클래스들이 만들어져 있고 연관관계가 있다고 해보자. 
AppConfig의 경우 Mybean 객체와 MyBeanConsumer 객체를 둘 다 사용하고, MyBeanConsumer 객체는 Mybean 객체를 사용한다. 만약 AppConfig가 @Configuration 태그를 사용하고 있다면, 해당 클래스가 멤버 객체로 사용하고 있는 멤버변수들은 무조건 싱글톤을 지켜야 한다. 따라서 AppConfig를 실행할 경우 출력될 문구는 다음과 같다. 

```
Bean 객체가 생성되었습니다. 
BeanConsumer 객체가 생성되었습니다. 
생성한 Bean 객체의 해쉬코드는 164777052 입니다. 
```

위의 예시에서는 @Configuration이 사용되었기 때문에, MyBeanConsumer에서도 Bean 객체를 하나 더 만들어서 사용할 수도 있었지만, 이미 AppConfig 객체를 만들 때, 만들었던 MyBean 객체를 다시 재활용한다. 따라서 MyBean, MyBeanConsumer 객체가 하나씩만 존재한다. 만약 AppConfig에 @Configuration 어노테이션을 제거하고 그냥 @Component를 사용하면,

``` 
Bean 객체가 생성되었습니다.  
Bean 객체가 생성되었습니다.
BeanConsumer 객체가 생성되었습니다.
생성한 Bean 객체의 해쉬코드는 294238503 입니다.
```

이제 AppConfig의 멤버 변수들에 대한 싱글톤 원칙을 지킬 필요가 없어졌다. AppConfig를 하면서 MyBean 객체와 MyBeanConsumer 객체가 만들어졌지만, MyBean 객체를 생성하면서 MyBean 객체가 한번 더 생성된다.  따라서 둘째 줄의 MyBean 객체는 MyBeanConsumer 객체 안의 MyBean 객체이다. 4번째 줄의 MyBean 객체의 해쉬코드는 2번째 줄에 생성된 MyBeanConsumer 객체 안의 MyBean의 해쉬코드이다. 

#### ② WebSocketHandlerRegistry 분석 

```java
  // 웹소켓에 Handler를 등록하는 매소드 
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(webSocketHandler, "ws/chat").setAllowedOrigins("*");
    }
```

멤버 변수로 형성된 Handler 객체를 웹소켓 자체에 장착하는 매소드이다. addHandler를 이용하여, 장착할 Handler와 , 해당 handler를 사용하기 위한 주소를 명시해주면 된다. 여기서는 CORS 정책에 발 묶이지 않고, 어느 브라우저에서나 사용할 수 있도록 .setAllowedOrigins("*")을 썼다. 

### (3) ChatController

```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatRoom createRoom(@RequestParam String name) {
        return chatService.createRoom(name);
    }

    @GetMapping
    public List<ChatRoom> findAllRoom(){
        return chatService.findAllRoom();
    }
}
```

컨트롤러는 우리가 기본적인 컨트롤러에서 사용하던 녀석이랑 다른 점이 없다. 여기서는 PostMapping으로 들어오면, 채팅방을 생성하여 채팅방 이름을 반환한다. GetMapping으로 들어오면, 모든 채팅방을 조회하여, 채팅방 객체 리스트를 반환한다. 

#### ① @RequestParam이란? 

파라미터의 이름으로 변수와 바인딩하는 방법이다. 나는 이것이 GetMapping 뒤 쪽에 StringQuery문에 대해서만 가능하다고 생각했는데, 아니었다. PostMapping이어도, RequestBody에 "키:값"형태로 값들이 들어있다면, 키의 이름과 같은 변수를 바인딩하는 것도 가능 했다. 위의 예시에서는 PostMapping에서 @RequestParam을 사용하여, 파라미터를 바인딩 하였다. 

### (4) DTO들에 대하여 

#### ① ChatMessage

```java
/*
* 클라이언트들은 서버에 접속하면 개별의 WebSocket Session을 가지게 된다.
* 따라서 채팅방에 입장 시 클라이언트들의 WebSocket Session 정보를 채팅방에 맵핑 시켜서 보관하고 있으면,
* 서버에 전달된 메시지를 특정 방에 매핑된  WebSocket 세션 전부에게 보낼 수 있음. -> 이것이 메세지를 구현한 모습
* 이것으로 개별 채팅방을 구현할 수 있다.
* */

// 채팅 메세지를 주고받기 위한 DTO
@Getter
@Setter
public class ChatMessage {

    // 메시지 타입: 입장, 채팅 -> 서버에서 클라이언트의 행동에 따라 클라이언트에게 전달할 메세지들을 선언한 것
    public enum MessageType {
        ENTER, TALK
    }


    private MessageType type;   // 메세지 타입
    private String roomId;      // 방 번호
    private String sender;      // 메세지 보낸 사람
    private String message;     // 메세지

}
```

원래 사용하던 DTO와의 차이점은 enum을 통해 Message의 타입을 생성한다는 점이다. 위의 Handler에서 해당 Type을 썼던 부분을 다시 보자. 

```java
     	// 1. 웹소켓 클라이언트로부터 메세지 내용을 전달받아 채팅 메세지 객체로 변환
        ChatMessage chatMessage = objectMapper.readValue(payLoad, ChatMessage.class);

        // 2. 전달 받은 메세지에 담긴 채팅방 ID로 내 메세지를 전송해야 하는 채팅방 객체를 불러온다.
        ChatRoom room = chatService.findRoomById(chatMessage.getRoomId());

        // 3. 해당 채팅방에 입장해있는 모든 클라이언트들(Websocket session)에게 타입에 따른 메세지 발송
        room.handleActions(session, chatMessage, chatService);
```

3번을 보면 room의 행동은 chatMessage의 MessageType이 무엇인가에 따라 행동이 달라진다. 

#### ② ChatRoom

```java
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;
import org.websocket.demo.service.ChatService;

import java.util.HashSet;
import java.util.Set;

// 채팅방 DTO
@Getter
public class ChatRoom {

    // 채팅방의 방 번호와 이름
    private String roomId;
    private String name;

    // 해당 채팅방에 입장한 클라이언트들의 세션 리스트
    private Set<WebSocketSession> sessions = new HashSet<>();

    // 커스터마이징한 생성자 -> @ArgsConstructor 쓰면 안됨! 그거는 모든 멤버 변수에 대해서 매핑을 진행하기 때문에!!
    @Builder
    public ChatRoom (String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }

    // 채팅방에서 하는 행동에는 크게 입장하기와 대화하기 기능 두 개가 있다.
    // 따라서 클라이언트가 어떤 행동을 하느냐에 따라 분기처리를 해줘야 한다.
    // 그것을 위한 매소드이다.
    // 입장 시: 채팅룸의 session 정보를 클라이언트의 session 리스트에 추가
    // 채팅 시: 채팅룸에 메세지가 도착할 경우 채팅룸의 모든 session에 메시지 발송
    public void handleActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService){
        if(chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            sessions.add(session);
            chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
        }
        sendMessage(chatMessage, chatService);
    }

    public <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
    }
}
```

##### ② - ⓐ @Builder란?

생성자 패턴이 아닌 빌더- 패턴으로 객체 생성 및 초기화 할 때, 필요한 어노테이션이다. 먼저 빌더 패턴이 무엇인지 알아보자. 만약 우리가 생성자를 통해서 객체를 생성하고 초기화하려면, 다음과 같은 생성자가 필요할 것이다. 

```java
Bag bag = new Bag("name", 1000, "memo");
```

만약 빌더 패턴을 통해 객체를 생성하고 초기화 하려면, 다음과 같이 하면 된다. 
```java
Bag bag = Bag.builder()
		.name("name")
        	.money(1000)
        	.memo("memo")
        	.build();
```

객체를 생성할 수 있는 빌더를 builder() 함수를 통해서 얻는다.  그 안에 멤버 변수 별로 초기화할 값을 적어주고, 마지막에 .build()를 사용하면, 해당 값들을 가진 객체가 생성된다.

**그렇다면 Builder Pattern을 사용해야하는 이유는 무엇일까?**

빌더 패턴의 경우 생성자보다 장점이 많다. 

1. 생성자 패턴보다 가독성이 좋다. 

   위의 생성자 패터의 경우, 초기화 하는 값이 3개 뿐이라서 보기 쉽지만 초기화할 값이 여러 개일 경우를 생각해보라.
   ```java
   Bag bag = new Bag("name", 1000, "memo", "abc", "what", "is", "it", "?");
   ```

   어떤 변수에 대한 초기화인지 한 눈에 알 수 있는가? 생성자 패턴 초기화만 보고는 알기가 쉽지 않다. 하지만 builder 패턴으로 하게 되면 다음과 같은 장점이 있다. 
   ```java
   Bag bag = Bag.builder()
   		.name("name")
           .money(1000)
           .memo("memo")
           .letter("This is the letter")
           .box("This is the box")
           .build();
   ```

   무슨 멤버변수에 대한 초기화인지 한 눈에 알 수 있다

2. Builder Pattern의 경우, 멤버 변수 초기화에 순서가 존재하지 않는다. 

생성자의 경우, 객체를 초기화 하기 위해 넣어야 하는 값의 순서가 존재했고, 이를 지키지 않았을 때는 에러가 나거나 엉뚱한 값이 들어갔다.

```java
public Bag(String name, int money, String memo) {
	this.name = name;
    	this.money = money;
    	this.memo = memo;
}
```

하지만 빌더 패턴의 경우, 필드 이름 별로 값을 명시하기 때문에 순서가 상관 없다. 따라서 코딩 시 훨씬 편하다. 

```java
Bag bag = Bag.builder()
		.name("name")
        	.memo("memo")	// memo를 money 대신 먼저!
        	.money(1000)
        	.build();
```

**하지만 Builder 패턴 쓰기에 어려운 점...**

빌더 패턴의 장점을 보면 엄청 많지만, 왜 자주 안 보일까? 그 이유는 클래스 내에서 Builder 함수를 커스터마이징 해서 만들어야 하는데 그 과정이 극악의 난이도이기 때문이다. 위의 예제를 보면 멤버 변수 이름별로 mapping이 되었다. 이렇게 멤버 변수 별로 값을 mapping 시키는 함수도 전부 만들어야 하고, Builder 객체를 만드는 생성자 또한 스스로 만들어야 한다! 
  하지만 고맙게도... **LOMBOK은 해당 Builder를 자동으로 생성해주는 어노테이션을 가지고 있고 그것이 @Builder** 이다! 다음 예시는 @Builder 어노테이션을 썼을 시, 와 안 썼을 시를 나누어 놓은 것이다. 어노테이션의 편리함을 실감해봐라 

 ```java
 // 빌더 어노테이션을 쓸 경우 ----------------------------------------------------------------------------
    @Builder
    class Example<T> {
    	private T foo;
    	private final String bar;
    }
    
 // 안 쓸 경우 -----------------------------------------------------------------------------------------
    class Example<T> {
    	private T foo;
    	private final String bar;
    	
    	private Example(T foo, String bar) {
    		this.foo = foo;
    		this.bar = bar;
    	}
    	
    	public static <T> ExampleBuilder<T> builder() {
    		return new ExampleBuilder<T>();
    	}
    	
    	public static class ExampleBuilder<T> {
    		private T foo;
    		private String bar;
    		
    		private ExampleBuilder() {}
    		
    		public ExampleBuilder foo(T foo) {
    			this.foo = foo;
    			return this;
    		}
    		
    		public ExampleBuilder bar(String bar) {
    			this.bar = bar;
    			return this;
    		}
    		
    		@java.lang.Override public String toString() {
    			return "ExampleBuilder(foo = " + foo + ", bar = " + bar + ")";
    		}
    		
    		public Example build() {
    			return new Example(foo, bar);
    		}
    	}
    }
 ```

#### ② - ⓑ handleAction에 대해

```java
    public void handleActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService){
        if(chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            sessions.add(session);
            chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
        }
        sendMessage(chatMessage, chatService);
    }

    public <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
    }
}

```

위의 매소드는 멤버 메소드이다. 해당 매소드는 인수로 들어온 chatMessage의 MessageType에 따라 해야할 일을 나눠서 관리하고 있다. 타입이 "ENTER"일 경우,  ~~님이 입장했습니다 라는 문자열을 모든 채팅방 멤버에게 전송한다. 그 외의 경우는 그냥 메세지를 전달한다. 

메세지 전달함수를 따로 뺴서 정의하였는데, Stream을 사용해서 현재 리스트에 든 세션 하나하나에게 메세지를 보낸다. 

**소견**

여기서 보면 DTO 속에 매소드를 정의하면서 Controller - service 관게를 제대로 지키지 않았다는 생각이 든다. 하지만 위의 webSockConfig라는 컨트롤러 역할을 하는 녀석에서 Dto 객체를 만든 후 그 안에 매소드를 바로 써야하는 상황이 나오긴 한다. 이처럼 컨트롤러 역할을 하는 녀석이 두 명이라 정석적인 MVC 패턴을 지키기 어려웠는지 아니면, 코드 작성하신 분이 편의에 따라 이렇게 하셨는지 분간은 안되지만, 내가 나중에 스스로 만들 때는 각 디렉토리 별 해야할 기능을 정확히 지켜서 다시 작성해봐야겠다.

### (5) ChatService

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ObjectMapper objectMapper;

    // 서버에 생성된 모든 채팅방의 정보를 모아둔 구조체. (채팅방의 정보 저장은 빠른 구현을 위해 일단 DB 안 쓰고 HashMap 저장으로 구현)
    private Map<String, ChatRoom> chatRooms;


    // 생성자 이용해 생성된 후에 할 일을 정의
    @PostConstruct
    private void init() {
        chatRooms = new LinkedHashMap<>();
    }

    // 모든 방 조회
    public List<ChatRoom> findAllRoom() {
        return  new ArrayList<>(chatRooms.values());
    }

    // 방 번호를 이용한 방 찾기
    public ChatRoom findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }

    // 방 생성
    public ChatRoom createRoom(String name) {
        // 방번호로 쓸 아이디 생성
        String randomId = UUID.randomUUID().toString();

        // 채팅방 (1 개) 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(randomId)
                .name(name)
                .build();
        //생성된 채팅방을 채팅방 리스트에 넣기
        chatRooms.put(randomId, chatRoom);

        return chatRoom;
    }

    // 세션 하나에 메세지를 보내는 매소드
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
        }catch (IOException e){
            log.error(e.getMessage(), e);
        }
    }
}
```

#### ⓐ @PostConstruct란?

Spring이 해당 클래스를 bean 객체로 만든 후에 딱 한번만 @PostConstruct가 달린 매소드를 호출하여 실행한다. 프론트에서 배웠던, 컴포넌트의 생명주기를 생각하면 더 이해하기 쉬울 것이다. 

#### ⓑ UUID란?

네크워크 상에서 유일성이 보장되는 ID를 만들기 위한 규약이다. (Universe Unique Identifier)
UUID는 분산 네크워크 환경에서 사용되는 ID로, 만약 중앙 관리 시스템이 있는 환경이면, 모든 클라이언트의 세션마다 일렬번호를 부여해서, 모든 클라이언트들에 대해서 유일성을 보장할 수 있겠다. 하지만 분산 네크워크 환경이면, 케르베로스처럼 머리가 되는 관리 시스템이 여러 개일 수 있기 때문에, 각 머리가 클라이언트의 세션에 고유 번호를 준다는 보장이 없다. 이때 사용하는 것이 UUID이다.

#### ⓒ WebSocketSession 

```java
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
        }catch (IOException e){
            log.error(e.getMessage(), e);
        }
    }
```

이 부분에 대해 해석하겠다. 
websocksession의 경우, sendMessage라는 매소드를 가지고 있다. 해당 매소드를 통해 해당 세션에 메세지를 보낼 수 있다. 메세지는 Message 객체 형태이어야 하고, 이때 Message의 payload는 직렬화 상태여야 한다. 

# 3. 시연 

## (1) 채팅방 생성

먼저 PostMan을 통해 Post 요청을 보내서 채팅방을 생성한다. 

![image2](.\\Images\image2.PNG)

우리는 결과값으로 받은 방번호를 통해서 접속해야한다. 아직 프론트 엔드는 구현하지 않았음으로, 방번호는 우리가 직접 복사하여 클라이언트 입장시 사용한다. 

## (2) 채팅방 입장 

우리는 아직 프론트 엔드 구현이 안되어있기 때문에 크롬 확장자 중 하나인  'WebSocketClient'를 쓴다. 실시간 채팅 확인을 위해 크롬 브라우저를 두 개 띄우고 양 쪽에서 전부 채팅방에 입장한다. 위에서 봤듯이 입장을 위한 Message Type은 ENTER였다. 적절한 JSON을 만들어서 입장하겠다. 
 아까 webSockConfig 파일에서 정의 해두었던, /ws/chat이 웹소켓 채팅방을 쓸 수 있는 주소 이다. 

![image3](.\Images\image3.PNG)

일단 이거 깔아라 

![image4](.\Images\image4.PNG)

해당 방번호로 입장하겠다. ![image5](C:\Users\SSAFY\IdeaProjects\WebSocketDemo\Images\image5.PNG)

입장 성공했다. 

이번엔 2번째 클라이언트를 입장시켜보겠다. 2번째 클라이언트가 들어오면 전개도에 따라 2번째 클라이언트가 입장했다는 메세지가 차무식과 클라이언트 2 전부에게 가야한다. 
![image6](C:\Users\SSAFY\IdeaProjects\WebSocketDemo\Images\image6.PNG)

클라이언트 2 이름은 허성태인데 허성태가 입장했음이 양쪽 다에게 떴다. 

마지막으로 TALK을 해보겠다. 차무식이 허성태한테 협박을 하도록 해보겠다. 메세지 타입을 TALK으로 바꾼다. 

![image7](C:\Users\SSAFY\IdeaProjects\WebSocketDemo\Images\image7.PNG)

협박 문자가 잘 갔다. 
다음은 웹 소켓에 프론트엔드도 추가해서 더 심화버젼을 만들어보겠다. 해당 내용 설명은 STEP2 branch에 올리고 만들겠다.  
