def call() {
    // Configuration
    def webhookUrl = "http://localhost:8081/api/jenkins-logs/webhook"
    def token = "119b088237bb9828d10be9d85d1470fc49"
    
    // Build info
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def buildStatus = currentBuild.currentResult
    def startTimeMillis = currentBuild.startTimeInMillis ?: System.currentTimeMillis()
    def durationMillis = currentBuild.duration ?: 0
    def endTimeMillis = startTimeMillis + durationMillis

    // Convert millis to ISO-8601 (pour ton LocalDateTime Java)
    def startTime = new Date(startTimeMillis).format("yyyy-MM-dd'T'HH:mm:ss")
    def endTime = new Date(endTimeMillis).format("yyyy-MM-dd'T'HH:mm:ss")

    echo "========================================================"
    echo "Sending webhook to Log Collector"
    echo "Job Name: ${jobName}"
    echo "Build Number: ${buildNumber}"
    echo "Build Status: ${buildStatus}"
    echo "Start Time: ${startTime}"
    echo "End Time: ${endTime}"
    echo "Duration (ms): ${durationMillis}"
    echo "========================================================"

    try {
        // Include new params in URL
        def fullUrl = "${webhookUrl}?jobName=${jobName}&buildNumber=${buildNumber}&buildStatus=${buildStatus}" +
                      "&startTime=${startTime}&endTime=${endTime}&duration=${durationMillis}&token=${token}"
        
        echo "Webhook URL: ${fullUrl}"

        def response = sh(
            script: """curl -X POST "${fullUrl}" -w "\\n%{http_code}" """,
            returnStdout: true
        ).trim()

        def lines = response.split('\n')
        def httpCode = lines[-1].trim()
        echo "HTTP Response Code: ${httpCode}"

        return (httpCode == '200' || httpCode == '201')

    } catch (Exception e) {
        echo "ERROR sending webhook: ${e.message}"
        e.printStackTrace()
        return false
    }
}
