def call(Map config = [:]) {
    // 1. Parameters receive kar rahe hain (agar koi pass na kare toh error aayega ya default set hoga)
    def imageFullName = config.imageFullName ?: error("Full image name (e.g., wajahathr/django-notes-app) is required")
    def imageTag = config.imageTag ?: 'latest'
    def manifestsPath = config.manifestsPath ?: 'kubernetes' 
    def deploymentFile = config.deploymentFile ?: 'deployment.yaml'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitRepoUrl = config.gitRepoUrl ?: error("Git repository URL is required")
    def gitBranch = config.gitBranch ?: 'main'
    
    echo "Updating Kubernetes manifest in ${manifestsPath}/${deploymentFile} with image: ${imageFullName}:${imageTag}"
    
    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        sh """
            # Git configure karna
            git config user.name "Jenkins CI"
            git config user.email "jenkins@cicd.com"
            
            # Sed command ab totally dynamic hai. Yeh aapke image name ko dhoondh kar uska tag update karegi.
            sed -i "s|image: ${imageFullName}:.*|image: ${imageFullName}:${imageTag}|g" ${manifestsPath}/${deploymentFile}
            
            # Check karte hain ke yaml file mein waqai koi change aaya hai ya nahi
            if git diff --quiet; then
                echo "No changes to commit. Manifest is already up to date."
            else
                echo "Changes detected. Committing and pushing back to GitHub..."
                git add ${manifestsPath}/${deploymentFile}
                git commit -m "Auto-update image tag to ${imageTag} [ci skip]"
                
                # Git URL se 'https://' hata kar usme credentials inject kar rahe hain taake secure push ho sake
                REPO_URL_WITHOUT_HTTPS=\$(echo ${gitRepoUrl} | sed 's|https://||')
                git remote set-url origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@\${REPO_URL_WITHOUT_HTTPS}
                
                # Changes ko wapas repo par push karna
                git push origin HEAD:${gitBranch}
            fi
        """
    }
}