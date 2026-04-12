pipeline {

    stages {
            steps {
                sh '''#!/bin/bash

auth_service_report="authservice/target/surefire-reports/TEST-com.main.authservice.service.AuthServiceTest.xml"

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test All') {
            parallel {
                stage('authservice') {
                    stages {
                        stage('Build & Test') {
                            steps {
                                dir('authservice') {
                                    sh 'chmod +x mvnw'
                                    sh './mvnw -B clean test'
                                }
                            }
                        }
                        stage('Test Minimum Per Class') {
                            steps {
                                sh '''#!/bin/bash
set -euo pipefail


        stage('Build Docker Images') {

if [[ ! -f "$auth_service_report" ]]; then
    echo "No se encontro el reporte: $auth_service_report"
    exit 1
fi

if [[ ! -f "$auth_controller_report" ]]; then
    echo "No se encontro el reporte: $auth_controller_report"
    exit 1
fi

extract_tests() {
    local file="$1"
    grep -o 'tests="[0-9]*"' "$file" | head -1 | sed 's/[^0-9]//g'
}

            steps {
                script {

                    docker.withRegistry('', 'dockerhub-credentials-id') {
                        docker.build('grupocordillera/authservice:latest', './authservice').push()

if [[ "$auth_service_count" -lt 20 ]]; then
    echo "Fallo de quality gate: AuthServiceTest tiene menos de 20 tests"
    exit 1
fi

if [[ "$auth_controller_count" -lt 20 ]]; then
    echo "Fallo de quality gate: AuthControllerTest tiene menos de 20 tests"
    exit 1
fi

                        docker.build('grupocordillera/data-ingestion-service:latest', './data-ingestion-service').push()
'''
                            }
                        }
                    }
                }
                stage('data-ingestion-service') {
                    steps {
                        dir('data-ingestion-service') {
                            sh 'chmod +x mvnw'
                            sh './mvnw -B clean test'
                        }
                    }
                }
                stage('gateway') {
                    steps {
                        dir('gateway') {
                            sh 'chmod +x mvnw'
                            sh './mvnw -B clean test'
                        }
                    }
                }
                stage('kpi-engine') {
                    steps {
                        dir('kpi-engine') {
                            sh 'chmod +x mvnw'
                            sh './mvnw -B clean test'
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { return false }
            }
            steps {
                echo 'SonarQube analysis disabled. Run manually from authservice: ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar -Dsonar.host.url=http://host.docker.internal:9000 -Dsonar.login=sqp_31d07692fe508884a1822a2b7de6b83c581cebc6'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    docker.withRegistry('', 'dockerhub-credentials-id') {
                        docker.build('grupocordillera/authservice:latest', './authservice').push()
                        docker.build('grupocordillera/data-ingestion-service:latest', './data-ingestion-service').push()
                        docker.build('grupocordillera/kpi-engine:latest', './kpi-engine').push()
                    }
                }
            }
        }

        stage('Deploy Stack') {
            when {
                expression { return false }
            }
            steps {
                echo 'Deploy Stack disabled - nginx.conf mount issues. Configure docker-compose paths manually.'
            }
        }
    }
                        docker.build('grupocordillera/kpi-engine:latest', './kpi-engine').push()
                    }
                }
            }
        }

        stage('Deploy Stack') {
            when {
                expression { return false }
            }
            steps {
                echo 'Deploy Stack disabled - nginx.conf mount issues. Configure docker-compose paths manually.'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar, **/target/surefire-reports/*.xml, gateway/test-results/*.xml', allowEmptyArchive: true
            junit testResults: '**/target/surefire-reports/*.xml, gateway/test-results/*.xml', allowEmptyResults: true
        }
        success {
            echo 'Pipeline completado correctamente.'
            // Notificación de ejemplo por Slack (requiere configuración de plugin y credencial)
            slackSend (channel: '#devops', color: 'good', message: "Build exitoso: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
        failure {
            echo 'Pipeline fallo. Revisar logs y reportes de pruebas.'
            slackSend (channel: '#devops', color: 'danger', message: "Build fallido: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
    }
}
