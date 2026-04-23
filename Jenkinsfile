pipeline {
    agent any

    tools {
        maven 'maven3'
    }

    environment {
        SCANNER_HOME   = tool 'sonar-scanner'
        IMAGE_TAG      = "v${BUILD_NUMBER}"
        AWS_ACCOUNT_ID = credentials('aws-account-id')
        AWS_REGION     = 'ap-south-1'
        ECR_REGISTRY   = "${AWS_ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com"
        ECR_REPO       = 'medibot'
        IMAGE_FULL     = "${AWS_ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/medibot"
    }

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'master',
                    credentialsId: 'git',
                    url: 'https://github.com/saikiranpi/medibot-app.git'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Trivy FS Scan') {
            steps {
                sh 'trivy fs --format table -o trivy-fs-report.html .'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '''
                        $SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectKey=medibot \
                        -Dsonar.projectName=medibot \
                        -Dsonar.java.binaries=target
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: false,
                                       credentialsId: 'sonar-token'
                }
            }
        }

        stage('Build JAR') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Publish to Nexus') {
            steps {
                withMaven(globalMavenSettingsConfig: 'medibot-nexus',
                          maven: 'maven3',
                          traceability: true) {
                    sh 'mvn deploy -DskipTests'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${ECR_REPO}:${IMAGE_TAG} ."
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh "trivy image --format table -o trivy-image-report.html ${ECR_REPO}:${IMAGE_TAG}"
            }
        }

        stage('Push to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                   credentialsId: 'aws-creds']]) {
                    sh '''
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}

                        docker tag ${ECR_REPO}:${IMAGE_TAG} ${IMAGE_FULL}:${IMAGE_TAG}
                        docker push ${IMAGE_FULL}:${IMAGE_TAG}

                        docker tag ${ECR_REPO}:${IMAGE_TAG} ${IMAGE_FULL}:latest
                        docker push ${IMAGE_FULL}:latest
                    '''
                }
            }
        }

        stage('Update Manifests Repo') {
            steps {
                script {
                    cleanWs()
                    withCredentials([usernamePassword(
                            credentialsId: 'git',
                            usernameVariable: 'GIT_USER',
                            passwordVariable: 'GIT_PASS')]) {
                        sh '''
                            git clone https://${GIT_USER}:${GIT_PASS}@github.com/saikiranpi/medibot-manifests.git
                            cd medibot-manifests

                            sed -i "s|image:.*medibot:.*|image: ${IMAGE_FULL}:${IMAGE_TAG}|g" deployment.yaml

                            git config user.name "Jenkins"
                            git config user.email "jenkins@medibot.com"
                            git add deployment.yaml
                            git commit -m "Update image to ${IMAGE_TAG} [build #${BUILD_NUMBER}]"
                            git push origin master
                        '''
                    }
                }
            }
        }
    }
}
