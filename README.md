# 📚 Deokhugam - 책 덕후들의 감상 공유 커뮤니티

> 도서 이미지 OCR 및 ISBN 매칭 기반 리뷰 공유 플랫폼  
> 책 읽는 즐거움을 나누고 지식을 교류하는 **책 덕후들의 공간**

[![codecov](https://codecov.io/gh/SB3-deokhugam-3/SB03-Deokhugam-Team3/branch/dev/graph/badge.svg?token=40MRGXBI6U)](https://codecov.io/gh/SB3-deokhugam-3/SB03-Deokhugam-Team3)
---

## 🔗 Notion

👉 [팀장즈 Notion 페이지](https://www.notion.so/ohgiraffers/207649136c118098b9c8c287d57fe7de)

---

## 👥 Team3 - 팀장즈

| 이름 | GitHub                                    | 개발 기능         | 개발 외 역할       |
|------|-------------------------------------------|---------------|---------------|
| 고희준 | [barrackkk](https://github.com/barrackkk) | 사용자 & 알림 관리   | 팀장            |
| 김현기 | [LZHTK](https://github.com/LZHTK)         | 도서, 파워 유저     | Git 형상관리      |
| 안여경 | [yeokyeong](https://github.com/yeokyeong) | 리뷰, 댓글, 인기 리뷰 | AWS           |
| 조현아 | [hyohyo-zz](https://github.com/hyohyo-zz) | 리뷰, 댓글, 인기 리뷰 | 회의록           |
| 한동우 | [Dw-real](https://github.com/Dw-real)     | 도서, 인기 도서     |  ERD, AWS RDS |

---

## 🛠 기술 스택

| 분류       | 기술 |
|------------|------|
| **Backend** | Spring Boot 3.5.3 |
| **Database** | PostgreSQL 17.5, H2 |
| **API 문서화** | Swagger UI |
| **협업 도구** | Discord, GitHub, Notion |
| **일정 관리** | GitHub Issues + Notion Timeline |

---

## 📌 주요 기능 요약

### 고희준

<img src="https://github.com/user-attachments/assets/341baca2-edab-4beb-8ca2-4e5c9aa45670" width="300"/>

- 사용자 관리: 등록, 수정, 삭제, 로그인
- 알림 관리: 등록, 수정, 삭제, 목록 조회, 삭제 배치

---

### 김현기

<img src="https://github.com/user-attachments/assets/9aa08b65-93e7-4525-b2b2-8d7e5d902a95" width="400"/>

- 도서 관리: OCR 기반 ISBN 등록, 목록 조회, 삭제
- 파워유저 관리: 배치, 목록 조회

---

### 안여경

<img src="https://github.com/user-attachments/assets/5dc99d35-9704-4b66-85c9-8915f3c8fd61" width="320"/>

- 리뷰 관리: 삭제, 목록 조회
- 댓글 관리: 등록, 수정, 상세 조회
- 인기 리뷰 관리: 배치

---

### 조현아

<img src="https://github.com/user-attachments/assets/76d9fd61-1119-423a-9be3-f7774b464aa0" width="350"/>

- 리뷰 관리: 좋아요, 등록, 수정, 상세 조회
- 댓글 관리: 목록 조회, 삭제
- 인기 리뷰 관리: 목록 조회
- 사용자 관리: 삭제 배치

---

### 한동우

<img src="https://github.com/user-attachments/assets/28245d6b-8466-4ca7-a383-e05bf1237080" width="400"/>

- 도서 관리: 등록, 수정, ISBN 상세 조회 (Naver API)
- 인기 도서 관리: 배치, 목록 조회

---

## 📁 프로젝트 구조

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
## 배포 URL

http://3.38.139.184/


---


## 회고록

https://www.notion.so/ohgiraffers/23f649136c118059b381dc88c93b4746

---
