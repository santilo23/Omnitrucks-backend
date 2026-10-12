# ==============================================================================
# ETAPA 1: BUILDER (Compilación y Empaquetado)
# ==============================================================================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

# 1. Copiar wrapper y pom primero para cachear dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 2. Copiar código fuente y compilar el JAR de producción sin tests (los tests ya corren en el CI)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# ETAPA 2: RUNTIME (Producción mínima, segura y sin compilador)
# ==============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime

# Crear usuario y grupo no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar el JAR generado en la etapa anterior
COPY --from=builder /build/target/omnitrucks-*.jar app.jar

# Asignar permisos al usuario sin privilegios
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Parámetros optimizados para contenedor
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
