FROM maven:3.9.9-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# 1) Создаем рабочую директорию - папка /app
WORKDIR /app
# 2) копируем помник
COPY pom.xml .
# 3) загружаем зависимости и кешируем
RUN mvn dependency:go-offline
# 4) копируем весь проект
COPY . .

# Теперь внутри есть зависимости, весь проект и мы готовы запускать тесты



USER root

# mvn test -P api
# mvn -DskipTests=true surfire-report:report
# лог выводится не в консоль, а в файл
# bash file
CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile: ${TEST_PROFILE}' ; \
    mvn test -B -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
   } > /app/logs/run.log 2>&1"

