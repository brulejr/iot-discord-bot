pipeline {
    agent any
    environment {
        NEXUS_PUBLISH_CREDS = credentials('nexus-publish-creds')
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
                echo "Publishing build ${BUILD_NUMBER} to ${NEXUS_PUBLISH_HOST}…"
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