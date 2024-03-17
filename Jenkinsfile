pipeline {
    agent any
    environment {
        NEXUS_PUBLISH_CREDS = credentials('nexus-publish-creds')
        NEXUS_PUBLISH_HOST = credentials("nexus-publish-host")
        NEXUS_PUBLISH_PORT = credentials("nexus-publish-port")
    }
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
                    execPattern: 'build/jacoco/test.exec',
                    sourcePattern: '**/src/main/kotlin',
                    sourceInclusionPattern: '**/*.kt'
                )
            }
        }
        stage('Publish') {
            steps {
                echo 'Publishing…'
                sh './gradlew jib'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}