metadata {
    definition (name: "Pool Aqualink Child OneTouch", namespace: "aqualink", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/aqualink/aqualink-driver-child.groovy") {
        capability "Switch"
        
        attribute "contact", "string"
        
        attribute "label", "string"
        attribute "status", "string"
        
        command "Update", null
    }

    preferences {
        section("Device Settings:") {
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
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
    parent.Updateinfo()
}


void parsechild(String status2, String label, String devtype, String subtype) {
    log.info "${status2}   ${label}"
    device.name = label
    status= status2
    if(status == "1") {
        sendEvent(name: "switch", value: "on")        
    } else {
        sendEvent(name: "switch", value: "off")
    }


}


void on(){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    unit = unit.replace("OneTouch_","")
    
    if (logEnable) log.info "The unit ${unit}"
    parent.rundeviceupdate()
    
    if(device.currentValue('switch') == 'off'){
        if (logEnable) log.info "Toggle Device ON  ${device}"
        parent.SetOneTouch(unit)
        sendEvent(name: "switch", value: "on")        
    } else {
        if (logEnable) log.info "Device Already ON  ${device}"
    }
      
}

void off(){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    unit = unit.replace("OneTouch_","")
    
    if (logEnable) log.info "The unit ${unit}"
    parent.rundeviceupdate()
    
    log.info "${unit}    ${device.currentValue('switch')} "
    
    if(device.currentValue('switch') == 'on'){
        if (logEnable) log.info "Toggle Device OFF  ${device}"
        parent.SetOneTouch(unit)
        sendEvent(name: "switch", value: "off")        
    } else {
        if (logEnable) log.info "Device Already OFF  ${device}"
    }
        
}
