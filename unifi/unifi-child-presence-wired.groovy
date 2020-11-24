metadata {
    definition (name: "Unifi Child Presence Wired", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-child-presence.groovy") {
        capability "PresenceSensor"
        capability "Initialize"
      
        attribute "name", "string"
        attribute "last_seen", "string"
        attribute "last_seen_readable", "string"
        attribute "sw_port", "string"
        attribute "sw_mac", "string"
        attribute "sw_depth", "string"
        
        command "Update", null
        command "DeleteThisChild", null
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

void DeleteThisChild(){
    parent.FromChildDeleteChild("${mac_addr}")
}
void Update(){
    def status3 = false
    status2 = parent.ChildGetClientConnected(mac_addr)
    
    if(status2.equals("")) {
        status3 = false
    } else {
        status3 = true
        
        sendEvent([name: "name", value: status2.data[0].name])
        sendEvent(name: "last_seen", value: status2.data[0].last_seen)
        sendEvent(name: "last_seen_readable", value: new Date((status2.data[0].last_seen as long)*1000))
        sendEvent(name: "sw_mac", value: status2.data[0].sw_mac) 
        sendEvent(name: "sw_port", value: status2.data[0].sw_port) 
        sendEvent(name: "sw_depth", value: status2.data[0].sw_depth) 
        
        state.name = status2.data[0].name
        state.last_seen = status2.data[0].last_seen
        tempstring = new Date((status2.data[0].last_seen as long)*1000)
        state.last_seen_readable = "${tempstring}"
        state.sw_mac = status2.data[0].sw_mac
        state.sw_port = status2.data[0].sw_port
        state.sw_depth = status2.data[0].sw_depth

    }
    
    if (status3) {
        if (logEnable) log.info "present  ${device.getName()}  ${mac_addr}"
        sendEvent(name: "presence", value: "present")
    } else {
        if (logEnable) log.info "not present  ${device.getName()}  ${mac_addr}"
        sendEvent(name: "presence", value: "not present")
    }
    
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
}
