pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh """
                    # .env 파일을 작업 디렉토리로 복사
                    cp /home/ubuntu/.env /var/jenkins_home/workspace/fitzcode-pipeline/fitzCode/
                    cd fitzCode
                    chmod +x gradlew
                    # .env 파일 로드 확인
                    if [ -f .env ]; then
                        export \$(cat .env | xargs)
                    else
                        echo "ERROR: .env file not found"
                        exit 1
                    fi
                    ./gradlew build -x test
                """
            }
        }
        stage('Deploy') {
            steps {
                sh """
                    cd fitzCode
                    chmod +x start.sh
                    # 기존 컨테이너 및 이미지 제거
                    docker rm -f fitzcode-web || true
                    docker rmi fitzcode-app || true
                    # Docker 이미지 빌드
                    docker build -t fitzcode-app -f Dockerfile .
                    # .env 파일을 컨테이너 내부로 복사하기 위해 임시 컨테이너 실행
                    docker create --name temp-container fitzcode-app
                    docker cp .env temp-container:/app/.env
                    # 임시 컨테이너 삭제
                    docker rm temp-container
                    # 최종 컨테이너 실행
                    docker run -d -p 80:8080 --name fitzcode-web fitzcode-app
                """
            }
        }
    }
}