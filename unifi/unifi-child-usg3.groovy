metadata {
    definition (name: "Unifi Child USG3", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-child-presence.groovy") {
        capability "PresenceSensor"
        capability "Initialize"
      
        attribute "wan1_ip", "string"
        attribute "wan1_up", "string"
        attribute "wan1_speed", "string"
        attribute "wan1_full_duplex", "string"
        attribute "wan1_mac", "string"
        attribute "wan1_gateway", "string"
        
        attribute "lan_name", "string"
        attribute "lan_ip", "string"
        attribute "lan_up", "string"
        attribute "lan_speed", "string"
        attribute "lan_full_duplex", "string"
        
        attribute "lan2_name", "string"
        attribute "lan2_ip", "string"
        attribute "lan2_up", "string"
        attribute "lan2_speed", "string"
        attribute "lan2_full_duplex", "string"
        
        attribute "loadavg_1", "string"
        attribute "loadavg_5", "string"
        attribute "loadavg_15", "string"
        
        attribute "model", "string"
        attribute "type", "string"
        attribute "version", "string"
       
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
    
    devinfo = parent.GetAPStatus(mac_addr)
    if (logEnable) log.info devinfo    
    
    sendEvent(name: "wan1_ip", value: devinfo.data[0].wan1.ip) 
    state.wan1_ip = devinfo.data[0].wan1.ip
        
    sendEvent(name: "wan1_up", value: devinfo.data[0].wan1.up) 
    state.wan1_up = devinfo.data[0].wan1.up

    sendEvent(name: "wan1_speed", value: devinfo.data[0].wan1.speed) 
    state.wan1_speed = devinfo.data[0].wan1.speed

    sendEvent(name: "wan1_full_duplex", value: devinfo.data[0].wan1.full_duplex) 
    state.wan1_full_duplex = devinfo.data[0].wan1.full_duplex

    sendEvent(name: "wan1_mac", value: devinfo.data[0].wan1.mac) 
    state.wan1_mac = devinfo.data[0].wan1.mac

    sendEvent(name: "wan1_gateway", value: devinfo.data[0].wan1.gateway) 
    state.wan1_gateway = devinfo.data[0].wan1.gateway

    //lan
    sendEvent(name: "lan_name", value: devinfo.data[0].port_table[1].name) 
    state.lan_name = devinfo.data[0].port_table[1].name
    
    sendEvent(name: "lan_ip", value: devinfo.data[0].port_table[1].ip) 
    state.lan_ip = devinfo.data[0].port_table[1].ip
        
    sendEvent(name: "lan_up", value: devinfo.data[0].port_table[1].up) 
    state.lan_up = devinfo.data[0].port_table[1].up

    sendEvent(name: "lan_speed", value: devinfo.data[0].port_table[1].speed) 
    state.lan_speed = devinfo.data[0].port_table[1].speed

    sendEvent(name: "lan_full_duplex", value: devinfo.data[0].port_table[1].full_duplex) 
    state.lan_full_duplex = devinfo.data[0].port_table[1].full_duplex

    //lan2
    sendEvent(name: "lan2_name", value: devinfo.data[0].port_table[2].name) 
    state.lan2_name = devinfo.data[0].port_table[2].name
    
    sendEvent(name: "lan2_ip", value: devinfo.data[0].port_table[2].ip) 
    state.lan2_ip = devinfo.data[0].port_table[2].ip
        
    sendEvent(name: "lan2_up", value: devinfo.data[0].port_table[2].up) 
    state.lan2_up = devinfo.data[0].port_table[2].up

    sendEvent(name: "lan2_speed", value: devinfo.data[0].port_table[2].speed) 
    state.lan2_speed = devinfo.data[0].port_table[2].speed

    sendEvent(name: "lan2_full_duplex", value: devinfo.data[0].port_table[2].full_duplex) 
    state.lan2_full_duplex = devinfo.data[0].port_table[2].full_duplex    
    
    //Sys
    
    sendEvent(name: "model", value: devinfo.data[0].model) 
    state.model = devinfo.data[0].model    
    sendEvent(name: "type", value: devinfo.data[0].type) 
    state.type = devinfo.data[0].type    
    sendEvent(name: "version", value: devinfo.data[0].version) 
    state.version = devinfo.data[0].version
    
    sendEvent(name: "loadavg_1", value: devinfo.data[0].sys_stats.loadavg_1) 
    state.loadavg_1 = devinfo.data[0].sys_stats.loadavg_1    
    sendEvent(name: "loadavg_5", value: devinfo.data[0].sys_stats.loadavg_5) 
    state.loadavg_5 = devinfo.data[0].sys_stats.loadavg_5    
    sendEvent(name: "loadavg_15", value: devinfo.data[0].sys_stats.loadavg_15) 
    state.loadavg_15 = devinfo.data[0].sys_stats.loadavg_15   
    
    
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
}
