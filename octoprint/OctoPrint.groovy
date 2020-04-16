metadata {
    definition (name: "OctoPrint", namespace: "OctoPrint", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/octoprint/OctoPrint.groovy") {
        capability "Initialize"
        capability "Switch"
        capability "PresenceSensor"
        
        attribute "state", "enum", ["Operational", "Printing", "Pausing","Paused", "Cancelling", "Error", "Offline"]
        attribute "completion", "string"
        attribute "printTimeLeft", "string"
        
        command "GetJob", null
        command "Print", null
        command "Restart", null
        command "Cancel", null
        command "Pause", null
        command "Resume", null
        command "PauseToggle", null
    }

    preferences {
        section("Device Settings:") {
            input "ip_addr", "string", title:"ip address", description: "", required: true, displayDuringSetup: true
            input "url_port", "string", title:"tcp port", description: "", required: true, displayDuringSetup: true, defaultValue: "80"
            input "api_key", "string", title:"API Key", description: "", required: true, displayDuringSetup: true, defaultValue: ""
            
            input "timedelay", "number", title:"Number of seconds before checking Job Status", description: "", required: true, displayDuringSetup: true, defaultValue: "600"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto Updating of Job Status", defaultValue: true
        }
    }


}        
void parse(String toparse){
    if (logEnable) log.info toparse
}
void initialize(){
    if (autoUpdate) runIn(timedelay.toInteger(), CheckJob)
}

def CheckJob() {
    GetJob()

    if (autoUpdate) runIn(timedelay.toInteger(), CheckJob)
}

def GetJob() {
    
    def wxURI2 = "http://${ip_addr}:${url_port}/api/job"
    def toReturn = " "
        
    def requestParams2 =
	[
		uri:  wxURI2,
        headers: [ 
                   "User-Agent": "Wget/1.20.1",
                   Accept: "*/*",
                   "Accept-Encoding": "identity",
                   Host: "${ip_addr}",
                   Connection: "Keep-Alive",
                   "X-Api-Key": "${api_key}",
                 ],
	]
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{

			if (response.data.progress.completion != null)
            		{
                		sendEvent(name: "completion", value: response.data.progress.completion)
            		}
            		if (response.data.progress.printTimeLeft != null)
            		{
            			sendEvent(name: "printTimeLeft", value: response.data.progress.printTimeLeft/60 )
            		}
 		        sendEvent(name: "state", value: response.data.state)
            
			toReturn = response.data.toString() 
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    } catch (Exception e){
        log.info e
        toReturn = e.toString()
    }
    
    return toReturn
}

def Print(){
    SendCommand("{ \"command\": \"start\" }")
} 
def Cancel(){
    SendCommand("{ \"command\": \"cancel\" }")
} 
def Restart(){
    SendCommand("{ \"command\": \"restart\" }")
} 
def Pause(){
    SendCommand('{"command":"pause","action":"pause"}')
} 
def Resume(){
    SendCommand('{"command":"pause","action":"resume"}')
} 
def PauseToggle(){
    SendCommand('{"command":"pause","action":"toggle"}')
}

def SendCommand(String payload) {
    def headers = [:] 
    headers.put("HOST", "${ip_addr}:${url_port}")
    headers.put("Content-Type", "application/json")
    headers.put("X-Api-Key", "${api_key}")
    
    try {
        def hubAction = new hubitat.device.HubAction(
            method: "POST",
            path: "/api/job",
            body: payload,
            headers: headers
            )
        //log.debug hubAction
        return hubAction
    }
    catch (Exception e) {
        log.debug "runCmd hit exception ${e} on ${hubAction}"
    }  
}
