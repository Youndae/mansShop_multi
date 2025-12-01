FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY module-admin/build.gradle module-admin/build.gradle
COPY module-api/build.gradle module-api/build.gradle
COPY module-auth/build.gradle module-auth/build.gradle
COPY module-auth-api/build.gradle module-auth-api/build.gradle
COPY module-cache/build.gradle module-cache/build.gradle
COPY module-cart/build.gradle module-cart/build.gradle
COPY module-common/build.gradle module-common/build.gradle
COPY module-config/build.gradle module-config/build.gradle
COPY module-file/build.gradle module-file/build.gradle
COPY module-mypage/build.gradle module-mypage/build.gradle
COPY module-notification/build.gradle module-notification/build.gradle
COPY module-order/build.gradle module-order/build.gradle
COPY module-product/build.gradle module-product/build.gradle
COPY module-test/build.gradle module-test/build.gradle
COPY module-user/build.gradle module-user/build.gradle

RUN ./gradlew dependencies --no-daemon || true

COPY .. .

RUN ./gradlew :module-api:bootJar --no-daemon -x test

FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/module-api/build/libs/module-api-*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]