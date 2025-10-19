def call() {
    // Configuration
    def webhookUrl = "http://localhost:8081/api/jenkins-logs/webhook"
    def token = "119b088237bb9828d10be9d85d1470fc49"
    
    // Get Jenkins build information
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def buildStatus = currentBuild.currentResult
    def duration = currentBuild.durationString
    def timestamp = System.currentTimeMillis()
    def startTime = new Date(currentBuild.startTime).format("yyyy-MM-dd'T'HH:mm:ss.SSS")
    def endTime = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS")
    
    echo "========================================================"
    echo "Sending webhook to Log Collector"
    echo "========================================================"
    echo "Job Name: ${jobName}"
    echo "Build Number: ${buildNumber}"
    echo "Build Status: ${buildStatus}"
    echo "Duration: ${duration}"
    echo "========================================================"
    
    try {
        // Build JSON payload
        def jsonPayload = """
        {
            "jobName": "${jobName}",
            "buildNumber": ${buildNumber},
            "status": "${buildStatus}",
            "duration": "${duration}",
            "timestamp": ${timestamp},
            "startTime": "${startTime}",
            "endTime": "${endTime}",
            "triggeredBy": "Jenkins Webhook",
            "token": "${token}"
        }
        """
        
        echo "JSON Payload: ${jsonPayload}"
        
        // Detect OS and use appropriate command
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def response
        def httpCode
        
        if (isWindows) {
            // Windows: use bat() with curl
            echo "Detected Windows OS - using bat()"
            response = bat(
                script: """@echo off
curl -X POST "${webhookUrl}" ^
  -H "Content-Type: application/json" ^
  -d '${jsonPayload}' ^
  -w "\\n%%{http_code}\"""",
                returnStdout: true
            ).trim()
        } else {
            // Unix/Linux: use sh() with curl
            echo "Detected Unix/Linux OS - using sh()"
            response = sh(
                script: """curl -X POST "${webhookUrl}" \\
  -H "Content-Type: application/json" \\
  -d '${jsonPayload}' \\
  -w "\\n%{http_code}\"""",
                returnStdout: true
            ).trim()
        }
        
        // Parse response
        def lines = response.split('\n')
        httpCode = lines[-1].trim()
        def body = lines.size() > 1 ? lines[0..-2].join('\n') : ""
        
        echo "HTTP Response Code: ${httpCode}"
        echo "Response Body: ${body}"
        
        if (httpCode.toInteger() in [200, 201]) {
            echo "SUCCESS - Webhook sent successfully!"
            return true
        } else {
            echo "WARNING - Webhook returned status code: ${httpCode}"
            return false
        }
        
    } catch (Exception e) {
        echo "ERROR sending webhook: ${e.message}"
        e.printStackTrace()
        return false
    }
}
