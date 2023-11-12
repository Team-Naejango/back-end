#!/usr/bin/env bash

PROJECT_ROOT="/home/ec2-user/action"
JAR_FILE="$PROJECT_ROOT/build/libs/naejango-0.0.1-SNAPSHOT.jar"
APPLICATION_NAME="naejango-0.0.1-SNAPSHOT.jar"
DEPLOY_LOG="$PROJECT_ROOT/log/deploy.log"
APP_LOG="$PROJECT_ROOT/log/application.log"
ERROR_LOG="$PROJECT_ROOT/log/error.log"

TIME_NOW=$(date +%c)

# jar 파일 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

# 현재 구동 중인 애플리케이션 pid 확인
CURRENT_PID=$(pgrep -f $APPLICATION_NAME)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG