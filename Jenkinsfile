@Library('Shared') _

pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }
    
    triggers {
        githubPush()
    }
    
    environment {
        DOCKER_BACKEND_IMAGE_NAME = 'loicmaxwell/fined-mentor-backend'
        DOCKER_FRONTEND_IMAGE_NAME = 'loicmaxwell/fined-mentor-frontend'
        SEMVER = "1.0.0"
        GITHUB_CREDENTIALS = credentials('github-credentials')
        GIT_BRANCH = "main"
        TRIVY_CACHE_DIR = "${WORKSPACE}/.trivy-cache"
    }
    
    stages {
        stage('Cleanup Workspace') {
            steps {
                script {
                    clean_ws()
                }
            }
        }
        
        stage('Clone Repository') {
            steps {
                script {
                    clone("https://github.com/maxwell-tchiabe/fined-mentor.git","main")
                    env.GIT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.DOCKER_IMAGE_TAG = "${env.SEMVER}-${env.GIT_SHA}"
                }
            }
        }
        
        stage('Detect Changes') {
            steps {
                script {
                    // Initialize as mutable env vars (not in environment{} block which is immutable)
                    env.BACKEND_CHANGED  = 'false'
                    env.FRONTEND_CHANGED = 'false'
                    detect_changes()
                }
            }
        }
        
        stage('Run Unit Tests') {
            steps {
                script {
                    run_tests()
                }
            }
        }
        
        stage('SonarQube Analysis') {
            when {
                environment name: 'BACKEND_CHANGED', value: 'true'
            }
            steps {
                script {
                    sonar_scan(
                        projectKey:  'Fined-Mentor',
                        projectName: 'Fined Mentor',
                        sonarServer: 'sonarqube',
                        sonarToken:  'sonarqube-token'
                    )
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Security Scan - Code & Dependencies') {
            steps {
                script {
                    trivy_fs_scan()
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build backend Image') {
                    when {
                        environment name: 'BACKEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            docker.withRegistry('', 'docker-hub-credentials') {
                                docker_build(
                                    imageName: env.DOCKER_BACKEND_IMAGE_NAME,
                                    imageTag: env.DOCKER_IMAGE_TAG,
                                    dockerfile: 'backend/Dockerfile',
                                    context: 'backend'
                                )
                                
                                env.BACKEND_IMAGE = "${env.DOCKER_BACKEND_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}"
                            }
                        }
                    }
                }
                
                stage('Build frontend Image') {
                    when {
                        environment name: 'FRONTEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            docker.withRegistry('', 'docker-hub-credentials') {
                                docker_build(
                                    imageName: env.DOCKER_FRONTEND_IMAGE_NAME,
                                    imageTag: env.DOCKER_IMAGE_TAG,
                                    dockerfile: 'frontend/Dockerfile',
                                    context: 'frontend'
                                )
                                
                                env.FRONTEND_IMAGE = "${env.DOCKER_FRONTEND_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}"
                            }
                        }
                    }
                }
            }
        }
        
        stage('Security Scan - Docker Images') {
            parallel {
                stage('Scan Backend Image') {
                    when {
                        environment name: 'BACKEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            trivy_image_scan(
                                imageName: env.BACKEND_IMAGE,
                                cacheDir: env.TRIVY_CACHE_DIR
                            )
                        }
                    }
                }
                
                stage('Scan Frontend Image') {
                    when {
                        environment name: 'FRONTEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            trivy_image_scan(
                                imageName: env.FRONTEND_IMAGE,
                                cacheDir: env.TRIVY_CACHE_DIR
                            )
                        }
                    }
                }
            }
        }
        
        stage('Push Docker Images') {
            parallel {
                stage('Push Backend Image') {
                    when {
                        environment name: 'BACKEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            docker_push(
                                imageName: env.DOCKER_BACKEND_IMAGE_NAME,
                                imageTag: env.DOCKER_IMAGE_TAG,
                                credentials: 'docker-hub-credentials'
                            )
                        }
                    }
                }
                
                stage('Push Frontend Image') {
                    when {
                        environment name: 'FRONTEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            docker_push(
                                imageName: env.DOCKER_FRONTEND_IMAGE_NAME,
                                imageTag: env.DOCKER_IMAGE_TAG,
                                credentials: 'docker-hub-credentials'
                            )
                        }
                    }
                }
            }
        }
        
        stage('Update Kubernetes Manifests') {
            steps {
                script {
                    update_k8s_manifests(
                        imageTag: env.DOCKER_IMAGE_TAG,
                        manifestsPath: 'infrastructure/ingress',
                        gitCredentials: 'github-credentials',
                        gitUserName: 'maxwell-tchiabe',
                        gitUserEmail: 'maxwelltchiabe@gmail.com',
                        updateBackend: env.BACKEND_CHANGED == 'true',
                        updateFrontend: env.FRONTEND_CHANGED == 'true'
                    )
                }
            }
        }
    }
    
    post {
        always {
            script {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports',
                    reportFiles: 'trivy-*.html',
                    reportName: 'Trivy Security Reports'
                ])
                
                archiveArtifacts artifacts: 'reports/*.json', allowEmptyArchive: true
            }
        }
        failure {
            script {
                echo "Security scan failed! Check the reports for details."
            }
        }
    }
}