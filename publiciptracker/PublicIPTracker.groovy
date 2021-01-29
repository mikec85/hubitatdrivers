metadata {
	definition (name: "PublicIPTracker", namespace: "PublicIPTracker", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/publiciptracker/PublicIPTracker.groovy") {
		capability "Initialize"
		
		
		command "Update"

		
	}
	
	preferences{
        input "timedelay", "number", title:"Number of seconds before rechecking", description: "", required: true, displayDuringSetup: true, defaultValue: "3600"
        input ("logdebugs", "bool", title: "Log debugging messages", defaultValue: false, displayDuringSetup: false)
        input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
	}

}

void debugOff() {
   log.warn("Disabling debug logging")
   device.updateSetting("logdebugs", [value:"false", type:"bool"])
}

void initialize(){
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
	
    if (logdebugs) {
	log.debug "Debug logging will be disabled in 600 seconds"
	runIn(600, debugOff)
    }
    
}

def version()
	{
	updateDataValue("driverVersion", "0.1.0")	
	return "0.1.0";
	}
def updateData(String name, String data) {
    
    updateDataValue(name, data)
    sendEvent(name: name, value: data)
    
}
def Update() {
    
    ipdata = GetIFConfig() 
    if(logdebugs) log.info ipdata


    state.ip = ipdata.ip
    
    updateData("ip", "${ipdata.ip}")
    updateData("country", "${ipdata.country}")
    updateData("ip_decimal", "${ipdata.ip_decimal}")
    updateData("country", "${ipdata.country}")
    updateData("country_iso", "${ipdata.country_iso}")
    updateData("country_eu", "${ipdata.country_eu}")
    updateData("region_name", "${ipdata.region_name}")
    updateData("region_code", "${ipdata.region_code}")
    updateData("metro_code", "${ipdata.metro_code}")
    updateData("zip_code", "${ipdata.zip_code}")
    updateData("city", "${ipdata.city}")
    updateData("latitude", "${ipdata.latitude}")
    updateData("longitude", "${ipdata.longitude}")
    updateData("time_zone", "${ipdata.time_zone}")
    updateData("asn", "${ipdata.asn}")
    updateData("asn_org", "${ipdata.asn_org}")
    updateData("hostname", "${ipdata.hostname}")

}

def GetIFConfig() {
    
    def wxURI2 = "https://ifconfig.co/"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        headers: [ 
                   Host: "ifconfig.co",
                   Accept: "application/json"
                 ]

	]
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    } catch (Exception e){
        log.info e
    }
}
