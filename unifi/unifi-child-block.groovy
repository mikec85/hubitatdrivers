metadata {
    definition (name: "Unifi Child Block", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-child-block.groovy") {
        capability "Switch"
        capability "Initialize"
      
        command "Update", null
        command "GetClientID", null
        command "DeleteThisChild", null
    }

    preferences {
        section("Device Settings:") {
            input "mac_addr", "string", title:"Mac Address of Client to Track", description: "", required: true, displayDuringSetup: true, defaultValue: ""
            input "_id", "string", title:"Client ID", description: "", required: true, displayDuringSetup: true, defaultValue: ""
            input "timedelay", "number", title:"Number of seconds before rechecking", description: "", required: true, displayDuringSetup: true, defaultValue: "3600"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
        }
    }


}

void DeleteThisChild(){
    parent.FromChildDeleteChild("${mac_addr}")
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
void initialize(){
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
    
}

void setmac(String MAC) {
       device.updateSetting("mac_addr", [value: "${MAC}", type: "string"])
}

void Update(){
    def status2 = false
    status2 = parent.GetKnownClientsDisabledChild(_id)
    status2 = status2.data[0].blocked
    if (logEnable) log.info status2
    
    if(!status2) {
       sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
    
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
}

void off(){
    
    parent.BlockDevice(mac_addr)   
    sendEvent(name: "switch", value: "off")
}

void on(){
    
    parent.unBlockDevice(mac_addr)
    sendEvent(name: "switch", value: "on")
}

def GetClientID() {
    status = parent.GetClientID2("${mac_addr}") 
    if(status) {
        sendEvent(name: "_id", value: status)
        device.updateSetting("_id", [value: "${status}", type: "string"])
    } else {
        sendEvent(name: "_id", value: "ID Not Found")
    }
}


