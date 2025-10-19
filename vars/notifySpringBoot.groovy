
def call() {
    // Configuration
    def webhookUrl = "http://localhost:8081/api/jenkins-logs/webhook"
    def token = "119b088237bb9828d10be9d85d1470fc49"
    
    // Get Jenkins build information
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def buildStatus = currentBuild.currentResult  // SUCCESS, FAILURE, UNSTABLE, etc.
    
    echo "========================================================"
    echo "Sending webhook to Log Collector"
    echo "========================================================"
    echo "Job Name: ${jobName}"
    echo "Build Number: ${buildNumber}"
    echo "Build Status: ${buildStatus}"
    echo "========================================================"
    
    try {
        // Build the webhook URL with parameters
        def fullUrl = "${webhookUrl}?jobName=${jobName}&buildNumber=${buildNumber}&buildStatus=${buildStatus}&token=${token}"
        
        echo "Webhook URL: ${fullUrl}"
        
        // Detect OS and use appropriate command
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def response
        def httpCode
        def body
        
        if (isWindows) {
            // Windows: use bat() with curl
            echo "Detected Windows OS - using bat()"
            response = bat(
                script: """@echo off
curl -X POST "${fullUrl}" -w "\\n%%{http_code}\"""",
                returnStdout: true
            ).trim()
        } else {
            // Unix/Linux: use sh() with curl
            echo "Detected Unix/Linux OS - using sh()"
            response = sh(
                script: """curl -X POST "${fullUrl}" -w "\\n%{http_code}\"""",
                returnStdout: true
            ).trim()
        }
        
        // Parse response - last line is HTTP code
        def lines = response.split('\n')
        httpCode = lines[-1].trim()
        body = lines.size() > 1 ? lines[0..-2].join('\n') : ""
        
        echo "HTTP Response Code: ${httpCode}"
        echo "Response Body: ${body}"
        
        if (httpCode == '200' || httpCode == '201' || httpCode.toInteger() == 200 || httpCode.toInteger() == 201) {
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
