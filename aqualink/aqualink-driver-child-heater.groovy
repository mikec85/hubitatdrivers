metadata {
    definition (name: "Pool Aqualink Child Heater", namespace: "aqualink", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/aqualink/aqualink-driver-child-heater.groovy") {
        capability "Switch"
        
        attribute "contact", "string"
        
        attribute "label", "string"
        attribute "status", "string"
        attribute "subtype", "string"
        attribute "devtype", "string"
        
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

void updatedevtype(String devtype){
    device.updateSetting("devtype", [value: devtype, type: "String"])
}

void parsechild(String status2, String label, String devtype, String subtype) {
    device.name = label
    status= status2
    
    if (logEnable) log.info "${device.name}  ${status}"
    
    if(status == "1") {
        sendEvent(name: "switch", value: "on")        
    } else if(status == "3") {
        sendEvent(name: "switch", value: "on")        
    } else {
        sendEvent(name: "switch", value: "off")
    }
    device.updateSetting("devtype", devtype)
    sendEvent(name:"devtype", value:devtype)
    state.devtype = devtype
   

}


void on(){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    parent.rundeviceupdate()

    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'off'){
        if (logEnable) log.info "Toggle Device ON  ${device}"
        parent.ToggleHeater(device.name)  
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
        parent.ToggleHeater(device.name)  
        sendEvent(name: "switch", value: "off")        
    } else {
        if (logEnable) log.info "Device Already OFF  ${device}"
    }
        
}
