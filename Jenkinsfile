pipeline {
    agent any

    environment {
        REGISTRY = "leesky0075"
        DOCKER_CREDENTIALS_ID = "docker-hub-credentials"
    }

    stages {
        stage('Clone Repository') {
            steps {
                git 'https://github.com/Trip-Transfer-Trade/backend.git'
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
                    def services = ['api-gateway', 'member-service', 'trip-service', 'exchange-service', 'alarm-service']
                    for (service in services) {
                        sh "docker build -t ${REGISTRY}/${service}:latest -f ${service}/Dockerfile ."
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        def services = ['api-gateway', 'member-service', 'trip-service', 'exchange-service', 'alarm-service']
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
