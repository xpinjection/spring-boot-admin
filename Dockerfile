FROM eclipse-temurin:24.0.2_12-jdk-alpine AS builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} boot-admin.jar
RUN java -jar -Djarmode=tools boot-admin.jar extract --layers --destination boot-admin

FROM eclipse-temurin:24.0.2_12-jdk-alpine
COPY --from=builder /boot-admin/dependencies/ ./
COPY --from=builder /boot-admin/snapshot-dependencies/ ./
COPY --from=builder /boot-admin/spring-boot-loader/ ./
COPY --from=builder /boot-admin/application/ ./
ENTRYPOINT ["java", "-jar", "boot-admin.jar"]