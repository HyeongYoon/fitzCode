pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh """
                    cd fitzCode
                    chmod +x gradlew
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