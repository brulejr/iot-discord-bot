pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out…'
                checkout scm
                sh 'ls -lat'
            }
        }
        stage('Build') {
            steps {
                echo 'Building…'
                sh './gradlew clean build'
                junit "**/build/test-results/test/*.xml"
                jacoco(
                    execPattern: 'build/jacoco/jacoco.exec'
                )
            }
        }
        stage('Test') {
            steps {
                echo 'Testing…'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}