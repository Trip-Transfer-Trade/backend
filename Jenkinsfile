pipeline {
    agent any

    // FULL_BUILD라는 Boolean(참/거짓) 파라미터를 추가. true 이면 전체 모듈을 강제로 빌드, false이면 변경된 모듈만 감지
    parameters {
        booleanParam(name: 'FULL_BUILD', defaultValue: false, description: '전체 모듈을 빌드할지 여부')
    }

    // 환경 변수 저장
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
                    checkout scm   // Jenkins가 Git 정보를 자동으로 가져옴
                }
            }
        }

        // 모듈 변경사항 가져오기
        stage('Detect Changed Modules') {
            steps {
                script {
                    def affectedModules = []

                    if (params.FULL_BUILD) {
                        affectedModules = ["gateway-service", "module-alarm", "module-exchange", "module-member", "module-trip"]
                    } else {
                        def changedFiles = sh(script: "git diff --name-only HEAD^ HEAD", returnStdout: true).trim().split("\n")

                        if (changedFiles.any { it.startsWith("module-utility/") }) {
                            affectedModules.addAll(["gateway-service", "module-alarm", "module-exchange", "module-member", "module-trip"])
                        }
                        if (changedFiles.any { it.startsWith("gateway-service/") }) {
                            affectedModules.add("gateway-service")
                        }
                        if (changedFiles.any { it.startsWith("module-alarm/") }) {
                            affectedModules.add("module-alarm")
                        }
                        if (changedFiles.any { it.startsWith("module-exchange/") }) {
                            affectedModules.add("module-exchange")
                        }
                        if (changedFiles.any { it.startsWith("module-member/") }) {
                            affectedModules.add("module-member")
                        }
                        if (changedFiles.any { it.startsWith("module-trip/") }) {
                            affectedModules.add("module-trip")
                        }
                    }

                    env.AFFECTED_MODULES = affectedModules.unique().join(" ")
                    if (env.AFFECTED_MODULES.trim().isEmpty()) {
                        currentBuild.result = 'SUCCESS'
                        echo "✅ 변경된 모듈이 없어 빌드 및 배포를 건너뜁니다."
                        return
                    }
                }
            }
        }

        stage('Build & Push Docker Images') {
            when {
                expression { return !env.AFFECTED_MODULES.trim().isEmpty() }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_HUB_USERNAME', passwordVariable: 'DOCKER_HUB_PASSWORD')]) {
                    script {
                        sh "echo \${DOCKER_HUB_PASSWORD} | docker login -u \${DOCKER_HUB_USERNAME} --password-stdin"

                        env.AFFECTED_MODULES.split(" ").each { module ->
                            sh """
                            echo ">>> Building ${module}"
                            cd ${module} || exit 1
                            pwd  # 현재 디렉토리 출력
                            ls -al  # Dockerfile 및 build/libs/*.jar 파일 확인

                            chmod +x ./gradlew
                            ./gradlew clean build -x test

                            echo "🔍 Checking if Dockerfile exists..."
                            if [ ! -f Dockerfile ]; then
                                echo "❌ Error: Dockerfile not found in ${module}"
                                exit 1
                            fi

                            echo "✅ Dockerfile found! Starting build..."
                            docker build -t ${DOCKER_HUB_USERNAME}/${module}:latest -f Dockerfile .
                            docker push ${DOCKER_HUB_USERNAME}/${module}:latest
                            """
                        }
                    }
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
                        "module-alarm": "alarm",
                        "module-exchange": "exchange",
                        "module-member": "member",
                        "module-trip": "trip"
                    ]

                    // ✅ 각 모듈별 포트 설정
                    def portMap = [
                        "gateway-service": "8085:8085",
                        "module-alarm": "8084:8084",
                        "module-exchange": "8083:8083",
                        "module-member": "8081:8081",
                        "module-trip": "8082:8082"
                    ]


                    // ✅ 각 모듈별 Private IP 매핑
                    def ipMap = [
                        "gateway-service": "10.0.10.28",
                        "module-alarm": "10.0.10.148",
                        "module-exchange": "10.0.10.223",
                        "module-member": "10.0.10.140",
                        "module-trip": "10.0.10.225"
                    ]


                    env.AFFECTED_MODULES.split(" ").each { module ->
                        def targetServer = serverMap[module]
                        def modulePort = portMap[module] ?: "8080:8080"  // 포트가 없으면 기본값
                        def moduleIp = ipMap[module] ?: "127.0.0.1"  // 기본값을 localhost로 설정

                        if (!targetServer) {
                            echo "❌ Error: ${module}에 대한 배포 대상 서버가 설정되지 않았습니다."
                            return
                        }

                        echo "🚀 Deploying ${module} to ${targetServer} on port ${modulePort}..."

                        sh """
                        ssh ${targetServer} '
                            echo "📥 Downloading environment file from S3..."
                            aws s3 cp s3://my-ttt-env/common.env /home/ubuntu/common.env;
                            chmod 600 /home/ubuntu/common.env

                            echo "🔄 Loading environment variables..."
                                set -a
                                source /home/ubuntu/common.env
                                set +a

                            echo "🔄 Stopping and removing existing ${module} container..."
                            sudo docker stop ${module} || true
                            sudo docker rm ${module} || true

                            echo "🚀 Running ${module} container..."
                            sudo docker run -d --name ${module} \\
                                --network=bridge \\
                                -e DB_HOST=\${DB_HOST} \\
                                -e DB_PORT=\${DB_PORT} \\
                                -e DB_NAME=\${DB_NAME} \\
                                -e DB_USERNAME=\${DB_USERNAME} \\
                                -e DB_PASSWORD=\${DB_PASSWORD} \\
                                -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=\${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE} \\
                                -e MODULE_INSTANCE_IP_ADDRESS=${moduleIp} \\
                                -p ${modulePort} \\
                                leesky0075/${module}:latest
                        '
                        """
                    }
                }
            }
        }



   }

}
