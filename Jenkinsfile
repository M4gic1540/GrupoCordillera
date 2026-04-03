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
            steps {
                script {
                    def sonarUrl = 'http://172.17.0.3:9000'
                    def sonarUser = 'admin'
                    def sonarPassword = 'admin'
                    
                    // Analizar authservice
                    dir('authservice') {
                        sh """
                            /opt/sonar-scanner/bin/sonar-scanner \
                              -Dsonar.projectKey=grupo-cordillera-authservice \
                              -Dsonar.projectName='Grupo Cordillera - Auth Service' \
                              -Dsonar.sources=src/main \
                              -Dsonar.host.url=${sonarUrl} \
                              -Dsonar.login=${sonarUser} \
                              -Dsonar.password=${sonarPassword}
                        """
                    }
                    
                    // Analizar data-ingestion-service
                    dir('data-ingestion-service') {
                        sh """
                            /opt/sonar-scanner/bin/sonar-scanner \
                              -Dsonar.projectKey=grupo-cordillera-data-ingestion \
                              -Dsonar.projectName='Grupo Cordillera - Data Ingestion' \
                              -Dsonar.sources=src/main \
                              -Dsonar.host.url=${sonarUrl} \
                              -Dsonar.login=${sonarUser} \
                              -Dsonar.password=${sonarPassword}
                        """
                    }
                    
                    // Analizar kpi-engine
                    dir('kpi-engine') {
                        sh """
                            /opt/sonar-scanner/bin/sonar-scanner \
                              -Dsonar.projectKey=grupo-cordillera-kpi-engine \
                              -Dsonar.projectName='Grupo Cordillera - KPI Engine' \
                              -Dsonar.sources=src/main \
                              -Dsonar.host.url=${sonarUrl} \
                              -Dsonar.login=${sonarUser} \
                              -Dsonar.password=${sonarPassword}
                        """
                    }
                }
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
                sh 'if command -v docker-compose >/dev/null 2>&1; then docker-compose up -d --build; else docker compose up -d --build; fi'
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
