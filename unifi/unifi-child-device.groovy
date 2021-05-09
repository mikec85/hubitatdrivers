metadata {
    definition (name: "Unifi Child Device", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-child-presence.groovy") {
        capability "PresenceSensor"
        capability "Initialize"
      
        attribute "last_seen", "string"
        attribute "essid", "string"
        attribute "network", "string"
        attribute "radio_proto", "string"
        attribute "ap_mac", "string"
        attribute "ap_name", "string"
        
        attribute "model", "string"
        attribute "type", "string"
        attribute "version", "string"
        attribute "state", "string"
        attribute "uptime", "string"
        attribute "ip", "string"
        attribute "devicecount", "string"
        
        command "Update", null
        command "Restart", null
        command "DeleteThisChild", null
    }

    preferences {
        section("Device Settings:") {
            input "mac_addr", "string", title:"Mac Address of Device to Track", description: "", required: true, displayDuringSetup: true, defaultValue: ""
            input "timedelay", "number", title:"Number of seconds before rechecking", description: "", required: true, displayDuringSetup: true, defaultValue: "600"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
            input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
            input name: "GetDeviceCount", type: "bool", title: "Enable Getting Connected Device Count", defaultValue: false
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
    apinfo = parent.Child_GetAPStatus(mac_addr)

    state.ap_name = apinfo.data.name[0]
    
    state.model = apinfo.data.model[0]
    state.type = apinfo.data.type[0]
    state.version = apinfo.data.version[0]
    state.state = apinfo.data.state[0]
    state.uptime = apinfo.data.uptime[0]
    state.ip = apinfo.data.ip[0]

    sendEvent(name: "ap_name", value: apinfo.data.name[0])
    sendEvent(name: "model", value: apinfo.data.model[0])
    sendEvent(name: "type", value: apinfo.data.type[0])
    sendEvent(name: "version", value: apinfo.data.version[0])
    sendEvent(name: "state", value: apinfo.data.state[0])
    sendEvent(name: "uptime", value: apinfo.data.uptime[0])
    sendEvent(name: "ip", value: apinfo.data.ip[0])
    
    try{
        state.uplink_mac = apinfo.data.last_uplink[0].uplink_mac
        sendEvent(name: "uplink_mac", value: apinfo.data.last_uplink[0].uplink_mac)
        
        state.uplink_remote_port = apinfo.data.last_uplink[0].uplink_remote_port
        sendEvent(name: "uplink_remote_port", value: apinfo.data.last_uplink[0].uplink_remote_port)
        
    } catch (Exception e){
        log.info e
    }
    if(GetDeviceCount){              
    try{
        count = parent.Child_GetAPStatus2(mac_addr)
        state.devicecount = count
        sendEvent(name: "devicecount", value: count)
        
    } catch (Exception e){
        log.info e
    }
    }
    if (autoUpdate) runIn(timedelay.toInteger(), Update)

}
void Restart(){
    apinfo = parent.RestartDevice(mac_addr)
}
