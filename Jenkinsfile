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
                    execPattern: 'build/jacoco/test.exec'
                    classPattern: '**/classes/kotlin/main',
                    sourcePattern: '**/src/main/kotlin',
                    sourceInclusionPattern: '**/*.kt'
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