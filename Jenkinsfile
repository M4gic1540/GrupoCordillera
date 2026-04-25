/* groovylint-disable NestedBlockDepth */
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build & Test All') {
            parallel {
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
                stage('authservice') {
                    steps {
                        script {
                            runMaven('authservice')
                            checkTestCount(
                                'authservice/target/surefire-reports/TEST-com.main.authservice.service.AuthServiceTest.xml',
                                20,
                                'AuthServiceTest'
                            )
                            checkTestCount(
                                'authservice/target/surefire-reports/TEST-com.main.authservice.controller.AuthControllerTest.xml',
                                20,
                                'AuthControllerTest'
                            )
                        }
                    }
                }
                stage('data-ingestion-service') {
                    steps {
                        script {
                            runMaven('data-ingestion-service')
                        }
                    }
                }
                stage('gateway') {
                    environment {
                        GATEWAY_LATENCY_THRESHOLD = '3000'
                    }
                    steps {
                        script {
                            sh 'echo "GATEWAY_LATENCY_THRESHOLD=$GATEWAY_LATENCY_THRESHOLD"'
                            runMaven('gateway')
                        }
                    }
                }
                stage('kpi-engine') {
                    steps {
                        script {
                            runMaven('kpi-engine')
                        }
                    }
                }
            }
        }
        // Funciones utilitarias para evitar repetición
        // Ejecuta mvnw clean test en el directorio dado
        // y asegura permisos de ejecución cross-platform
        // Uso: runMaven('authservice')
        //
        // checkTestCount(reportPath, minTests, className)
        // Uso: checkTestCount('path/to/report.xml', 20, 'AuthServiceTest')
        //
        // Estas funciones requieren Jenkins Pipeline: Groovy Sandbox desactivado o aprobadas por admin
        // Si no es posible, dejar la lógica inline como antes
        
        // Declaración de funciones utilitarias
        // (Colocar al inicio del pipeline si Jenkins lo permite)
        // def runMaven(serviceDir) { ... }
        // def checkTestCount(reportPath, minTests, className) { ... }
        stage('SonarQube Analysis') {
            when {
                expression { return false }
            }
            steps {
                echo 'SonarQube analysis deshabilitado. Ejecutar manualmente desde authservice:'
                echo './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar \
  -Dsonar.host.url=http://host.docker.internal:9000 \
  -Dsonar.login=sqp_31d07692fe508884a1822a2b7de6b83c581cebc6'
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
            steps {
                sh 'docker-compose up -d --build'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar, **/target/surefire-reports/*.xml, \
                gateway/test-results/*.xml', allowEmptyArchive: true
            junit testResults: '**/target/surefire-reports/*.xml, gateway/test-results/*.xml', allowEmptyResults: true
        }
        success {
            echo 'Pipeline completado correctamente.'
            // Notificación de ejemplo por Slack (requiere configuración de plugin y credencial)
            slackSend(channel: '#devops', color: 'good', message: "Build exitoso: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
        failure {
            echo 'Pipeline fallo. Revisar logs y reportes de pruebas.'
            slackSend(
                channel: '#devops',
                color: 'danger',
                message: "Build fallido: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            )
        }
    }
}

// Declaración de funciones utilitarias para Groovy Pipeline (fuera del bloque pipeline)
def runMaven(serviceDir) {
    dir(serviceDir) {
        sh 'chmod +x mvnw'
        sh './mvnw -B clean test'
    }
}

def checkTestCount(reportPath, minTests, className) {
    if (!fileExists(reportPath)) {
        error "No se encontró el reporte: ${reportPath}"
    }
    def content = readFile(reportPath)
    def matcher = content =~ /tests="(\d+)"/
    if (!matcher.find()) {
        error "No se pudo extraer el número de tests de ${reportPath}"
    }
    def testCount = matcher[0][1] as int
    echo "${className} tests: ${testCount}"
    if (testCount < minTests) {
        error "Fallo de quality gate: ${className} tiene menos de ${minTests} tests"
    }
}
}
