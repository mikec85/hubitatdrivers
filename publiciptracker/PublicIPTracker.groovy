metadata {
	definition (name: "PublicIPTracker", namespace: "PublicIPTracker", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/publiciptracker/PublicIPTracker.groovy") {
	       capability "Initialize"
	       command "Update"

	       attribute "country", "string"
               attribute "country_iso", "string"
	       attribute "country_eu", "string"
	       attribute "region_name", "string"
	       attribute "region_code", "string"
	       attribute "metro_code", "string"
	       attribute "zip_code", "string"
	       attribute "city", "string"
	       attribute "latitude", "string"
	       attribute "longitude", "string"
	       attribute "time_zone", "string"
	       attribute "asn", "strring"
	       attribute "asn_org", "string"
	       attribute "hostname", "string"
	       attribute "lastIP", "string"
	       attribute "lastUpdate", "string"
	       attribute "ip", "string"
	       attribute "IPChanged", "boolean"
		
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
def installed()
{
    initialize()
}
void initialize(){
    if (autoUpdate) runIn(timedelay.toInteger(), Update)
	
    if (logdebugs) {
	log.debug "Debug logging will be disabled in 600 seconds"
	runIn(600, debugOff)
    }
	
    sendEvent(name: "IPChanged", value: false)
    state.IPChanged = false
    
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
    if (state.ip != null)
       {
           state.lastIP = state.ip
           sendEvent(name: "lastIP", value: state.lastIP)
       }
	
	
    ipdata = GetIFConfig() 
    if (ipdata != null)
      {
	    def now = new Date().format('MM/dd/yyyy h:mm a',location.timeZone)
            sendEvent(name: "lastUpdate", value: now, descriptionText: "Last Update: $now")
	      
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
      
	
      if ((state.ip != null) && (state.lastIP != null))
          {
           if (state.ip != state.lastIP)
              {
                log.debug  "Ip Changed: old = $state.lastIP, new = $state.ip, hostname = $ipdata.hostname!"
                state.IPChanged = true
                sendEvent(name: "IPChanged", value: true)
              }
           else
           {
            state.IPChanged = false
            sendEvent(name: "IPChanged", value: false)
           }
          }
      }
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
