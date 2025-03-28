pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
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
