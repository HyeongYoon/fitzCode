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
                sh 'cd fitzCode'
                sh 'chmod +x start.sh'
                sh './start.sh'
            }
        }
    }
}
