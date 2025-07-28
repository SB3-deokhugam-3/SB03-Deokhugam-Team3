## Team3 팀장즈
---
Notion Link - [팀장즈](https://www.notion.so/ohgiraffers/207649136c118098b9c8c287d57fe7de)

## 팀원 구성

고희준 (https://github.com/barrackkk)

김현기 (https://github.com/LZHTK)

안여경 (https://github.com/yeokyeong)

조현아 (https://github.com/barrackkk)

한동우 (https://github.com/Dw-real)

## Deokhugam

<img width="950" height="885" alt="전체샷" src="https://github.com/user-attachments/assets/e8581ae1-1f6f-4ad4-99c4-0173189fee73" />

- 도서 이미지 OCR 및 ISBN 매칭 서비스
- 책 읽는 즐거움을 공유하고, 지식과 감상을 나누는 책 덕후들의 커뮤니티 서비스

---

## 기술 스택

| **분류**   | **사용 예정 도구**                |
|----------|-----------------------------|
| Backend  | Spring Boot 3.5.3           |
| Database | PostgreSQL 17.5, H2         |
| API 문서화  | Swagger UI                  |
| 협업 도구    | Discord, GitHub, Notion     |
| 일정 관리    | GitHub Issues + Notion 타임라인 |

--- 

## 팀원별 구현 기능 상세

## 고희준

<img width="377" height="522" alt="알림" src="https://github.com/user-attachments/assets/341baca2-edab-4beb-8ca2-4e5c9aa45670" />

- 사용자 관리

    - 등록

    - 수정

    - 삭제

    - 로그인

- 알림 관리

    - 등록

    - 수정

    - 삭제

    - 목록 조회

---

## 김현기

<img width="901" height="605" alt="ISBN" src="https://github.com/user-attachments/assets/9aa08b65-93e7-4525-b2b2-8d7e5d902a95" />

- 도서 관리

    - OCR 기반 ISBN 등록

    - 목록 조회

    - 삭제

- 파워유저 관리

    - 배치

    - 목록 조회

---

## 안여경

<img width="606" height="478" alt="인기 리뷰" src="https://github.com/user-attachments/assets/5dc99d35-9704-4b66-85c9-8915f3c8fd61" />

- 리뷰 관리

    - 삭제

    - 목록 조회

- 댓글 관리

    - 등록

    - 수정

    - 상세 정보 조회

- 인기 리뷰 관리

    - 배치

---

## 조현아

<img width="889" height="768" alt="리뷰" src="https://github.com/user-attachments/assets/76d9fd61-1119-423a-9be3-f7774b464aa0" />

- 리뷰 관리

    - 좋아요

    - 등록

    - 수정

    - 상세 정보 조회

- 댓글 관리

    - 목록 조회

    - 삭제

- 인기 리뷰 관리

  -- 목록 조회

--- 

## 한동우

<img width="925" height="651" alt="등록된 도서" src="https://github.com/user-attachments/assets/28245d6b-8466-4ca7-a383-e05bf1237080" />

- 도서 관리

    - 등록

    - 수정

    - Naver API로 ISBN 정보 조회

    - 상세 정보 조회

- 인기 도서 관리

    - 배치

    - 목록 조회

---

## 파일 구조

```java
com.example.myproject
├── global         ← 전역 사용 항목
│   ├── config
│   │   └── SwaggerConfig.java
│   ├── exception
│   │   └── GlobalExceptionHandler.java
│   ├── dto        # 전역에서 사용되는 DTO가 있다면 (PageResponse, ErrorResponse 등)
│   ├── util       # 유틸 클래스들 (DateUtil, StringUtil 등)
│   └── base       # 공통 엔티티(BaseEntity), 공통 응답 클래스 등
├── domain         ← 도메인별 기능 모듈
│   ├── User
│   │   ├── controller
│   │   │   ├── api
│   │   │   └── UserController.java
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   ├── mapper
│   │   └── dto
│   │        └── data
│   │        └── request     
│   └── Book
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       ├── mapper
│       └── dto
└── Sb03DeokhugamTeam3Application.java
```

---

## 구현 홈페이지

---

## 회고록

---
