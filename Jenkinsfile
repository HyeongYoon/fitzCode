pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew build -x test'
            }
        }
        stage('Deploy') {
            steps {
                sh 'chmod +x start.sh'
                sh './start.sh'
            }
        }
    }
}
