metadata {
    definition (name: "Pool Aqualink", namespace: "aqualink", author: "MC") {
        capability "Initialize"
        capability "Switch"
        
        attribute "contact", "string"
        attribute "session_id", "string"
        attribute "auth_token", "string"
        attribute "user_id", "string"
        
        attribute "label", "string"
        attribute "status", "string"
        
        command "GetSerial", ["String"]
        command "Update", ["String"]
    }

    preferences {
        section("Device Settings:") {
            input "serial_number", "string", title:"Pool Serial Number", description: "", required: true, displayDuringSetup: true
            input "email", "string", title:"Userid - Email", description: "", required: true, displayDuringSetup: true
            input "password", "string", title:"User Password", description: "", required: true, displayDuringSetup: true
            input "devicenum", "string", title:"Device Number", description: "", required: true, displayDuringSetup: true
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
        }
    }


}

void installed() {
    log.warn "..."
    initialize()
}
def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

void parse(String description) {
    

}

void Update(){
    updateinfo()
}
void updated() {
    log.info "updated..."
    initialize()
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
    
    if (autoUpdate) runIn(1800, Updateinfo)
}

void uninstalled() {

}

void initialize() {
    
    LoginGetSession()
    updatedeviceinfo()
    

}

void Updateinfo(){
    if(!session_id) {
        LoginGetSession()
    }
    updatedeviceinfo()
    
    if (autoUpdate) runIn(1800, Updateinfo)
}

void updatedeviceinfo(){
    
    devices = GetDevices()
    
    switch (devicenum){
        case '1':
            status = devices.devices_screen[5].aux_1[0].state
            label = devices.devices_screen[5].aux_1[1].label
        break
        case '2':
            status = devices.devices_screen[5].aux_2[0].state
            label = devices.devices_screen[5].aux_2[1].label
        break
        case '3':
            status = devices.devices_screen[5].aux_3[0].state
            label = devices.devices_screen[5].aux_3[1].label
        break
        case '4': //level
            status = devices.devices_screen[5].aux_4[0].state
            label = devices.devices_screen[5].aux_4[1].label
        break
        case '5': //level
            status = devices.devices_screen[5].aux_5[0].state
            label = devices.devices_screen[5].aux_5[1].label
        break
        case '6': //level
            status = devices.devices_screen[5].aux_6[0].state
            label = devices.devices_screen[5].aux_6[1].label
        break
    }
    
    sendEvent(name: "label", value: "${label}")
    sendEvent(name: "status", value: "${status}")
    device.updateSetting("status", [value: "${status}", type: "string"])
    
    if(status == "1") {
        sendEvent(name: "switch", value: "on")        
    } else {
        sendEvent(name: "switch", value: "off")
    }
    
}

def LoginGetSession(){
    
    def wxURI = "https://support.iaqualink.com/users/sign_in.json"
    
	def requestParams =
	[
		uri:  wxURI,
		requestContentType: "application/json",
        body: """{"api_key": "EOOEMOW4YR6QNB07", "email": "${email}", "password": "${password}"}"""
	]

	httpPost(requestParams)
	{
	  response ->
		if (response?.status == 200)
		{
            updatedeviceinfo
            if (logEnable) response.data.authentication_token
            if (logEnable) response.data.session_id
            
            session_id = response.data.session_id
            auth_token = response.data.authentication_token
            user_id = response.data.id
			return response.data.session_id
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
}


void on(){
    
    if(!session_id) {
        LoginGetSession()
    }
    
    updatedeviceinfo()
    log.info "  ${status}"
    if(status=='0'){
        if (logEnable) log.info "Toggle Device ON  ${status}"
        OperateDevice(devicenum)
        sendEvent(name: "switch", value: "on")        
    } else {
        if (logEnable) log.info "Device Already ON  ${status}"
    }
}

void off(){
    if(!session_id) {
        LoginGetSession()
    }
    updatedeviceinfo()
    if(status=='1'){
        if (logEnable) log.info "Toggle Device OFF  ${status}"
        OperateDevice(devicenum)
        sendEvent(name: "switch", value: "off")        
    } else {
        if (logEnable) log.info "Device Already OFF  ${status}"
    }
}

void OperateDevice(String num){
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=set_aux_${num}&serial=${serial_number}&sessionID=${session_id}"
    //log.info wxURI2
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
}


void GetSerial() {
    
    if(!session_id) {
        LoginGetSession()
    }
    serial_number = GetSerialfromAqualink()
    sendEvent(name: "serial", value: "${serial_number}")
    
}

def GetSerialfromAqualink() {
    
    def wxURI2 = "https://support.iaqualink.com//devices.json?api_key=EOOEMOW4YR6QNB07&authentication_token=${auth_token}&user_id=${user_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
            serial_number = response.data.serial_number[0]
            
            
            device.updateSetting("serial_number", [value: "${serial_number}", type: "string"])
            
			return serial_number
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    
}

def GetDevices() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=get_devices&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    
}
