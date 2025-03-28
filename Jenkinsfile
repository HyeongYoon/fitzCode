pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh """
                    docker cp /home/ubuntu/.env jenkins:/var/jenkins_home/.env
                    cd fitzCode
                    chmod +x gradlew
                    export GRADLE_WRAPPER_DIR=fitzCode/gradle/wrapper
                    ./gradlew build -x test
                """
            }
        }
        stage('Deploy') {
            steps {
                sh """
                    cd fitzCode
                    chmod +x start.sh
                    ./start.sh
                """
            }
        }
    }
}