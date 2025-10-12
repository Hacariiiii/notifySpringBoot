// vars/notifySpringBoot.groovy


def call() {
    // URL du webhook Spring Boot
    def webhookUrl = "http://localhost:8081/api/jenkins-logs/webhook"
    def token = "119b088237bb9828d10be9d85d1470fc49"

    // Récupération des infos du build
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def buildStatus = currentBuild.currentResult

    // Affichage dans la console
    echo "Envoi du webhook pour ${jobName} #${buildNumber} (status: ${buildStatus})"

    try {
        // Commande curl pour Windows
        bat """
        curl -X POST "${webhookUrl}?jobName=${jobName}&buildNumber=${buildNumber}&buildStatus=${buildStatus}&token=${token}"
        """
        echo "Webhook appelé avec succès !"
    } catch (Exception e) {
        echo "Erreur lors de l'appel du webhook: ${e.message}"
    }
}
