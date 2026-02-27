@Library('Shared') _

pipeline {
    agent any
    
    triggers {
        githubPush()
    }
    
    environment {
        DOCKER_BACKEND_IMAGE_NAME = 'loicmaxwell/fined-mentor-backend'
        DOCKER_FRONTEND_IMAGE_NAME = 'loicmaxwell/fined-mentor-frontend'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}"
        GITHUB_CREDENTIALS = credentials('github-credentials')
        GIT_BRANCH = "main"
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
        
        stage('Build Docker Images') {
            parallel {
                stage('Build backend Image') {
                    when {
                        environment name: 'BACKEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            docker_build(
                                imageName: env.DOCKER_BACKEND_IMAGE_NAME,
                                imageTag: env.DOCKER_IMAGE_TAG,
                                dockerfile: 'backend/Dockerfile',
                                context: 'backend'
                            )
                        }
                    }
                }
                
                stage('Build frontend Image') {
                    when {
                        environment name: 'FRONTEND_CHANGED', value: 'true'
                    }
                    steps {
                        script {
                            docker_build(
                                imageName: env.DOCKER_FRONTEND_IMAGE_NAME,
                                imageTag: env.DOCKER_IMAGE_TAG,
                                dockerfile: 'frontend/Dockerfile',
                                context: 'frontend'
                            )
                        }
                    }
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
        
        stage('Security Scan with Trivy') {
            steps {
                script {
                    trivy_scan()
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
                        manifestsPath: 'infrastructure',
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
}
