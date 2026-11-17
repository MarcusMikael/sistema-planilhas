FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre

# O contêiner é sempre produção: PostgreSQL, sem dados de demonstração e com SDPP_JWT_SECRET obrigatório.
# Sem isto, esquecer a variável no Render subiria o perfil dev (H2, contas "sdpp123", segredo JWT conhecido).
ENV SPRING_PROFILES_ACTIVE=prod

# Datas no horário de Brasília. O Render roda em UTC (3 h à frente): sem isto, entre 21h e meia-noite o
# "hoje" do sistema já seria o dia seguinte (prazos de ciclo e de clima, datas de conclusão, ações atrasadas).
ENV TZ=America/Sao_Paulo

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# ---------- Inicialização rápida (o plano gratuito do Render desliga o serviço sem uso) ----------
# 1. Extrai o jar em camadas: carregar classes de um diretório é mais rápido que de um jar dentro de outro jar.
# 2. Treino de CDS (Class Data Sharing): sobe o contexto do Spring uma vez durante o build e grava as classes
#    carregadas em application.jsa; nas próximas partidas a JVM as mapeia direto da memória.
#    O treino usa H2 em memória (não há banco no build) e encerra assim que o contexto fica pronto.
RUN java -Djarmode=tools -jar app.jar extract --destination application \
    && rm app.jar \
    && java -XX:ArchiveClassesAtExit=application/application.jsa \
        -Dspring.context.exit=onRefresh \
        -Dspring.datasource.url="jdbc:h2:mem:cds;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE" \
        -Dspring.datasource.driver-class-name=org.h2.Driver \
        -Dspring.datasource.username=sa -Dspring.datasource.password= \
        -Dsdpp.jwt.secret=apenas-treino-cds-durante-o-build-0123456789 \
        -Dsdpp.seed.demo=false -Dserver.port=0 \
        -jar application/app.jar

# Ajustes para pouca CPU e memória (Render gratuito: 512 MB):
# - SerialGC e TieredStopAtLevel=1: menos trabalho de GC e compilação na partida;
# - MaxRAMPercentage=75: usa a memória do contêiner sem estourar o limite.
# Pode ser sobrescrito pela variável JAVA_OPTS no Render.
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=75"

# Executa sem privilégios de root
RUN useradd --system --no-create-home sdpp
USER sdpp

EXPOSE 8080

# O fuso também vai direto para a JVM (fora de JAVA_OPTS, para não se perder se a variável for trocada no Render)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=America/Sao_Paulo -XX:SharedArchiveFile=application/application.jsa -jar application/app.jar"]
