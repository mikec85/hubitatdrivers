metadata {
    definition (name: "Pool Aqualink Child", namespace: "aqualink", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/aqualink/aqualink-driver-child.groovy") {
        capability "Switch"
        
        attribute "contact", "string"
        
        attribute "label", "string"
        attribute "status", "string"
        attribute "subtype", "string"
        attribute "devtype", "string"
        
        command "ChangeLightColor", ["String"]
        command "Update", null
    }

    preferences {
        section("Device Settings:") {
            input "lightcolor", "string", title:"Color for Light (Should be Number from 1-12)", description: "", required: true, displayDuringSetup: true, defaultValue: "1"
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

void ChangeLightColor(String color){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    parent.OperateColorLightParent(unit,color)
}

void updatecolor(){
    device.updateSetting("colorlightenable", [value: true, type: "bool"])
    device.updateSetting("devtype", [value: "2", type: "String"])
}
void updatedevtype(String devtype){
    device.updateSetting("devtype", [value: devtype, type: "String"])
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


void on(){
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    if (logEnable) log.info "The unit ${unit}"
    parent.rundeviceupdate()
    //log.info "  ${device.currentValue('switch')} "
    //log.info "  ${device.currentValue('subtype')}"
    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'off'){
        if (logEnable) log.info "Toggle Device ON  ${device}"
        if(unit == "FilterPump"){
            parent.TogglePoolPump()  
        } else if(unit == "Spa_Pump"){
            parent.ToggleSpaPump()
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
    if (logEnable) log.info "The unit ${unit}"
    parent.rundeviceupdate()
    log.info "${unit}    ${device.currentValue('switch')} "
    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'on'){
        if (logEnable) log.info "Toggle Device OFF  ${device}"
        if(unit == "FilterPump"){
            parent.TogglePoolPump()  
        } else if(unit == "Spa_Pump"){
            parent.ToggleSpaPump()
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
