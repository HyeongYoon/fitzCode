pipeline {
    agent any
    stages {
        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/HyeongYoon/fitzCode.git'
            }
        }
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