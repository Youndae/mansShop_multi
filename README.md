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
  - SpringSecurity
  - jackson databind
  - jackson dataformat yaml
  - json
  - log4j2
  - Spring Data JPA (module-api만 제외처리)
  - queryDSL (module-api만 제외처리)
  - lombok
- module-config
  - spring-boot-starter-web
  - redis
  - mysql-connector-j
  - h2
- module-auth
  - spring-boot-starter-web
  - spring-boot-starter-oauth2-client
  - com.auth0:java-jwt:4.4.0
  - spring-boot-starter-data-redis
- module-api
  - spring-boot-starter-web
  - swagger(springdoc-optionapi-starter-webmvc-ui:2.6.0)
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
  - spring-boot-starter-web
  - iamport

## 각 모듈의 의존 관계

- module-common
  - 모든 module이 참조
- module-config
  - module-common을 제외한 모든 module이 참조
- module-auth
  - module-common과 module-config를 제외한 모든 module이 참조.
- 각 서비스 모듈
  - module-api가 각 서비스 모듈을 참조.
  - module-api의 경우 서비스 모듈 뿐만 아니라 모든 module을 참조하고 있는 상태.


## 모듈 설계 설명

- module-common
  - Entity, 공통 DTO 등 도메인에 대한 클래스들이 모여있는 모듈
- module-config
  - 공통적으로 사용될 모든 yml, properties가 위치.
  - 그 외 보안을 제외한 나머지 설정 파일들이 위치.
- module-auth
  - 보안에 대한 설정과 도메인 등의 클래스가 위치.
  - jwt, SpringSecurity, SuccessHandler, UserDeatils, oAuth2등의 인증 / 인가 관련 설정 및 도메인, Filter 등 모두 여기에 위치.
  - 인증 / 인가와 oAuth2 관련 기능이 포함되어 있기에 사용자 관련된 MemberRepository가 위치.
- module-api
  - Controller, UseCase, 그 외 요청 및 응답 데이터 처리를 위한 Mapper 또는 Factory가 위치.
- module-user
  - oAuth2가 아닌 로컬 로그인, 회원가입, 장바구니 및 마이페이지 기능에 대한 서비스 모듈.
- module-admin
  - 관리자의 모든 기능을 담는 모듈
- module-order
  - 주문 관련 모듈
- module-product
  - 상품 관련 모듈.
  - 상품 리스트, 상품 상세 페이지 등의 기능 포함


## 히스토리 및 문제 해결 과정

- 24/12/14 
  - 모듈 설계 및 최초 프로젝트 생성 및 설정.
  - 모듈 구조는 common, config, api, 그 외 서비스 모듈
  - config에서 모든 설정 클래스 포함.

<br/>

- 24/12/15
  - module-product에 메인 페이지 관련 Service와 Repository 생성 및 테스트
  - 문제 발생
    - module-config에서 인증 관련 설정 및 처리를 하다보니 MemberRepository에서 사용자 조회 기능이 필요.   
      그렇다고 config에서 MemberRepository를 담고 있기에는 구조가 애매하다고 생각했기 때문에 인터페이스를 통한 처리를 하고자 config에서 인터페이스 생성.   
      module-user에서는 module-config를 참조하고 있기 때문에 해당 서비스 구현체를 module-user가 갖고 MemberRepository 조회 처리를 수행하도록 추가.   
      module-config에서는 정상적으로 테스트를 통과했고, module-user에서 Repository 테스트도 통과했으나, module-product에서 테스트를 수행하던 중 문제가 발생.
  - 문제 원인
    - module-product에서 테스트를 수행하면서 각 Component를 가져와야 하는데 config의 Service Bean에 대해 구현체가 없다는 오류가 발생.   
      구현체가 module-user에만 존재하면 되는 상황이지만 module-product는 module-user를 바라보고 있지 않기 때문에 해당 구현체를 알 수 없어 오류가 발생한 것.
  - 문제 해결 과정
    - 생각했던 문제 해결 방법으로는 module-product가 module-user를 참조하면 문제가 해결되지 않을까? 라는 생각으로 시작.   
      그러나, 참조하는 것으로 문제가 해결되지 않았고 방법을 찾던 중 테스트 클래스에서 @MockitoBean을 통해 Mock객체를 만들어주면 문제를 해결할 수 있다는 점을 알게 됨.   
      실제로 테스트해본 결과 문제가 해결되는 것을 확인했지만, 이렇게 처리하게 되면 앞으로 작성하게 될 모든 Module의 테스트 클래스에서 사용하지도 않을 Mock 객체를 만들어야 한다는 점이 문제.   
      그래서 Module 구조를 다시 고민하며 설계 자체를 수정할 계획을 세웠고, 그 결과 module-auth로 분리하고 auth 모듈에서는 인증 / 인가에 대한 모든 처리를 담당하도록 하는 방법을 선택.   
      MemberRepository도 auth 모듈에 위치하도록 수정했는데 이유는 인증 / 인가 관련 기능을 제외하고도 모든 모듈에서 Member 엔티티를 조회하는 기능이 많았기 때문에 auth 모듈에서 갖도록 처리하고 각 모듈들은 Member 관련 조회를 auth 모듈을 통해 처리하도록 수정.   
      그 결과 하위 모듈에서 테스트 수행 시 정상적으로 수행할 수 있고 불필요한 Mock 객체를 생성하지 않고도 처리가 가능해졌다.   
      auth 모듈에 MemberRepository를 위치시켜야 한다는 점이 구조상 책임 분배가 제대로 이루어지지 않는 것이 아닐까 라는 생각을 했지만, 도메인 관점으로 각 Module을 설계했기 때문에 인증 / 인가에 대한 기능이 존재하는 auth에 위치시키는 것이 괜찮겠다는 생각을 했다.   
      또한, user 모듈에서도 인증 / 인가를 제외하고는 MemberRepository를 호출하는 경우가 데이터 확인인 경우가 많으므로 문제 발생 여지가 적다라는 생각을 했다.

<br/>

- 24/12/18
  - 문제 발생
    - module-api에서 상품 리스트 관련 swagger 작성 중 문제 발생.   
      페이지네이션 기능이 존재하는 경우 동일한 구조로 처리하기 위해 PagingResponseDTO 라는 record가 존재하고 그 내부에 제네릭 타입의 content라는 필드가 존재한다.   
      그러나, 제네릭 타입이다보니 swagger에서 어떤 클래스를 잡아야 하는지 판단할 수 없어 content에 대한 데이터가 { } 로 처리되는 문제가 발생.
  - 문제 원인
    - 여러 타입의 content를 받아야 하는 record이다보니 명확하게 이 DTO를 사용할거다! 라는게 없어서 발생하는 문제.
  - 문제 해결 과정
    - Controller에서 @ApiResponse 내부 content 하위에 위치한 schema 설정에서 property 옵션으로 @SchemaProperty를 통해 설정하는 방법이 존재한다는 것을 알 수 있었으나,    
      Incompatible types. Found: 'io.swagger.v3.oas.annotations.media.Schema', required: 'io.swagger.v3.oas.annotations.StringToClassMapItem' 이런 오류가 발생하며 정상적으로 처리되지 않았다.   
      다른 방법으로 ResponseEntity를 통해 해당 데이터를 반환하고 있으니 따로 ApiResonse를 굳이 작성하지 않아도 정상적으로 출력된다는 것을 확인.   
      이 방법으로 수정했을 때 의도한대로 출력되는 것을 확인할 수 있었지만, 문제는 다른 응답도 추가하는 경우 최하단에 의도한 출력이 발생하고 200 설정에서는 content가 {}로 출력되는 문제가 발생.   
      여러 방법을 통해 문제를 해결해보고자 했으나, 방법을 찾지 못하고 있었고 그 중 useReturnTypeSchema = true 설정을 통해 의도한 content를 출력할 수 있다는 것을 알 수 있었다.   
      이 경우 ApiResponse Annotation 하위에 위치하며 content 설정이 없어야 정상적으로 출력된다는 단점이 있다.   
      그리고 content 설정을 추가하면 아무런 데이터가 출력되지 않기 때문에 content 내부에서 media type 설정이 불가능하다는 점도 단점이었다.   
      하지만 최종적으로 이 방법을 선택해 처리하기로 했고, swagger가 제네릭을 제대로 처리할 수 있는 방안이 없다는 의견이 많았기에 추후 더 알아보고 개선하는 방법으로 마무리.