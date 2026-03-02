# 취미 플랫폼 프로젝트

인터페이스 기반의 모듈 결합과 WebSocket 실시간 통신을 적용한 백엔드 중심 프로젝트

---

## 📌 Project Overview

관심사가 같은 사람들끼리 취미를 공유하고 소통하는 커뮤니티입니다. 

단순한 기능 구현을 넘어 멀티 모듈 구조를 통한 계층 분리, WebSocket을 활용한 실시간성 확보, 그리고 다형성 설계를 통한 유연한 데이터 구조 구축에 집중했습니다.

## 🛠 Tech Stack
Backend Core
- Language: Java 17

- Framework: Spring Boot 3.2.x

- Build Tool: Gradle (Multi-Module Management)

- Database: MySQL 8.0 (Production), H2 (Test/Local)

- ORM: Spring Data JPA

Messaging & Real-time
- Protocol: WebSocket (STOMP)

- Library: spring-boot-starter-websocket

- Features: Pub/Sub 기반 실시간 1:1 채팅, 채팅방 세션 관리

Security & Auth
- Security: Spring Security 6.x

- Authentication: JWT (jjwt 0.12.3)

- Features: Custom JWT Filter, 인증 기반 API 접근 제어, BCrypt 패스워드 암호화

Documentation & Test
- API Doc: OpenAPI 3 (Swagger UI / springdoc-openapi 2.2.0)

- Test: JUnit5, AssertJ

- Lombok: 보일러플레이트 코드 제거 및 생산성 향상

## 🏗 System Architecture (Multi-Module)

계층별 책임 분리와 모듈 간 결합도 완화를 위해 프로젝트를 독립적인 모듈로 분리하여 관리합니다.

- api-module : 전체 서비스의 Entry Point. Security 설정 및 각 도메인 서비스를 조합하여 API 제공.
- common-module : 전역 예외 처리 및 도메인 간 협력을 위한 Interface 정의 (결합도 완화).
- user-module / board-module : 회원 및 취미 게시글/댓글의 핵심 비즈니스 로직 독립 보유.
- interaction-module : 좋아요 및 신고 로직 담당. 타 모듈과의 의존성 최소화 설계.
- chat-module : websocket, stomp를 이용한 실시간 채팅 제공.

## 🚀 Key Technical Implementation
###  💬 WebSocket & STOMP 기반 실시간 채팅
- 구현 방식: STOMP 프로토콜을 활용한 메시지 브로커 기반의 Pub/Sub 구조 설계.

- 데이터 모델: chat_room, chat_room_member 테이블을 통한 다대다(M:N) 관계 매핑 및 메시지 이력 관리.

- 학습 포인트: 실시간 통신의 Handshake 및 세션 유지 과정을 직접 디버깅하며 데이터 흐름 이해도 제고.

### 🛡️ 인터페이스 기반 설계 및 자율 정화 시스템
- 인터페이스 활용: common-module에 추상화된 인터페이스를 두고 각 모듈에서 구현하게 함으로써 모듈 간 직접 참조 제거.

- 신고 시스템: REPORT 테이블의 다형성 구조를 활용해 게시글/댓글 신고 통합 관리. 누적 신고 20개 초과 시 자동 비공개 로직 구현.
 
## 📊 Data Modeling (ERD)
  프로젝트의 전체 데이터 구조입니다. 설계의 핵심은 다형성을 활용한 상호작용 관리와 채팅 데이터의 정규화입니다.

![erd.png](images/erd.png)

- 다형성(Polymorphism): LIKE, REPORT 테이블에서 target_type을 사용하여 게시글/댓글 등 다양한 대상을 유연하게 처리.

- 관계 설계: chat_room_member 중간 테이블을 활용한 유저-채팅방 간의 관계 최적화.

## 🔍 Troubleshooting (백엔드 고민 지점)
- 순환 참조 해결: 모듈 분리 후 발생한 의존성 사이클을 API 계층에서의 로직 조합과 인터페이스 추출을 통해 해결.

- 멀티 모듈 빈(Bean) 스캔: 서로 다른 모듈에 흩어진 Component들을 메인 어플리케이션에서 인식하도록 스캔 범위 최적화.

## 📝 Detailed Progress (기존 개발 기록)

<details>
<summary>기능 구현 상세 리스트 펼치기</summary>

- 설정
- [x] Swagger
- [x] Security
- [x] MailSender
- [x] PasswordEncoder
- [x] Jwt Filter


- 회원 
- [x] 회원가입
- [x] 이메일 인증
- [x] 로그인
- [x] 회원 정보 수정(회원)
- [x] 비밀번호 재설정
- [x] 회원 관리 (관리자)
- [x] 회원 탈퇴, 복구


- 게시판 / 공지사항
- [x] 카테고리 등록(회원/ 관리자)
- [x] 카테고리 관리(조회, 삭제) (관리자)
- [x] 게시글 작성 (회원)
- [x] 게시글 조회 (회원)
- [x] 게시글 상세 조회(회원 / 관리자)
- [x] 게시글 조회(전체, 비공개만, 공개만, 카테고리별, 이메일별) (관리자)
- [x] 게시글 수정 (회원)
- [x] 게시글 삭제 (회원)
- [x] 게시글 숨김 (관리자 -> 신고 20개 이상일 경우)
- [x] 댓글 작성 (회원)
- [x] 댓글 조회 (회원/ 관리자)
- [x] 댓글 수정 (회원)
- [x] 댓글 삭제 (회원)
- [x] 댓글 숨김 (관리자 -> 신고 20개 이상일 경우)
- [x] 공지사항 작성 (관리자)
- [x] 공지사항 조회 (관리자)
- [x] 공지사항 수정 (관리자)
- [x] 공지사항 삭제 (관리자)
- [x] 공지사항 공개여부 (관리자)


- 상호작용
- [x] 게시글 좋아요 / 취소 (회원)
- [x] 댓글 좋아요 / 취소 (회원)
- [x] 좋아요 개수 구하기
- [x] 좋아요 관련 조회 (좋아요 한 게시글, 댓글별 조회)(회원)
- [x] 게시판 / 댓글 신고 하기 (회원)
- [x] 신고 조회 (전체, 아이디별, 게시글별, 댓글별) (관리자)
- [x] 신고 취소 (회원)
- [x] 신고 수 


- 채팅
- [x] 채팅방 만들기
- [x] 메시지 보내기
- [x] 보낸 메시지 삭제하기
- [x] 읽음 여부

</details>


## 📈 Future Roadmap
- [ ] 서버 확장 시 세션 공유를 위한 Redis Pub/Sub 도입

- [ ] QueryDSL 도입을 통한 복잡한 동적 쿼리 최적화

- [ ] GitHub Actions를 활용한 CI/CD 자동화 구축
