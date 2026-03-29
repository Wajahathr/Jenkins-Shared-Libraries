# Jenkins Shared Library 🚀

This repository contains reusable Jenkins pipeline functions (Shared Libraries) to make writing Declarative Pipelines faster, cleaner, and DRY (Don't Repeat Yourself).

## 📦 Available Functions

Here is the list of all available functions in this library and how to call them in your `Jenkinsfile`.

1. Source Code Management

**`code_checkout(String GitUrl, String GitBranch)`**
Pulls code from the specified Git repository.
```groovy
code_checkout('[https://github.com/your-username/your-repo.git](https://github.com/your-username/your-repo.git)', 'main')

2. Docker Operations
_________________________________________________________________

docker_build(Map config)
Builds a Docker image.

docker_build(
    imageName: 'your-dockerhub-user/app-name', 
    imageTag: 'latest', // Optional, defaults to 'latest'
    dockerfile: 'Dockerfile', // Optional
    context: '.' // Optional
)
___________________________________________________________________

docker_push(Map config)
Pushes the built image to Docker Hub.

docker_push(
    imageName: 'your-dockerhub-user/app-name', 
    imageTag: 'latest',
    credentials: 'docker-hub-credentials-id'
)
____________________________________________________________________

docker_compose()
Deploys the application using docker compose (downs old containers and brings up new ones in detached mode).

docker_compose()
_____________________________________________________________________

3. Security & Code Quality (DevSecOps)

______________________________________________________________________

sonarqube_analysis(String SonarQubeAPI, String Projectname, String ProjectKey)
Runs SonarQube code quality analysis. (Requires SonarQube environment configured in Jenkins).

sonarqube_analysis('sonar-server', 'MyProject', 'my-project-key')

_______________________________________________________________________

trivy_scan()
Scans the file system for vulnerabilities using Trivy.

trivy_scan()

_______________________________________________________________________

owasp_dependency()
Runs OWASP Dependency-Check to find vulnerable dependencies.

owasp_dependency()

________________________________________________________________________

4. Kubernetes Operations
update_k8s_manifests(Map config)
Updates the image tag in Kubernetes deployment YAML files and pushes the changes back to GitHub.

update_k8s_manifests(
    imageFullName: 'your-dockerhub-user/app-name',
    imageTag: env.BUILD_NUMBER,
    manifestsPath: 'k8s', 
    deploymentFile: 'deployment.yaml',
    gitCredentials: 'github-credentials-id',
    gitRepoUrl: '[https://github.com/your-username/your-repo.git](https://github.com/your-username/your-repo.git)'
)

_________________________________________________________________________

5. Utility
clean_ws()
Cleans up the Jenkins workspace.

clean_ws()

__________________________________________________________________________

🚀 Quick Starter Template (Copy & Paste)

If you are starting a new project with build, push, test, deploy just copy this Jenkinsfile template to your application's root directory, change the environment variables, and you are good to go!

@Library('NAME') _

pipeline {
    agent any  OR agent { label 'AGENT-NAME' }
    
    environment {
        GIT_URL = 'GIT-REPO-URL'
        GIT_BRANCH = 'Branch-Name'
        DOCKERHUB_CREDENTIALS = 'ID' // Your credentials ID in Jenkins
        IMAGE_NAME = 'DOCKER-USERNAME/your-new-app'
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