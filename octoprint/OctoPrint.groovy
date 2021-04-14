metadata {
    definition (name: "OctoPrint", namespace: "OctoPrint", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/octoprint/OctoPrint.groovy") {
        capability "Actuator"
		capability "Initialize"
        //capability "Switch"
        capability "PresenceSensor"
        
        attribute "state", "enum", ["Operational", "Printing", "Pausing", "Paused", "Cancelling", "Error", "Offline", "Disconnected"]
        attribute "stateMessage", "string"
        attribute "completion", "string"
        attribute "printTimeLeft", "string"
        attribute "printTime", "string"
        attribute "estimatedPrintTime", "string"
        attribute "name", "string"
        attribute "user", "string"
		attribute "lastPrinterCheck", "String"
        
		
		// attributes for temperature
		attribute "bed-actual", "number"
		attribute "bed-offset", "number"
		attribute "bed-target", "number"
		// primary extruder
		attribute "tool0-actual", "number"
		attribute "tool0-offset", "number"
		attribute "tool0-target", "number"
		// additional extruders (if available on printer)
		attribute "tool1-actual", "number"
		attribute "tool1-offset", "number"
		attribute "tool1-target", "number"
		attribute "tool2-actual", "number"
		attribute "tool2-offset", "number"
		attribute "tool2-target", "number"
        
        
        command "CheckPrinter", null
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
            
            input "delayCheckIdle", "number", title:"Number of seconds between checking printer while idle", description: "", required: true, displayDuringSetup: true, defaultValue: "600"
            input "delayCheckPrinting", "number", title:"Number of seconds between checking printer while printing", description: "After a print, this short delay will be used to refresh the printer data until the Primary Extruder (tool0) is less than 50C", required: true, displayDuringSetup: true, defaultValue: "60"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto Updating of Printer Status", defaultValue: true
        }
    }


}        
void parse(String toparse){
    if (logEnable) log.info "Parsing: ${toparse}"
}

void initialize(){
	state.isPrinting = false
	unschedule()
    if (autoUpdate) runIn(1, CheckPrinter)
}

def updated(){
	state.isPrinting = false
	unschedule()
    if (autoUpdate) runIn(5, CheckPrinter)
}

def CheckPrinter() {
	unschedule(CheckPrinter)
	GetPrinter()
}

def GetPrinter() {
	
	def nowDay = new Date().format("MMM dd", location.timeZone)
	def nowTime = new Date().format("h:mm:ss a", location.timeZone)
	sendEvent(name: "lastPrinterCheck", value: nowDay + " at " + nowTime, displayed: false)	
    
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
				def stateName,msg
				if(response.data.state){
					if(response.data.state.contains(" (")){
						def rSplit = response.data.state.split(' \\(', 2)
						stateName = rSplit[0]
						msg = rSplit[1]
					} else {
						stateName = response.data.state
					}
				}
				if(state.state == null || state.state != stateName){
					sendEvent(name: "state", value: stateName)
					state.state = stateName
				}
				if (msg != "" && msg != null && msg.charAt(msg.length() - 1) == ')') {
					msg = msg.substring(0, msg.length() - 1);
				} else {
					msg = "none"
				}
				if(device.currentValue("stateMessage") != msg){
					sendEvent(name: "stateMessage", value: msg)
				}
				if(state.state != null && ["Printing", "Pausing", "Paused", "Cancelling"].contains(state.state)){
					state.isPrinting = true
				} else {
					state.isPrinting = false
				}
				
				if(state.state != null && ["Offline", "Disconnected"].contains(state.state)){
					state.printerConnected = false
					sendEvent(name: "presence", value: "not present")
				} else {
					state.printerConnected = true
					sendEvent(name: "presence", value: "present")
				}				
				
				if (state.isPrinting && response.data.progress.completion != null)
					{
						//state.completion = response.data.progress.completion
						sendEvent(name: "completion", value: response.data.progress.completion)
					} else {
						sendEvent(name: "completion", value: 0 )
					}
				if (state.isPrinting && response.data.progress.printTimeLeft != null)
					{
						//state.printTimeLeft = response.data.progress.printTimeLeft/60
						sendEvent(name: "printTimeLeft", value: response.data.progress.printTimeLeft/60 )
					} else {
						sendEvent(name: "printTimeLeft", value: 0 )
					}
				if (state.isPrinting && response.data.progress.printTime != null)
					{
						//state.printTime = response.data.progress.printTime/60
						sendEvent(name: "printTime", value: response.data.progress.printTime/60 )
					} else {
						sendEvent(name: "printTime", value: 0 )
					}
				
				if (state.isPrinting && response.data.job.estimatedPrintTime != null)
					{
						//state.estimatedPrintTime = response.data.job.estimatedPrintTime/60
						sendEvent(name: "estimatedPrintTime", value: response.data.job.estimatedPrintTime/60 )
					} else {
						sendEvent(name: "estimatedPrintTime", value: 0 )
					}
				
				if (state.isPrinting && response.data.job.file.name != null)
					{
						//state.name = response.data.job.file.name
						sendEvent(name: "name", value: response.data.job.file.name )
					} else {
						sendEvent(name: "name", value: "none" )
					}
				if (response.data.job.user != null)
					{
						//state.user = response.data.job.user
						sendEvent(name: "user", value: response.data.job.user )
					} else {
						sendEvent(name: "user", value: "none" )
					}
				
				if (logEnable) log.info response.data
				toReturn = response.data.toString()


				// check printer temperatures after successful return of printer job details
				if(state.printerConnected){
					GetPrinterTemp()
				}
			}
			else
			{
				log.warn "${response?.status}"
				sendEvent(name: "stateMessage", value: "${response?.status}")
				// set default status for values
				PrinterNotResponding()
			}
		}
    } catch (Exception e){
        log.info e
        toReturn = e.toString()
		sendEvent(name: "stateMessage", value: toReturn)
		// set default status for values
		PrinterNotResponding()
    }


	// run fast check if state.isPrinting == true or if tool0-actual > 50  (extruder 1 is over 50C)
	unschedule(CheckPrinter)
	def currentToolTemp = device.currentValue("tool0-actual")?.toInteger() ?: 0
	if(state.isPrinting || currentToolTemp > 50){
		if (autoUpdate) runIn(delayCheckPrinting.toInteger(), CheckPrinter)
	} else {
		if (autoUpdate) runIn(delayCheckIdle.toInteger(), CheckPrinter)
	}
    return toReturn
}

def PrinterNotResponding(){
	sendEvent(name: "presence", value: "not present")
	state.printerConnected = false
	sendEvent(name: "state", value: "Disconnected")
	//sendEvent(name: "stateMessage", value: "none")
	state.state = "Disconnected"
	state.isPrinting = false
	sendEvent(name: "completion", value: 0 )
	sendEvent(name: "printTimeLeft", value: 0 )
	sendEvent(name: "printTime", value: 0 )
	sendEvent(name: "estimatedPrintTime", value: 0 )
	sendEvent(name: "name", value: "none" )
	sendEvent(name: "user", value: "none" )
}

def GetPrinterTemp(){
    if (logEnable) log.debug "GetPrinterTemp"
    def wxURI2 = "http://${ip_addr}:${url_port}/api/printer"
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
    asynchttpGet('GetPrinterTempReturn', requestParams2)
}

def GetPrinterTempReturn(response, data) {
	if (logEnable) log.debug "GetPrinterTempReturn"
	//log.debug response.getJson().temperature
	def R = response.getJson()
	def value = ""
	def event
	
	// just processing temperature values
	if(R.temperature != null){
		R.temperature.each{ t ->
			//log.debug t
			if(t != null){
				//log.debug t.getValue()
				t.getValue().each { i ->
					//log.debug "${t.getKey()}-${i}"
					value = "${t.getKey()}-${i}"
					
					event = value.split("=")
					
					//log.debug event[0]
					//log.debug event[1]
					
					sendEvent(name: event[0], value: event[1] )
				}
			}
		}
	}
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