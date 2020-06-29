metadata {
    definition (name: "Pool Aqualink Child Temps", namespace: "aqualink", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/aqualink/aqualink-driver-child-temps.groovy") {
        capability "Thermostat"
        capability "Initialize"
        
    }

    preferences {
        section("Device Settings:") {
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
        }
    }


}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

void parse(String description) {
    

}

void initialize() {
    //sendEvent(name: "temperature", value: temp.toInteger()) 

    sendEvent(name: "coolingSetpoint", value: "0") 
    sendEvent(name: "heatingSetpoint", value: "0") 
    sendEvent(name: "supportedThermostatFanModes", value: "auto") 
    sendEvent(name: "supportedThermostatModes", value: "auto") 
    sendEvent(name: "thermostatFanMode", value: "auto") 
    sendEvent(name: "thermostatOperatingState", value: "idle") 
    sendEvent(name: "thermostatSetpoint", value: "0") 
    
    sendEvent(name: "hysteresis", value: "0") 
    sendEvent(name: "thermostatMode", value: "idle") 
    
}

void updated() {
    log.info "updated..."
    initialize()
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
    
    if (autoUpdate) runIn(3600, Updateinfo)
}




void updatetemp(String temp){
    sendEvent(name: "temperature", value: temp.toInteger()) 
}


void parsechild(String status2, String label, String devtype, String subtype) {
    device.name = label
    
    log.info "temp   ${status2}   ${label}   ${devtype}  ${subtype} "
    //log.info status2.toInteger()
    if(status2 == "") {
       sendEvent(name: "temperature", value: 0) 
    } else {
       sendEvent(name: "temperature", value: status2.toInteger())  
    }
}

def setTempUp() 
{ 
    log.debug "Setting temp up: "
}

def setTempDown() 
{ 
    log.debug "Setting temp down: "
}

def setTemperature(temp)
{
	log.debug "setTemperature $temp"
}

def setHeatingSetpoint(degrees) 
{
	log.debug("Degrees at setheatpoint: $degrees")
}

def setHeatingSetpoint(Double degrees) 
{
	log.debug "setHeatingSetpoint($degrees)"
}
