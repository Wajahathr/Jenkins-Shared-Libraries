**TEMPLATES**

1. Docker Operations
docker_build(Map config)
Builds a Docker image.

Groovy
docker_build(
    imageName: 'your-dockerhub-user/app-name', 
    imageTag: 'latest', // Optional, defaults to 'latest'
    dockerfile: 'Dockerfile', // Optional
    context: '.' // Optional
)
docker_push(Map config)
Pushes the built image to Docker Hub.

Groovy
docker_push(
    imageName: 'your-dockerhub-user/app-name', 
    imageTag: 'latest',
    credentials: 'docker-hub-credentials-id'
)
docker_compose()
Deploys the application using docker compose (downs old containers and brings up new ones in detached mode).

Groovy
docker_compose()


2. Security & Code Quality (DevSecOps)
sonarqube_analysis(String SonarQubeAPI, String Projectname, String ProjectKey)
Runs SonarQube code quality analysis. (Requires SonarQube environment configured in Jenkins).

Groovy
sonarqube_analysis('sonar-server', 'MyProject', 'my-project-key')
trivy_scan()
Scans the file system for vulnerabilities using Trivy.

Groovy
trivy_scan()
owasp_dependency()
Runs OWASP Dependency-Check to find vulnerable dependencies.

Groovy
owasp_dependency()


3. Kubernetes Operations
update_k8s_manifests(Map config)
Updates the image tag in Kubernetes deployment YAML files and pushes the changes back to GitHub.

Groovy
update_k8s_manifests(
    imageFullName: 'your-dockerhub-user/app-name',
    imageTag: env.BUILD_NUMBER,
    manifestsPath: 'k8s', 
    deploymentFile: 'deployment.yaml',
    gitCredentials: 'github-credentials-id',
    gitRepoUrl: '[https://github.com/your-username/your-repo.git](https://github.com/your-username/your-repo.git)'
)
4. Utility
clean_ws()
Cleans up the Jenkins workspace.

**EXAMPLE**

@Library('NAME') _

pipeline {
    agent any // OR agent { label 'AGENT-NAME' }
    
    environment {
        // ✏️ UPDATE THESE VARIABLES FOR YOUR NEW PROJECT
        GIT_URL = 'YOUR GIT-REPO URL'
        GIT_BRANCH = 'BRANCH'
        DOCKERHUB_CREDENTIALS = 'ID' // Your credentials ID in Jenkins
        IMAGE_NAME = 'DOCKER_USER/IMAGE-NAME'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout Code') {
            steps {
                code_checkout(env.GIT_URL, env.GIT_BRANCH)
            }
        }

        /* ----- DEVSECOPS STAGES (Uncomment if needed) -----
        stage('SonarQube Analysis') {
            steps {
                sonarqube_analysis('sonar-server', 'YourApp', 'your-app-key')
            }
        }
        
        stage('Trivy File Scan') {
            steps {
                trivy_scan()
            }
        }
        --------------------------------------------------- */

        stage('Build Docker Image') {
            steps {
                docker_build(imageName: env.IMAGE_NAME, imageTag: env.IMAGE_TAG)
            }
        }

        stage('Push to Docker Hub') {
            steps {
                docker_push(
                    imageName: env.IMAGE_NAME, 
                    imageTag: env.IMAGE_TAG, 
                    credentials: env.DOCKERHUB_CREDENTIALS
                )
            }
        }

        stage('Deploy Application') {
            steps {
                docker_compose()
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up workspace and docker environment..."
            clean_ws()
            sh "docker logout || true"
            sh "docker image prune -f || true"
        }
        success {
            echo "✅ Pipeline successfully completed!"
        }
        failure {
            echo "❌ Pipeline failed. Please check the Jenkins console output."
        }
    }
}