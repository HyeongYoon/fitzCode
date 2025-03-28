pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh """
                    # fitzCode 디렉토리 생성
                    mkdir -p /var/jenkins_home/workspace/fitzcode-pipeline/fitzCode/
                    # .env 파일 복사
                    if [ -f /home/ubuntu/.env ]; then
                        cp /home/ubuntu/.env /var/jenkins_home/workspace/fitzcode-pipeline/fitzCode/
                    else
                        echo "ERROR: /home/ubuntu/.env file not found"
                        exit 1
                    fi
                    cd fitzCode
                    chmod +x gradlew
                    if [ -f .env ]; then
                        export \$(cat .env | xargs)
                    else
                        echo "ERROR: .env file not found in fitzCode directory"
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
                    docker rm -f fitzcode-web || true
                    docker rmi fitzcode-app || true
                    docker build -t fitzcode-app -f Dockerfile .
                    docker create --name temp-container fitzcode-app
                    docker cp .env temp-container:/app/.env
                    docker rm temp-container
                    docker run -d -p 80:8080 --name fitzcode-web --env-file .env fitzcode-app
                """
            }
        }
    }
}