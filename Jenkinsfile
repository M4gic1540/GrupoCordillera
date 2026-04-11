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
            JWT_SECRET = 'local_test_secret_12345678901234567890123456789012'
        BOOTSTRAP_ADMIN_TOKEN = 'local_test_bootstrap_token'
    // Solo si usas perfil postgres:
    // DB_URL = 'jdbc:postgresql://localhost:5432/auth_db'
    // DB_USERNAME = 'postgres'
    // DB_PASSWORD = 'tu_password_postgres'
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

                stage('Authservice Test Minimum Per Class') {
            steps {
                sh '''#!/bin/bash
set -euo pipefail

auth_service_report="authservice/target/surefire-reports/TEST-com.main.authservice.service.AuthServiceTest.xml"
auth_controller_report="authservice/target/surefire-reports/TEST-com.main.authservice.controller.AuthControllerTest.xml"

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

auth_service_count=$(extract_tests "$auth_service_report")
auth_controller_count=$(extract_tests "$auth_controller_report")

echo "AuthServiceTest tests: $auth_service_count"
echo "AuthControllerTest tests: $auth_controller_count"

if [[ "$auth_service_count" -lt 20 ]]; then
    echo "Fallo de quality gate: AuthServiceTest tiene menos de 20 tests"
    exit 1
fi

if [[ "$auth_controller_count" -lt 20 ]]; then
    echo "Fallo de quality gate: AuthControllerTest tiene menos de 20 tests"
    exit 1
fi

echo "Quality gate OK: cada clase objetivo tiene al menos 20 tests"
'''
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

        stage('Gateway Test Suite') {
            steps {
                dir('gateway') {
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
                echo 'SonarQube analysis disabled. Run manually from authservice: ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar -Dsonar.host.url=http://host.docker.internal:9000 -Dsonar.login=sqp_31d07692fe508884a1822a2b7de6b83c581cebc6'
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
        }
        failure {
            echo 'Pipeline fallo. Revisar logs y reportes de pruebas.'
        }
    }
}
