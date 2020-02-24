metadata {
    definition (name: "Unifi Child Presence", namespace: "unifi", author: "MC") {
        capability "PresenceSensor"
        capability "Initialize"
      
        command "Update", null
    }

    preferences {
        section("Device Settings:") {
            input "mac_addr", "string", title:"Mac Address of Client to Track", description: "", required: true, displayDuringSetup: true, defaultValue: ""
            input "timedelay", "number", title:"Number of seconds before rechecking", description: "", required: true, displayDuringSetup: true, defaultValue: "600"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
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
void initialize(){
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
    
}

void setmac(String MAC) {
       device.updateSetting("mac_addr", [value: "${MAC}", type: "string"])
}

void Update(){
    def status2 = false
    status2 = parent.ChildGetClientConnected(mac_addr)
    String thisId = device.id
    if (status2) {
        if (logEnable) log.info "present  ${thisId}  ${mac_addr}"
        sendEvent(name: "presence", value: "present")
    } else {
        if (logEnable) log.info "not present  ${thisId}  ${mac_addr}"
        sendEvent(name: "presence", value: "not present")
    }
    
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
}


