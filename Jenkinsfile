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
                    execPattern: 'build/jacoco/test.exec',
                    sourcePattern: '**/src/main/kotlin',
                    sourceInclusionPattern: '**/*.kt'
                )
            }
        }
        stage('Publish') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-publish-creds',
                    usernameVariable: 'NEXUS_PUBLISH_USERNAME',
                    passwordVariable: 'NEXUS_PUBLISH_PASSWORD'
                )]) {
                    echo "Publishing to ${NEXUS_PUBLISH_HOST} using ${NEXUS_PUBLISH_USERNAME}//${NEXUS_PUBLISH_PASSWORD}…"
                    sh './gradlew jib'
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}