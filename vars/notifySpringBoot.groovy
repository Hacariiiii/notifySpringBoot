def call() {
    // Configuration
    def webhookUrl = "http://localhost:8081/api/jenkins-logs/webhook"
    def token = "119b088237bb9828d10be9d85d1470fc49"
    
    // Get Jenkins build information
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def buildStatus = currentBuild.currentResult  // SUCCESS, FAILURE, UNSTABLE, etc.
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📤 Sending webhook to Log Collector"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Job Name: ${jobName}"
    echo "Build Number: ${buildNumber}"
    echo "Build Status: ${buildStatus}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    try {
        // Build the webhook URL with parameters
        def fullUrl = "${webhookUrl}?jobName=${jobName}&buildNumber=${buildNumber}&buildStatus=${buildStatus}&token=${token}"
        
        // Execute curl command (works on Windows and Unix)
        def response = sh(
            script: """curl -X POST "${fullUrl}" -w "\\n%{http_code}" """,
            returnStdout: true
        ).trim()
        
        // Parse response
        def lines = response.split('\n')
        def httpCode = lines[-1]
        def body = lines[0..-2].join('\n')
        
        if (httpCode == '200' || httpCode == '201') {
            echo "✅ Webhook sent successfully!"
            echo "Response: ${body}"
            return true
        } else {
            echo "⚠️ Webhook returned status code: ${httpCode}"
            echo "Response: ${body}"
            return false
        }
        
    } catch (Exception e) {
        echo "❌ Error sending webhook: ${e.message}"
        echo "Stack trace: ${e.printStackTrace()}"
        return false
    }
}
