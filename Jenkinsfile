pipeline {
    agent any

    parameters {
        booleanParam(name: 'FULL_BUILD', defaultValue: false, description: '전체 모듈을 빌드할지 여부')
    }

    environment {
        DOCKER_HUB_USERNAME = 'leesky0075'
    }

    triggers {
        githubPush()   // Github Webhook 트리거
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    checkout scm
                }
            }
        }


        stage('Deploy to EC2') {
            when {
                expression { return !env.AFFECTED_MODULES.trim().isEmpty() }
            }
            steps {
                script {
                    def serverMap = [
                        "gateway-service": "api-gateway",
                        "eureka-server": "eureka",
                        "module-alarm": "alarm",
                        "module-exchange": "exchange",
                        "module-member": "member",
                        "module-trip": "trip"
                    ]

                    def ipMap = [
                        "gateway-service": "10.0.10.28",
                        "eureka-server": "10.0.10.28",
                        "module-alarm": "10.0.10.148",
                        "module-exchange": "10.0.10.223",
                        "module-member": "10.0.10.140",
                        "module-trip": "10.0.10.225"
                    ]

                    env.AFFECTED_MODULES.split(" ").each { module ->
                        def targetServer = serverMap[module]
                        def moduleIp = ipMap[module] ?: "127.0.0.1"

                        if (!targetServer) {
                            echo "❌ Error: ${module}에 대한 배포 대상 서버가 설정되지 않았습니다."
                            return
                        }

                        sh """
                            whoami
                        """
                        echo "🚀 Deploying ${module} to ${targetServer} (IP: ${moduleIp})..."

                        sh """
                            ssh ${module} <<EOF
                                set -e

                                echo "📥 Downloading environment file from S3..."
                                aws s3 cp s3://my-ttt-env/common.env /home/ubuntu/common.env || {
                                    echo "❌ 환경 변수 파일 다운로드 실패"; exit 1;
                                }
                                chmod 600 /home/ubuntu/common.env

                                echo "🔄 Stopping and removing existing ${module} container..."
                                if sudo docker inspect ${module} >/dev/null 2>&1; then
                                    sudo docker stop ${module} || true
                                    sudo docker rm ${module} || true
                                fi

                                echo "📂 Updating ${module} using docker-compose..."
                                docker-compose --env-file /home/ubuntu/common.env pull
                                docker-compose --env-file /home/ubuntu/common.env up -d ${module}

                                echo "🧹 Cleaning up unused Docker images..."
                                docker image prune -a -f
                            EOF
                        """

                    }
                }
            }
        }
    }
}
