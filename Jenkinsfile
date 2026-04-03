pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(name: 'DEPLOY_STACK', defaultValue: false, description: 'Levantar stack con Docker Compose al final del pipeline')
    }

    environment {
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=false'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test authservice') {
            steps {
                dir('authservice') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B clean test'
                }
            }
        }

        stage('Build & Test data-ingestion-service') {
            steps {
                dir('data-ingestion-service') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B clean test'
                }
            }
        }

        stage('Build & Test kpi-engine') {
            steps {
                dir('kpi-engine') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B clean test'
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { return false }
            }
            steps {
                echo 'SonarQube analysis skipped - auth issues. Enable in next iteration.'
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker build -t grupocordillera/authservice:latest ./authservice'
                sh 'docker build -t grupocordillera/data-ingestion-service:latest ./data-ingestion-service'
                sh 'docker build -t grupocordillera/kpi-engine:latest ./kpi-engine'
            }
        }

        stage('Deploy Stack') {
            when {
                expression { return params.DEPLOY_STACK }
            }
            steps {
                sh 'docker-compose up -d --build'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar, **/target/surefire-reports/*.xml', allowEmptyArchive: true
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo 'Pipeline completado correctamente.'
        }
        failure {
            echo 'Pipeline fallo. Revisar logs y reportes de pruebas.'
        }
    }
}
