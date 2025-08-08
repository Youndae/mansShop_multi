# Man's Shop Multi-module

## 목적
- Multi-module 구조의 프로젝트 완성
- 추후 MSA까지 진행하기 위한 초석

## 환경
- Spring Boot 3.4.0
- JDK 17
- Docker
  - MySQL
  - Redis
  - mailhog(integration-test)
- IntelliJ

<img src="./README_image/dependency_diagram.png">

## Architecture
- Clean Architecture
  - Architecture 규칙
    - UseCase는 데이터 조작(조회, 갱신, 삭제 등), 비즈니스 로직 서비스 호출을 통해 데이터를 조합 후 응답하는 역할
      - HTTP Method에 따라 GET 요청은 ReadUseCase, POST, PUT, PATCH, DELETE 요청은 WriteUseCase에서 처리
    - Service는 DataService, DomainService, ExternalService로 분리
      - DataService에서는 MySQL, Redis 등의 데이터 조회, 갱신 등 데이터 조작 관련
      - DomainService에서는 비즈니스 로직, 검증 관련
      - ExternalService에서는 메일 전송, RabbitMQ 호출 등 외부 시스템 호출 관련
    - Service 메서드 분리 규칙
      - 단일 책임 원칙을 최대한 위배하지 않도록 각 메서드는 단위별로 설계
      - 기능에 완전 종속적으로 재사용 가능성이 없는데 로직도 단순한 경우 분리하지 않도록 설계
        - 이런 케이스까지 분리하게 되면 너무 과한 세분화가 이루어져 오히려 복잡성 증가 우려
  - 요청 발생 시 호출 구조
    - Controller(module-api) -> UseCase(각 서비스 모듈) -> Service -> Repository

## Test
- Controller
  - 통합 테스트
- UseCase
  - 통합 테스트
  - 단위 테스트
- Service
  - 로직이 포함된 메서드만 단위 테스트 수행 단순 비교, 단순 Repsoitory 조회 같은 메서드는 생략
  - 필요에 따라 통합 테스트
- Repository
  - 통합 테스트


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

<br/>

- 24/12/30
  - module-product 작업 중
    - 서비스 코드 작성.
    - Repository 테스트 코드 작성 및 테스트 중
    - 이후 Service 테스트 코드 작성 및 테스트 필요.
    - module-api에서 ProductController 메소드 작성.

<br/>

- 25/01/05
  - module-product Repository 테스트 코드 작성 및 테스트 완료.
    - 각 테스트의 BeforeEach, Fixture 주석 추가.
  - module-product MainService 단위 테스트 코드 작성 및 테스트 완료.

<br/>

- 25/01/07
  - module-product ProductService 단위 테스트 코드 작성 및 테스트 완료.
  - module-product MainService 통합 테스트 코드 작성 중.
  - module-config application-integration-test.yml 추가.
    - 통합 테스트 환경은 동일한 로컬 MySQL mansShop-test 데이터베이스에서 테스트 하도록 처리.
    - 현재 편의상 prod, dev는 동일한 mansShop, integration-test에서는 mansShop-test, test에서는 h2를 사용.

<br/>

- 25/01/08
  - Product의 createdAt, updatedAt 타입을 LocalDate -> LocalDateTime으로 변경.
    - BEST 상품 조회를 제외한 나머지 상품 조회는 모두 createdAt을 기준으로 하기 때문에 시,분,초를 갖도록 수정.
  - ProductRepositoryTest 수정 및 재 테스트
    - 기존 테스트 Fixture는 5개의 데이터만 넣어두고 테스트했는데 각 기능에 대한 명확한 테스트를 위해 20개의 데이터로 수정.
    - OUTER, TOP 두개의 Classification을 생성했기 때문에 분류별 조회에서도 좀 더 명확하게 테스트를 수행할 수 있고, 상품 검색 또한 TOP이 포함된 상품명을 검색하는 테스트를 수행하도록 해 모든 기능의 테스트를 수행 할 수 있게 되었다.
  - ProductService 통합 테스트 작성 및 테스트 완료.

<br/>

- 25/01/09
  - module-user의 service, DTO 작성.
    - 각 Service 클래스 생성은 했지만, 작성한건 MemberService만 작성.
  - module-user의 MemberServiceUnitTest 작성 및 테스트 완료.

<br/>

- 25/01/10
  - module-user의 MemberService 통합 테스트 작성 및 테스트 완료.
  - module-auth의 SecurityConfig에 작성되어 있던 BCryptPasswordEncoder Bean AuthConfig로 분리.
    - module-user에서 통합 테스트 중 BCryptPasswordEncoder가 필요한데 주입을 위함.
  - module-user에서 메일 테스트를 위해 mailhog 사용.
    - Docker에서 jcalonso/mailhog를 실행하고 application-integration-test.yml에 설정해서 메일 테스트 수행

<br/>

- 25/07/18
  - 작업 재시작
    - Monolithic Boot 버전이 많이 개선되었기 때문에 그에 맞춰서 구조 제외 코드 전부 개선해서 처리 예정
    - module-product 우선적으로 구현 예정
    - 공통 테스트 fixture는 module-common에 배치
    - module-config에서 각종 config 파일들을 관리하는데 각 모듈에서 의존성 문제로 인해 resources/META-INF/spring 하위에 org.springframework.boot.autoconfigure.AutoConfiguration.imports 파일 생성
      - 실제로 문제가 발생하면서 동작을 안한것은 아니지만 빨간 밑줄이 계속 신경쓰여서 이 방법으로 처리
    - application-integration-test.yml을 제거하고 application-test.yml로 처리
    - h2도 의존성 및 설정 다 제거하고 미사용으로 처리 계획.
    - module-product에서 메인 관련 기능 구현 및 MainService Integration test 코드 작성 및 테스트

<br/>

- 25/07/21
  - Main, Member 구현 및 테스트
    - MainController, MemberController 작성 및 통합 테스트
    - MainService, UserReadService, UserWriteService 작성 및 단위, 통합 테스트
    - 테스트 과정에서 MailHog 사용을 위한 MailHogUtils 생성
    - MailHogUtils의 경우 하나의 모듈에 작성하고 참조하도록 하기에는 오히려 복잡도가 증가. module-api와 module-user에서만 사용된다는 점을 고려해 완전 중복되는 코드더라도 각각 작성하는 방향으로 설계.
      - 단순히 Utils를 의존하는 케이스를 넘어 불필요하게 spring-boot-starter-mail 의존성까지 가져야 한다는 문제.
      - MailHogUtils가 MailHog에서 처리된 메일 데이터 조회 및 제거만 담당하고 있다는 점에서 차라리 중복을 허용하는게 운영 환경까지 고려했을 때 적합하다고 생각.

<br/>

- 25/07/22
  - module-user 마무리
    - 서비스 구조 개선.
      - DataService, DomainService, ExternalService로 분류.
      - DataService는 DB를 통한 데이터 조회 및 갱신, 삽입, 삭제만 담당
      - DomainService는 비즈니스 로직, 검증 등의 로직 위주 담당
      - ExternalService는 RabbitMQ나 Mail, 외부 API와 같은 외부 시스템 호출 및 사용을 담당

<br/>

- 25/07/23
  - module-product 마무리
    - product 관련 기능 마무리
    - Product 관련 각 서비스, UseCase, Controller에 대한 단위 테스트 및 통합 테스트 완료

<br/>

- 25/07/24
  - module-cart 마무리
    - useCase, service, controller 모두 작성 및 테스트 완료.
    - 주로 사용되는 CartMemberDTO ( userId, CartCookieValue )에 대해서는 api는 DTO 생성을 하지 않고 Cookie, userId만 꺼내서 useCase에 전달하는 구조로 설계
    - 이 처리와 기능 처리 이후 비회원의 장바구니 쿠키 갱신 또는 추가 설정을 위해 CartUtils라는 유틸 클래스를 생성. 해당 유틸 클래스에서 담당 하도록 설계

<br/>

- 25/07/25
  - module-order 마무리
    - module-mypage에 있던 주문 목록 조회를 module-order로 이동.
    - useCase, service, controller 모두 작성 및 테스트 완료.

<br/>

- 25/07/26
  - MemberRepository, AuthRepository module-auth -> module-user 이동
    - module-auth는 완전하게 인증 / 인가에 대한 처리만 담당하는 모듈
    - 기존에는 인증 / 인가 처리 중 MemberRepository가 필요했기 때문에 auth에 배치해야 된다고 생각했으나, interface를 통한 사용이 가능하다는 것을 확인
    - module-auth가 module-user를 참조하지 않기 때문에 구현체가 module-user에 있으면 참조하지 않고 DI가 주입하지 못할것이라고 생각했는데 가능하다는 점을 알게됨
    - module-auth에서는 AuthMemberReader, AuthMemberStore 인터페이스를 갖고 있고, 이 두 인터페이스에 대한 구현체를 module-user에서 작성하는 방법으로 의존성 없이 사용이 가능.
    - 이 개선 사항으로 인해 이전에 module-auth의 MemberReader를 참조하고 사용하던 부분들을 모두 수정. 확인까지 완료

<br/>

- 25/07/27
  - module-mypage 작업 중
    - 이미 작성된 코드를 활용한다는 점에서 다른 모듈들에 대해서는 서비스 -> 컨트롤러 방향으로 작성해 나가며 테스트를 수행했지만, myPage, admin은 자신의 모듈들 내에서만 처리되는 것이 아닌 기능에 따라 다른 모듈로 요청을 보내는 경우가 발생하기 때문에 컨트롤러 -> 서비스 방향으로 탑다운 방식으로 진행.

<br/>

- 25/07/28
  - module-mypage 테스트 제외 마무리.
    - 컨트롤러 통합 테스트는 확인.

<br/>

- 25/07/29
  - module-admin 작업 중
    - 알림, 캐싱 작성 필요

<br/>

- 25/07/30
  - module-admin 테스트 제외 마무리.
    - 컨트롤러 통합 테스트는 확인.
  - 각 모듈의 service, usecase, repository, consumer 및 참조 관계에 대한 다이어그램 작성

<br/>

- 25/07/31
  - 작성한 다이어그램 기반 모듈간 의존성 수정
  - 의존성 수정 이후 테스트에서 문제 발생
    - controller 테스트에서는 문제가 없었으나, 기존에 작성했던 각 모듈 내부의 통합 테스트에서 문제 발생.
    - 이유는 의존하지 않는 모듈에 대한 interface 문제. 
    - Product 모듈에서는 review 처리 과정에서 OrderDetail 데이터의 리뷰 상태값을 수정하는 과정이 포함되어 있음. 하지만 order가 product를 참조해야만 하기 때문에 product가 order를 참조할 수 없음.
    - 이 문제 해결을 위해 interface - impl 구조로 orderDetail 데이터 수정을 처리.
    - 그러나, 컨트롤러 테스트를 담당하는 api 모듈에서는 모든 서비스 모듈을 참조하기 때문에 문제가 발생하지 않았지만, product 모듈 내부의 통합 테스트는 order 모듈에 대한 참조가 없기 때문에 구현체를 찾을 수 없어 No Qualifying bean of type..... 이라는 NoSuchBeanDefinitionException이 발생.
    - 특히 Cache 관련해서 common 모듈에 작성했는데 이 common 모듈을 모든 모듈이 참조하다보니 문제가 더 커짐.
  - 문제 해결 방안
    - 이 의존성 문제를 해결하는 방법은 2가지.
    - 각 테스트 클래스에서 @MokitoBean을 통해 Mocking하는 방법과 module-test로 분리하는 방법
    - mocking을 하기에는 Cart와 같이 캐싱과 관련없는 모듈에서 조차 문제가 발생하므로 모든 테스트 클래스에 대한 Mocking이 필요하다는 문제가 있음.
    - 또한, 점점 인터페이스를 통한 접근이 늘어날수록 Mocking 해야하는 양도 늘어나기 때문에 비효율.
    - 1차 문제 해결로 module-cache로 캐싱 서비스를 분리하는 것이 필요.
    - 이게 완전한 해결책은 아닌게 만약 나중에 장바구니 기능에서도 캐싱이 적용된다면 Cart 모듈의 통합 테스트에서 또 Mocking이 필요하게 됨.
    - 그래서 2차 문제 해결로 각 모듈 내부에는 단위 테스트만 배치. Service, UseCase에 대한 통합 테스트는 module-test 라는 테스트 모듈에서 전부 작성하는 것으로 규칙을 생성.
    - 이유는 어느 모듈에서 캐싱이 적용될지 모르고 캐싱이 아니더라도 테스트에서만 다른 의존성이 필요할 수 있게 될 수 있는데 그때마다 module-test로 테스트 코드를 옮기는 것 보다 처음부터 일관성 있게 module-test에서 작성하는 것이 앞으로의 유지보수 측면에서 더 유리할 것이라고 생각
    - testImplementation을 사용한다는 방법도 있지만, 이 경우 테스트 환경에서의 양방향 참조가 발생할 수 있으므로 제외.

<br/>

- 25/08/01
  - module-test로 테스트 코드 분리 작업
    - admin 관련 usecase들 통합 테스트 분리 및 테스트

<br/>

- 25/08/02
  - module-test로 테스트 코드 분리 작업
    - module-product 관련 usecase 통합 테스트 분리 및 테스트
    - admin 관련 단위 테스트 각 모듈에 작성 및 테스트

<br/>

- 25/08/03
  - module-test로 테스트 코드 분리 작업
    - module-product 관련 단위 테스트 작성 및 테스트

<br/>

- 25/08/04
  - module-test로 테스트 코드 분리 작업
    - module-mypage 단위 테스트 작성 및 테스트
    - module-mypage 통합 테스트 분리 및 테스트

<br/>

- 25/08/05
  - module-test로 테스트 코드 분리 작업
    - module-cart, module-order, module-user 단위 테스트 작성 및 테스트
    - module-cart, module-order, module-user 통합 테스트 분리 및 테스트

<br/>

- 25/08/06
  - module-test로 테스트 코드 분리작업. ( 마무리 )
    - module-notification 단위 테스트 작성 및 테스트
    - module-notification 통합 테스트 분리 및 테스트
  - 전체 테스트 재수행으로 오류 발생하지 않는 것 확인.
  - 문제 발생
    - 테스트 코드에서 수행하는 경우 테스트가 모두 통과하지만 gradlew build로 빌드 시 테스트에서는 오류가 발생.
    - 원인 파악 중.

<br/>

- 25/08/07
  - 문제 원인 파악
    - 의존성이 없거나 의존할 수 없는 구조의 모듈에서 사용해야 할 Repository가 있는 경우 interface - impl로 사용했었으나, 이 부분에서 문제가 발생.
    - 한 예로 auth 모듈에서 MemberRepository를 사용하기 위해 AuthMemberReader, AuthMemberStore라는 인터페이스를 갖고 user 모듈에서 그 구현체를 갖는 구조로 개선했었음.
    - 그러나, 이 경우 api 모듈이나 test 모듈처럼 auth와 user 모듈에 대해 모두 의존성을 갖고 있다면 문제가 되지 않지만 auth 모듈만 놓고 봤을때는 구현체를 전혀 찾을 수 없는 구조.
    - 그렇기 때문에 테스트 클래스 단위로 실행하는 경우 문제가 발생하지 않지만 build 과정에서는 문제가 발생할 수 밖에 없음.
    - 이유는 빌드시 이 프로젝트륾 통째로 한번에 빌드하는 것이 아닌 각 모듈별로 빌드한다음 합치는 개념이기 때문.
    - 그래서 auth 모듈을 빌드할 때 그 구현체 component를 찾을 수 없다는 점 때문에 문제가 발생함.
    - 이런식으로 처리했던 모든 부분들을 개선해야 하기 때문에 대대적인 수정이 필요.
  - 문제 해결
    - 모든 모듈에 대해 Interface - impl 을 통해 다른 모듈에 간접적으로 참조하는 케이스를 분리.
    - 대부분은 api 모듈의 컨트롤러에서 필요한 모듈의 UseCase들을 호출해 조합하는 방식으로 처리했으나, auth와 user의 경우 그렇게 처리할 수 없기 떄문에 auth-api 라는 모듈을 추가.
    - auth-api 모듈에서는 user 모듈이 필요로하는 JWTTokenProvider, JWTTokenService를 갖도록 설계.
    - auth-api 모듈이 추가되면서 user -> auth 였던 의존성 구조를 auth -> user, auth-api로 수정. user 역시 auth-api 참조로 수정.
    - 그럼 user는 auth 모듈에서 필요했던 JWTTokenProvider, JWTTokenService를 auth-api를 통해 사용할 수 있게 되고, auth 역시 user를 참조함으로써 MemberRepository 사용이 가능. JWTTokenProvider와 JWTTokenService 역시 auth-api를 참조하기 때문에 사용이 가능해짐.
    - 이렇게 처리하면서 CustomUser를 통해 처리하던 user 모듈의 로그인 로직에도 약간의 수정이 발생.
    - user 모듈의 UseCase에서는 CustomUser를 직접 사용하는 것이 아닌 api 모듈에서 auth 모듈을 통해 CustomUser 객체를 받아내도록 해 인증 / 인가를 처리하도록 하고
    - user 모듈의 UseCase는 userId만 받아서 JWT를 생성하도록 수정.
    - 비슷한 문제로 Notification과 cache 문제가 있었음.
    - Notification은 불가피하게 NotificationConsumer와 WebSocket을 통해 사용자에게 알림을 반환하는 처리를 api 모듈로 이동.
    - api 모듈에서 WebSocket을 관리해야 하므로 SimpMessagingTemplate을 사용해야 하고 이것 역시 Interface - impl 구조로 처리할 순 없었기 때문에 부득이하게 이동.
    - cache의 경우 의존성 구조와 로직을 수정하는 것으로 해결.
    - 기존에는 필요한 서비스 모듈에서 cache 모듈을 참조하는 구조였지만, interface - impl 구조가 안된다는 시점에서 이런 의존성 구조로는 문제를 해결할 수가 없었음.
    - 그래서 cache 모듈이 각 서비스 모듈을 참조하는 구조로 수정하고 cache 모듈을 참조하는 api 모듈에서 데이터 조회 이전 캐싱 데이터 조회 및 갱신을 우선 수행하도록 수정.

<br/>

- 25/08/08
  - 2차 문제 발생
    - test 모듈에서 RabbitMQ가 호출되는 UseCase의 테스트에서 정상처리 되지 못하는 것을 확인.
    - 이 문제 역시 테스트 클래스 단위로 실행하는 경우에는 문제가 발생하지 않지만 build 를 통한 테스트에서만 문제가 발생.
  - 2차 문제 원인 파악
    - 기존 단일 모듈 모놀리식 구조에서는 build 시에도 정상적으로 처리되었기 때문에 의문이 있었음.
    - 알아보니 단일 모듈에서는 Application context가 하나만 존재하기 때문에 build 테스트에서도 RabbitMQ의 consumer가 동일 트랜잭션에서 동작하는 게 가능했음
    - 반면, 멀티모듈 구조에서는 각 모듈마다 Application Context가 따로 존재하기 때문에 build 테스트에서는 모든 Application Context를 가져오지 않아 발생하는 문제라는 것을 알게 됨.
    - 그래서 결과적으로 테스트 트랜잭션과 consumer의 트랜잭션이 분리되어 마치 @Transactional을 사용했을때처럼 별개의 트랜잭션으로 동작한다는 것이 문제의 원인.
  - 2차 문제 해결
    - 불가파하게 UseCase 통합 테스트에서는 RabbitMQ로 처리되는 부분을 Mocking처리하고 RabbitMQ의 동작은 api 모듈의 컨트롤러 통합 테스트에서 검증하는 방법으로 처리
    - 컨트롤러 통합 테스트는 정상 동작하는 이유가 MockMvc 사용으로 인해 Controller -> Service -> Repository 까지 모든 호출이 같은 트랜잭션 경계 안에서 실행되기 때문.
