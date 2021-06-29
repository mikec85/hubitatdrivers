metadata {
    definition (name: "Pool Aqualink Parent", namespace: "aqualink", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/aqualink/aqualink-driver-parent.groovy") {
        capability "Initialize"
        capability "Switch"
        
        attribute "contact", "string"
        attribute "session_id", "string"
        attribute "auth_token", "string"
        attribute "user_id", "string"
	attribute "api_url", "string"    
         
        attribute "label", "string"
        attribute "status", "string"
        attribute "aqualink_status", "string"
        attribute "subtype", "string"
        attribute "devtype", "string"
        
        command "GetSerial", null
        command "Update", null
        command "DeleteChild", ["String"]
        command "TogglePoolPump", null
        command "GetOneTouch", null
        command "SetOneTouch", ["String"]
	    
        command "SetHeater", [[name: "Pool or SPA", type:"ENUM", constraints:["spa","pool"]],
                              [name: "Temperature", type:"STRING", description: "Enter the Temperature"]]
        
    }

    preferences {
        section("Device Settings:") {
            input "serial_number", "string", title:"Pool Serial Number", description: "", required: true, displayDuringSetup: true
            input "email", "string", title:"Userid - Email", description: "", required: true, displayDuringSetup: true
            input "password", "string", title:"User Password", description: "", required: true, displayDuringSetup: true
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
            input name: "aux_EA", type: "bool", title: "Enable AUX EA Port", defaultValue: false
            input name: "SpaTemp", type: "bool", title: "Enable Spa Temperature", defaultValue: false
            input name: "AirTemp", type: "bool", title: "Enable Air Temperature", defaultValue: false
            input name: "SpaPump", type: "bool", title: "Enable Spa Pump", defaultValue: false
            input name: "SpaHeater", type: "bool", title: "Enable Spa Heater", defaultValue: false
            input name: "PoolHeater", type: "bool", title: "Enable Pool Heater", defaultValue: false
            input name: "SolarHeater", type: "bool", title: "Enable Solar Heater", defaultValue: false
	    input name: "OneTouch", type: "bool", title: "Enable OneTouch", defaultValue: false
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

def get_aqualink_status(){
    //log.info device.currentValue("aqualink_status")
    return device.currentValue("aqualink_status")
}

void DeleteChild(String device){
    deleteChildDevice(device)
   
}
void Update(){
    Updateinfo()
}
void updated() {
    log.info "updated..."
    initialize()
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
    
    if (autoUpdate) runIn(3600, Updateinfo)
}

void uninstalled() {
    String thisId = device.id
    deleteChildDevice("${thisId}-Pool_Temp")
    deleteChildDevice("${thisId}-Spa_Temp")
    deleteChildDevice("${thisId}-Air_Temp")
    deleteChildDevice("${thisId}-Spa_Pump")
    deleteChildDevice("${thisId}-OneTouch_1")
    deleteChildDevice("${thisId}-OneTouch_2")
    deleteChildDevice("${thisId}-OneTouch_3")
    deleteChildDevice("${thisId}-OneTouch_4")
    deleteChildDevice("${thisId}-OneTouch_5")
    deleteChildDevice("${thisId}-OneTouch_6")
	
}

void initialize() {
    device.updateSetting("api_url", "https://p-api.iaqualink.net/v1/mobile/session.json")
    
    LoginGetSession()
    CreateChildren()
    updatedeviceinfo()

}

def CreateChild2(String Unit, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
        cd = addChildDevice( "Pool Aqualink Child", "${thisId}-${Unit}", [name: "${label}", isComponent: true])
    }
    return cd 
}

def CreateChildTemp(String Unit, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
        cd = addChildDevice( "Pool Aqualink Child Temps", "${thisId}-${Unit}", [name: "${label}", isComponent: true])
    }
    return cd 
}

def CreateChildHeater(String Unit, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
        cd = addChildDevice( "Pool Aqualink Child Heater", "${thisId}-${Unit}", [name: "${label}", isComponent: true])
    }
    return cd 
}
def CreateChildOneTouch(String Unit, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
        cd = addChildDevice( "Pool Aqualink Child OneTouch", "${thisId}-${Unit}", [name: "${label}", isComponent: true])
    }
    return cd 
}

void updatecolor(){
    device.updateSetting("colorlightenable", [value: true, type: "bool"])
    device.updateSetting("devtype", [value: "2", type: "String"])
}
void updatedevtype(String devtype){
    device.updateSetting("devtype", [value: devtype, type: "String"])
}
void CreateChild(String Unit, String label, String devtype){
    
    if("AUX${Unit}" != label && label != "AUX-11") {
        if(devtype == "2") {
            //Unit = "${Unit}-Color"
            cd = CreateChild2(Unit, label)
            cd.updatecolor()
        } else {
            cd = CreateChild2(Unit, label)
            cd.updatedevtype(devtype)
            //cd.removeSetting("serial_number")
        }
        if (logEnable) log.info "Unit is ${Unit}   DeviceType is ${devtype}"
    }
    
}
void CreateChildren(){
   
    devices = GetDevices()
    CreateChild("1", devices.devices_screen[3].aux_1[1].label, devices.devices_screen[3].aux_1[3].type )
    try { CreateChild("2", devices.devices_screen[4].aux_2[1].label, devices.devices_screen[4].aux_2[3].type ) } catch (Exception e) { if (logEnable) log.warn "CreateChild 2 failed: ${e.message}" }
    try { CreateChild("3", devices.devices_screen[5].aux_3[1].label, devices.devices_screen[5].aux_3[3].type ) } catch (Exception e) { if (logEnable) log.warn "CreateChild 3 failed: ${e.message}" }
    try { CreateChild("4", devices.devices_screen[6].aux_4[1].label, devices.devices_screen[6].aux_4[3].type ) } catch (Exception e) { if (logEnable) log.warn "CreateChild 4 failed: ${e.message}" }
    try { CreateChild("5", devices.devices_screen[7].aux_5[1].label, devices.devices_screen[7].aux_5[3].type ) } catch (Exception e) { if (logEnable) log.warn "CreateChild 5 failed: ${e.message}" }
    try { CreateChild("6", devices.devices_screen[8].aux_6[1].label, devices.devices_screen[8].aux_6[3].type ) } catch (Exception e) { if (logEnable) log.warn "CreateChild 6 failed: ${e.message}" }
    try { CreateChild("7", devices.devices_screen[9].aux_7[1].label, devices.devices_screen[9].aux_7[3].type ) } catch (Exception e) { if (logEnable) log.warn "CreateChild 7 failed: ${e.message}" }
    if (aux_EA) { CreateChild("8", devices.devices_screen[10].aux_EA[1].label, devices.devices_screen[10].aux_EA[3].type ) } 
    CreateChild("FilterPump", "FilterPump", "0")
    
    CreateChildTemp("Pool_Temp", "Pool Temp")
    if(SpaTemp){
        CreateChildTemp("Spa_Temp", "Spa Temp" )
    }
    if(AirTemp){
        CreateChildTemp("Air_Temp", "Air Temp" )
    }
    if(SpaPump){
        CreateChild("Spa_Pump", "Spa Pump", "0" )
    }
    if(SpaHeater){
        CreateChildHeater("Spa_Heater", "Spa Heater")
    }
    if(PoolHeater){
        CreateChildHeater("Pool_Heater", "Pool Heater" )
    }
    if(SolarHeater){
        CreateChildHeater("Solar_Heater", "Solar Heater" )
    }
    if(OneTouch){
        log.info "create onetouch"
        onetouch = GetOneTouch()
        log.info onetouch.onetouch_screen.onetouch_1[2][2].label
        CreateChildOneTouch("OneTouch_1", onetouch.onetouch_screen.onetouch_1[2][2].label )
        CreateChildOneTouch("OneTouch_2", onetouch.onetouch_screen.onetouch_2[3][2].label )
        CreateChildOneTouch("OneTouch_3", onetouch.onetouch_screen.onetouch_3[4][2].label )
        CreateChildOneTouch("OneTouch_4", onetouch.onetouch_screen.onetouch_4[5][2].label )
        CreateChildOneTouch("OneTouch_5", onetouch.onetouch_screen.onetouch_5[6][2].label )
        CreateChildOneTouch("OneTouch_6", onetouch.onetouch_screen.onetouch_6[7][2].label )

    }
}

def fetchChild(String Unit){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
         return null
    }
    return cd 
}

void Updateinfo(){
    
    if(!session_id) {
        LoginGetSession()
    }
    updatedeviceinfo()
    
    if (autoUpdate) runIn(3600, Updateinfo)
}

void notifychild(String chunit, String status, String label, String devtype, String subtype){
    def cd = fetchChild(chunit)
    if(cd){
        cd.parsechild(status, label, devtype, subtype)
    }
}
void parsechild(String status2, String label, String devtype, String subtype) {
    device.name = label
    status= status2
    if(status == "1") {
        sendEvent(name: "switch", value: "on")        
    } else {
        sendEvent(name: "switch", value: "off")
    }
    device.updateSetting("devtype", devtype)
    sendEvent(name:"devtype", value:devtype)
    state.devtype = devtype
   

}

void updatedeviceinfo(){
    devices = GetDevices()
   
    try {  notifychild("1",devices.devices_screen[3].aux_1[0].state, devices.devices_screen[3].aux_1[1].label, devices.devices_screen[3].aux_1[3].type, devices.devices_screen[3].aux_1[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_1 failed: ${e.message}" }
    try {  notifychild("2",devices.devices_screen[4].aux_2[0].state, devices.devices_screen[4].aux_2[1].label, devices.devices_screen[4].aux_2[3].type, devices.devices_screen[4].aux_2[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_2 failed: ${e.message}" }
    try {  notifychild("3",devices.devices_screen[5].aux_3[0].state, devices.devices_screen[5].aux_3[1].label, devices.devices_screen[5].aux_3[3].type, devices.devices_screen[5].aux_3[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_3 failed: ${e.message}" }
    try {  notifychild("4",devices.devices_screen[6].aux_4[0].state, devices.devices_screen[6].aux_4[1].label, devices.devices_screen[6].aux_4[3].type, devices.devices_screen[6].aux_4[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_4 failed: ${e.message}" }
    try {  notifychild("5",devices.devices_screen[7].aux_5[0].state, devices.devices_screen[7].aux_5[1].label, devices.devices_screen[7].aux_5[3].type, devices.devices_screen[7].aux_5[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_5 failed: ${e.message}" }
    try {  notifychild("6",devices.devices_screen[8].aux_6[0].state, devices.devices_screen[8].aux_6[1].label, devices.devices_screen[8].aux_6[3].type, devices.devices_screen[8].aux_6[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_6 failed: ${e.message}" }
    try {  notifychild("7",devices.devices_screen[9].aux_7[0].state, devices.devices_screen[9].aux_7[1].label, devices.devices_screen[9].aux_7[3].type, devices.devices_screen[9].aux_7[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_7 failed: ${e.message}" }
    if (aux_EA) { try {  notifychild("8",devices.devices_screen[10].aux_EA[0].state, devices.devices_screen[10].aux_EA[1].label, devices.devices_screen[10].aux_EA[3].type, devices.devices_screen[10].aux_EA[4].subtype )  } catch (Exception e) { if (logEnable) log.warn "aux_EA failed: ${e.message}" } }
    
    
    devices = GetHomeScreenInfo()
    
    notifychild("FilterPump",devices.home_screen[12].pool_pump, "FilterPump" , "FilterPump", "0")
    
    state.aqualink_status = devices.home_screen[0].status
    sendEvent(name: "aqualink_status", value: devices.home_screen[0].status)
    
    if (logEnable) log.info "pool pump status  ${devices.home_screen[12].pool_pump}"
    
    if (logEnable) log.info "pool pump temp  ${devices.home_screen[5].pool_temp}"
    
    try {  notifychild("Pool_Temp",devices.home_screen[5].pool_temp, "Pool Temp", "0", "0" )  } catch (Exception e) { log.warn "pool temp failed: ${e.message}" }
    
    if(SpaTemp){
      try {  notifychild("Spa_Temp",devices.home_screen[4].spa_temp, "Spa Temp", "0", "0" )  } catch (Exception e) { log.warn "Spa temp failed: ${e.message}" }
    }    
    if(AirTemp){
      try {  notifychild("Air_Temp",devices.home_screen[6].air_temp, "Air Temp", "0", "0" )  } catch (Exception e) { log.warn "Air temp failed: ${e.message}" }
    } 
    if(SpaPump){
      try {  notifychild("Spa_Pump",devices.home_screen[11].spa_pump, "SpaPump", "SpaPump", "0" )  } catch (Exception e) { log.warn "Spa Pump failed: ${e.message}" }
    }  
    
    if(SpaHeater){
      try {  notifychild("Spa_Heater",devices.home_screen[13].spa_heater, "Spa_Heater", "Spa_Heater", "0" )  } catch (Exception e) { log.warn "Spa_Heater failed: ${e.message}" }
    }  
    if(PoolHeater){
      try {  notifychild("Pool_Heater",devices.home_screen[14].pool_heater, "Pool_Heater", "Pool_Heater", "0" )  } catch (Exception e) { log.warn "Pool_Heater failed: ${e.message}" }
    }
    if(SolarHeater){
      try {  notifychild("Solar_Heater",devices.home_screen[15].solar_heater, "Solar_Heater", "Solar_Heater", "0" )  } catch (Exception e) { log.warn "Solar_Heater failed: ${e.message}" }
    }  
    if(OneTouch){
      onetouch = GetOneTouch()
      try {  notifychild("OneTouch_1",onetouch.onetouch_screen.onetouch_1[2][1].state, onetouch.onetouch_screen.onetouch_1[2][2].label, "OneTouch", "0" )  } catch (Exception e) { log.warn "OneTouch failed: ${e.message}" }
      try {  notifychild("OneTouch_2",onetouch.onetouch_screen.onetouch_2[3][1].state, onetouch.onetouch_screen.onetouch_2[3][2].label, "OneTouch", "0" )  } catch (Exception e) { log.warn "OneTouch failed: ${e.message}" }
      try {  notifychild("OneTouch_3",onetouch.onetouch_screen.onetouch_3[4][1].state, onetouch.onetouch_screen.onetouch_3[4][2].label, "OneTouch", "0" )  } catch (Exception e) { log.warn "OneTouch failed: ${e.message}" }
      try {  notifychild("OneTouch_4",onetouch.onetouch_screen.onetouch_4[5][1].state, onetouch.onetouch_screen.onetouch_4[5][2].label, "OneTouch", "0" )  } catch (Exception e) { log.warn "OneTouch failed: ${e.message}" }
      try {  notifychild("OneTouch_5",onetouch.onetouch_screen.onetouch_5[6][1].state, onetouch.onetouch_screen.onetouch_5[6][2].label, "OneTouch", "0" )  } catch (Exception e) { log.warn "OneTouch failed: ${e.message}" }
      try {  notifychild("OneTouch_6",onetouch.onetouch_screen.onetouch_6[7][1].state, onetouch.onetouch_screen.onetouch_6[7][2].label, "OneTouch", "0" )  } catch (Exception e) { log.warn "OneTouch failed: ${e.message}" }

    }
}

def LoginGetSession(){
    
    //def wxURI = "https://support.iaqualink.com/users/sign_in.json"
    def wxURI = "https://prod.zodiac-io.com/users/v1/login"
    
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
            
            device.updateSetting("session_id", [value: response.data.session_id, type: "String"])
            session_id = response.data.session_id
            settings.session_id = response.data.session_id
            
	    auth_token = response.data.authentication_token
            sendEvent(name: "auth_token", value: auth_token)
            state.auth_token = auth_token
            user_id = response.data.id
            sendEvent(name: "user_id", value: user_id)
            state.user_id = user_id		
			
			return response.data.session_id
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
}

void rundeviceupdate(){
    if(!session_id) {
        LoginGetSession()
    }
    updatedeviceinfo()
}

void on(){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    //log.info "${unit}"
    parent.rundeviceupdate()
    //log.info "  ${device.currentValue('switch')} "
    //log.info "  ${device.currentValue('subtype')}"
    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'off'){
        if (logEnable) log.info "Toggle Device ON  ${device}"
        if(unit == "FilterPump"){
            parent.TogglePoolPump()  
        } else if(devtype == "2"){
            parent.OperateColorLightParent(unit,settings.lightcolor)
        } else {
            parent.OperateDeviceParent(unit)
        }
        sendEvent(name: "switch", value: "on")        
    } else {
        if (logEnable) log.info "Device Already ON  ${device}"
    }
      
}

void off(){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    parent.rundeviceupdate()
    log.info "${unit}    ${device.currentValue('switch')} "
    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'on'){
        if (logEnable) log.info "Toggle Device OFF  ${device}"
        if(unit == "FilterPump"){
            parent.TogglePoolPump()  
        } else if(devtype == "2"){
            parent.OperateColorLightParent(unit,"0")
        } else {
            parent.OperateDeviceParent(unit)
        }
        sendEvent(name: "switch", value: "off")        
    } else {
        if (logEnable) log.info "Device Already OFF  ${device}"
    }
        
}

void OperateDeviceParent(String num){
    if(!session_id) {
        LoginGetSession()
    }
    OperateDevice(num)
}
void OperateDevice(String num){
    def wxURI2 = "${api_url}?actionID=command&command=set_aux_${num}&serial=${serial_number}&sessionID=${session_id}"
    if (logEnable) log.info wxURI2
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
    auth_token = device.currentValue("auth_token")
    user_id = device.currentValue("user_id")
    //def wxURI2 = "https://support.iaqualink.com/devices.json?api_key=EOOEMOW4YR6QNB07&authentication_token=${auth_token}&user_id=${user_id}"
    def wxURI2 = "https://r-api.iaqualink.net/devices.json?api_key=EOOEMOW4YR6QNB07&authentication_token=${auth_token}&user_id=${user_id}"
    
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
def TogglePoolPump() {
    
    if(!session_id) {
        LoginGetSession()
    }
    
    OperatePoolPump();
    
}
def OperatePoolPump() {
    
    def wxURI2 = "${api_url}?actionID=command&command=set_pool_pump&serial=${serial_number}&sessionID=${session_id}"
    
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
def ToggleSpaPump() {
    
    if(!session_id) {
        LoginGetSession()
    }
    
    OperateSpaPump();
    
}
def OperateSpaPump() {
    
    def wxURI2 = "${api_url}?actionID=command&command=set_spa_pump&serial=${serial_number}&sessionID=${session_id}"
    
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

def SetHeater(String type, String temp) {
    
    if(type=="spa"){
        type2="temp1"
    }
    if(type=="pool"){
        type2="temp2"
    }
    
    def wxURI2 = "${api_url}?actionID=command&command=set_temps&serial=${serial_number}&sessionID=${session_id}&${type2}=${temp}"
    
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
def ToggleHeater(String type) {
    
    if(type=="Spa_Heater"){
        type2="set_spa_heater"
    }
    if(type=="Pool_Heater"){
        type2="set_pool_heater"
    }
    
    def wxURI2 = "${api_url}?actionID=command&command=${type2}&serial=${serial_number}&sessionID=${session_id}"
    
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
def GetHomeScreenInfo() {
    
    def wxURI2 = "${api_url}?actionID=command&command=get_home&serial=${serial_number}&sessionID=${session_id}"
    
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

def SetOneTouch(String button) {

    def wxURI2 = "${api_url}?actionID=command&command=set_onetouch_${button}&serial=${serial_number}&sessionID=${session_id}"
    if (logEnable) log.info wxURI2
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
            log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    
}

def GetOneTouch() {

    def wxURI2 = "${api_url}?actionID=command&command=get_onetouch&serial=${serial_number}&sessionID=${session_id}"
    if (logEnable) log.info wxURI2
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
            log.info response.data
            //log.info response.data.onetouch_screen.onetouch_1[2][1].state
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    
}

def GetDevices() {
    
    def wxURI2 = "${api_url}?actionID=command&command=get_devices&serial=${serial_number}&sessionID=${session_id}"
    if (logEnable) log.info wxURI2
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

void OperateColorLightParent(String num, String color){
    if(!session_id) {
        LoginGetSession()
    }
    OperateColorLight(num,color)
}
void OperateColorLight(String num,String light_color){
    def wxURI2 = "${api_url}?actionID=command&aux=${num}&command=set_light&light=${light_color}&serial=${serial_number}&sessionID=${session_id}"
    log.info wxURI2
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
