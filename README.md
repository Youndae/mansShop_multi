# Man's Shop Multi-module

## 목적
- Multi-module 구조의 프로젝트 완성
- 추후 MSA까지 진행하기 위한 초석

## 환경
- Spring Boot 3.4.0
- JDK 17
- MySQL
- h2(test DB)
- Redis
- IntelliJ

## dependency
- spring-boot-starter-web
- lombok
- Redis
- MySQL connector
- commons-io
- commons-fileupload
- Spring Data JPA
- QueryDSL
- spring-boot-starter-aop
- jackson
  - databind 2.16.1
  - dataformat-yaml 2.16.1
- json 20240303
- log4j2
- swagger (springdoc-openapi-starter-webmvc-ui 2.6.0)
- spring-boot-starter-oauth2-client
- h2
- jwt 4.4.0
- iamport-rest-client-java 0.2.23
- spring-boot-starter-mail 3.3.0

## 모듈별 의존성

- 공통(root allprojects)
  - aop
  - jackson databind
  - jackson dataformat yaml
  - json
  - log4j2
  - Spring Data JPA (module-api만 제외처리)
  - queryDSL (module-api만 제외처리)
  - swagger
  - lombok
- module-config
  - spring-boot-starter-web
  - oauth2-client
  - redis
  - mysql-connector-j
  - h2
  - jwt
- module-api
  - spring-boot-starter-web
- module-user
  - spring-boot-starter-web
  - commons-io
  - commons-fileupload
  - spring-boot-starter-mail
- module-admin
  - spring-boot-starter-web
  - commons-io
  - commons-fileupload
- module-order
  - iamport

