pipeline {
    agent any

    // FULL_BUILD라는 Boolean(참/거짓) 파라미터를 추가. true 이면 전체 모듈을 강제로 빌드, false이면 변경된 모듈만 감지
    parameters {
        booleanParam(name: 'FULL_BUILD', defaultValue: false, description: '전체 모듈을 빌드할지 여부')
    }

    // 환경 변수 저장
    environment {
        DOCKER_HUB_USERNAME = 'leesky0075'
        S3_ENV_FILE = "s3://your-bucket-name/env/common.env" // S3 환경 변수 파일 경로
        LOCAL_ENV_FILE = "/tmp/common.env" // 로컬 환경 변수 파일 경로
        EUREKA_SERVER_URL = "http://10.0.1.78:8761/eureka/apps"
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

        // 환경 변수 파일 가져오기
        stage('Download Environment File from S3') {
            steps {
                script {
                    sh """
                    echo "📥 S3에서 환경 변수 파일 다운로드 중..."
                    aws s3 cp ${S3_ENV_FILE} ${LOCAL_ENV_FILE}
                    echo "✅ 환경 변수 파일 다운로드 완료: ${LOCAL_ENV_FILE}"
                    """

                    // .env 파일을 환경변수로 로드
                    sh """
                    export \$(grep -v '^#' ${LOCAL_ENV_FILE} | xargs)
                    """
                }
            }
        }

        // 모듈 변경사항 가져오기
        stage('Detect Changed Modules') {
            steps {
                script {
                    def affectedModules = []

                    if (params.FULL_BUILD) {
                        affectedModules = ["gateway-service", "module-alarm", "module-exchange", "module-member", "module-trip", "module-utility"]
                    } else {
                        def changedFiles = sh(script: "git diff --name-only HEAD^ HEAD", returnStdout: true).trim().split("\n")

                        if (changedFiles.any { it.startsWith("module-utility/") }) {
                            affectedModules.addAll(["gateway-service", "module-alarm", "module-exchange", "module-member", "module-stock", "module-trip"])
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
                            chmod +x ./gradlew
                            ./gradlew clean build -x test
                            docker build --build-arg SERVER_PORT=${SERVER_PORT} -t ${DOCKER_HUB_USERNAME}/${module}:latest .
                            docker push ${DOCKER_HUB_USERNAME}/${module}:latest
                            """
                        }
                    }
                }
            }
        }

        stage('Deploy to ASG') {
            when {
                expression { return !env.AFFECTED_MODULES.trim().isEmpty() }
            }
            steps {
                script {
                    env.AFFECTED_MODULES.split(" ").each { module ->
                        def targetASG = ""

                        if (module == "gateway-service") {
                            targetASG = "api-gateway-asg"
                        } else if (module == "module-alarm") {
                            targetASG = "alarm-service-asg"
                        } else if (module == "module-exchange") {
                            targetASG = "exchange-service-asg"
                        } else if (module == "module-member") {
                            targetASG = "member-service-asg"
                        } else if (module == "module-trip") {
                            targetASG = "trip-service-asg"
                        }
                        echo "🚀 Deploying ${module} to ${targetASG}"

                        sh """
                        INSTANCE_IDS=\$(aws autoscaling describe-auto-scaling-instances --query 'AutoScalingInstances[?AutoScalingGroupName==`${targetASG}`].InstanceId' --output text || true)

                        for instance in \$INSTANCE_IDS; do
                            echo "🔄 인스턴스 \$instance 에 배포 중..."
                            ssh -o StrictHostKeyChecking=no ubuntu@\${instance} '
                                echo "📥 S3에서 환경 변수 파일 다운로드 중..."
                                aws s3 cp ${S3_ENV_FILE} /home/ubuntu/common.env &&
                                echo "✅ 환경 변수 파일 다운로드 완료."

                                echo "🚀 Docker 최신 이미지 다운로드 중..."
                                docker pull \${DOCKER_HUB_USERNAME}/${module}:latest &&

                                echo "🛑 기존 컨테이너 중지 및 제거..."
                                docker stop ${module} || true &&
                                docker rm ${module} || true &&

                                echo "🐳 새 컨테이너 실행..."
                                docker run -d --name ${module} --env-file /home/ubuntu/common.env -p 8080:8080 \${DOCKER_HUB_USERNAME}/${module}:latest
                            '
                        done
                        """

                    }
                }
            }
        }
    }
}
