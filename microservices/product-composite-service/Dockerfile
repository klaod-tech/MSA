# ★ 변경 ① (교재: eclipse-temurin:24_36-jre-noble)
FROM eclipse-temurin:21-jre-noble AS builder
WORKDIR /extracted
ADD ./build/libs/*.jar app.jar
# ★ 변경 ② (교재: RUN java -Djarmode=layertools -jar app.jar extract)
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination layers

# ★ 변경 ① (교재: eclipse-temurin:24_36-jre-noble)
FROM eclipse-temurin:21-jre-noble
WORKDIR /application
# ★ 변경 ③ 아래 4줄 경로에 layers/ 추가 (교재: /extracted/dependencies/ ...)
COPY --from=builder /extracted/layers/dependencies/ ./
COPY --from=builder /extracted/layers/spring-boot-loader/ ./
COPY --from=builder /extracted/layers/snapshot-dependencies/ ./
COPY --from=builder /extracted/layers/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]