pipeline {
    agent any

    environment {
        REGISTRY = "leesky0075"
        DOCKER_CREDENTIALS_ID = "docker-hub-credentials"
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'dev', url: 'https://github.com/Trip-Transfer-Trade/backend.git'
            }
        }

        stage('Build with Gradle') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        'gateway-service': 'gateway-service',
                        'member-service': 'module-member',
                        'trip-service': 'module-trip',
                        'exchange-service': 'module-exchange',
                        'alarm-service': 'module-alarm'
                    ]
                    for (service in services.keySet()) {
                        def folder = services[service]
                        sh "docker build -t ${REGISTRY}/${service}:latest -f ${folder}/Dockerfile ${folder}"
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        def services = ['gateway-service', 'member-service', 'trip-service', 'exchange-service', 'alarm-service']
                        for (service in services) {
                            sh "docker push ${REGISTRY}/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy Services') {
            steps {
                sh 'docker-compose down'
                sh 'docker-compose up -d'
            }
        }
    }
}
