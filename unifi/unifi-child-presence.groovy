metadata {
    definition (name: "Unifi Child Presence", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-child-presence.groovy") {
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
    def status3 = false
    status2 = parent.ChildGetClientConnected(mac_addr)
    
    if(status2.equals("")) {
        status3 = false
    } else {
        status3 = true
        log.info status2.data[0].hostname
        sendEvent(name: "hostname", value: status2.data[0].hostname)
        log.info status2.data[0].last_seen
        sendEvent(name: "last_seen", value: status2.data[0].last_seen)
        sendEvent(name: "essid", value: status2.data[0].essid)
        sendEvent(name: "network", value: status2.data[0].network)
        sendEvent(name: "radio_proto", value: status2.data[0].radio_proto) 
        sendEvent(name: "ap_mac", value: status2.data[0].ap_mac) 
        apinfo = parent.GetAPStatus(status2.data[0].ap_mac)
        sendEvent(name: "ap_name", value: apinfo.data[0].name) 
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


