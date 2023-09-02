metadata {
    definition (name: "Unifi", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-parent.groovy") {
        capability "Initialize"
        capability "Switch"
        capability "PresenceSensor"
        
        attribute "cookie", "string"
        attribute "csrf", "string"
        attribute "CookieValid", "string"
        
        command "GetDevices", null
        command "GetStatus", null
        command "GetSelf", null
        command "Login", null
        command "BlockDevice", ["String"]
        command "unBlockDevice", ["String"]
        command "GetDeviceStatus", ["String"]
        command "GetClientConnected", ["String"]
        command "CreateChildPresence", ["MAC","Label"]
        command "CreateChildBlock", ["MAC","Label"]
	command "CreateChildMonitorDevice", ["MAC","Label"]
        command "CreateChildPresenceWired", ["MAC","Label"]
        command "DeleteChild", ["String"]
        command "GetKnownClients", null
        command "GetKnownClientsDisabled", ["_id"]
        command "GetClientID", ["MAC"]
        command "SetURL", null
        command "CheckIfCookieValid", null
    }

    preferences {
        section("Device Settings:") {
            input "ip_addr", "string", title:"ip address", description: "", required: true, displayDuringSetup: true
            input "url_port", "string", title:"tcp port, For UDM Pro change to 443", description: "", required: true, displayDuringSetup: true, defaultValue: "8443"
            input "unifi_site", "string", title:"Unifi Site, most likely the default", description: "", required: true, displayDuringSetup: true, defaultValue: "default"
            input "username", "string", title:"Username", description: "", required: true, displayDuringSetup: true, defaultValue: "admin"
            input "password", "string", title:"User Password", description: "", required: true, displayDuringSetup: true
            input "timedelaycookie", "number", title:"Number of seconds before checking if login works", description: "", required: true, displayDuringSetup: true, defaultValue: "600"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto Testing for Valid Login", defaultValue: true
            input name: "UDMPro", type: "bool", title: "Enable if you have a Unifi OS product such as UDM Pro(testing)", defaultValue: false
        }
    }


}
void FromChildDeleteChild(String devicedata){
    String thisId = device.id
    log.info "${thisId}-${devicedata}"
    deleteChildDevice("${thisId}-${devicedata}")
}

void DeleteChild(String devicedata){
    log.info devicedata
    deleteChildDevice(devicedata)
    String thisId = device.id
    log.info thisId
    deleteChildDevice("${thisId}-${devicedata}")
}

def ChildGetClientConnected(String mac_addr){
    return GetClientConnected(mac_addr)   
}

def CreateChildPresence(String MAC, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${MAC}")
    if (!cd) {
        cd = addChildDevice( "Unifi Child Presence", "${thisId}-${MAC}", [name: "${label}", isComponent: true])
    }
    cd.setmac(MAC)
    return cd 
}

def CreateChildBlock(String MAC, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${MAC}")
    if (!cd) {
        cd = addChildDevice( "Unifi Child Block", "${thisId}-${MAC}", [name: "${label}", isComponent: true])
    }
    cd.setmac(MAC)
    return cd 
}

def CreateChildMonitorDevice(String MAC, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${MAC}")
    if (!cd) {
        cd = addChildDevice( "Unifi Child Device", "${thisId}-${MAC}", [name: "${label}", isComponent: true])
    }
    cd.setmac(MAC)
    return cd 
}

def CreateChildPresenceWired(String MAC, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${MAC}")
    if (!cd) {
        cd = addChildDevice( "Unifi Child Presence Wired", "${thisId}-${MAC}", [name: "${label}", isComponent: true])
    }
    cd.setmac(MAC)
    return cd 
}

void initialize(){
    Login()
    if (autoUpdate) runIn(timedelaycookie.toInteger(), CheckIfCookieValid)
    SetURL()
}

void parse(String description) {
    

}

def GetClientID2(String mac) {
    return GetClientID(mac)
}
def GetClientID(String mac) {
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/stat/sta/${mac}"
    cookie = device.currentValue("cookie")
    csrf = device.currentValue("csrf")
    log.info payload
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}",
                   'X-CSRF-Token': "${csrf}"
                 ]
	]
 
    def status = ""
    
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
            if (logEnable) log.info "Client ID ${response.data.data[0]._id}"
            
            sendEvent(name: "_id", value: response.data.data[0]._id)
            
			status = response.data.data[0]._id
		}
		else
		{
			log.warn "${response?.status}"
		}
        
	}
    } catch (Exception e){
        log.info e
    }
    return status
}

def GetKnownClients() {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/rest/user"
    if (logEnable) log.info wxURI2
    cookie = device.currentValue("cookie")
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ],
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
def GetKnownClientsDisabledChild(String id) {
    return GetKnownClientsDisabled(id)
}
def GetKnownClientsDisabled(String id) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/rest/user/${id}"
    payload = "{\"type\":[\"disabled\"]}"
    cookie = device.currentValue("cookie")
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ],
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

def CheckIfCookieValid() {
    tempstatus = GetSelf()
    if (logEnable) log.info tempstatus
	
    if (tempstatus.toString().contains("data=[{")) {
        sendEvent(name: "CookieValid", value: true)
    } else {
        sendEvent(name: "CookieValid", value: false)
        Login()
    }
    if (autoUpdate) runIn(timedelaycookie.toInteger(), CheckIfCookieValid)
}
def GetSelf() {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/self"
    cookie = device.currentValue("cookie")
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ]
	]
    rdata = ""
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			rdata = response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    } catch (Exception e){
        log.info e
    }
    return rdata
}
def GetClientConnected(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/stat/sta/${mac}"
    cookie = device.currentValue("cookie")
    def requestParams2 =
	[
	   uri:  wxURI2,
           ignoreSSLIssues:  true,
           headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ],
	]
 
    def status = ""
    
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
           
			status = response.data
		}
		else
		{
			log.warn "${response?.status}"
            
            if(response?.status.contains("groovyx.net.http.HttpResponseException"))  {
                if (logEnable) log.info "check login"
                Login()
            }
		}
	}
    
    } catch (Exception e){
        if (logEnable) log.info e     
        if(e.toString().contains( "groovyx.net.http.HttpResponseException:") )  {
            if (e.response.status == 400 && e.response.data && e.response.data.meta && e.response.data.meta.rc == "error" && e.response.data.meta.msg.contains("UnknownStation")) {
                // this is a definitive response from the UniFi controller that such client is currently not connected
                return "not_present"
            }
            if (logEnable) log.info "check login"
            Login()
        }
    }
    return status
}

def Child_GetAPStatus(String mac) {
    apinfo = GetAPStatus(mac)
    return apinfo
}
def GetAPStatus(String mac) {
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/stat/device/${mac}"
    cookie = device.currentValue("cookie")
    def requestParams2 =
	[
          uri:  wxURI2,
          ignoreSSLIssues:  true,
          headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ],
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
        if (logEnable) log.info e
    }
}
def Child_GetAPStatus2(String ap_mac) {
    apinfo = GetAPStatus2(ap_mac)
    return apinfo
}
def GetAPStatus2(String ap_mac) {
    count = 0
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/stat/sta"
    cookie = device.currentValue("cookie")
    def requestParams2 =
	[
          uri:  wxURI2,
          ignoreSSLIssues:  true,
          headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ],
	]
 
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            log.info response.data.data[0]
            
            try{

        
                for(int lcv; lcv < response.data.data.size() ; lcv++ ) {
            
                    //log.info "${response.data.data[lcv].mac}   ${response.data.data[lcv].ap_mac}   ${response.data.data[lcv].hostname}"
                    
                    if( response.data.data[lcv].ap_mac == ap_mac ) {
                       count++   
                    }
            
                }

                log.info "the count ${count}"
        
            } catch (Exception e){
                log.info e
            }
            
			return count
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
def GetDeviceStatus(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/stat/sta/${mac}"
    cookie = device.currentValue("cookie")
    def requestParams2 =
	[
          uri:  wxURI2,
          ignoreSSLIssues:  true,
          headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ],
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
        if (logEnable) log.info e
    }
}
def unBlockDevice(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/cmd/stamgr"
    payload = "{\"cmd\":\"unblock-sta\",\"mac\":\"${mac}\"}"
    cookie = device.currentValue("cookie")
    csrf = device.currentValue("csrf")
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}",
                   'X-CSRF-Token': "${csrf}"
                 ],
        body: payload
	]
 
    try{
    httpPost(requestParams2)
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
def Child_RestartDevice(String mac) {
    RestartDevice(mac) 
}
def RestartDevice(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/cmd/devmgr"
    payload = "{\"cmd\":\"restart\",\"mac\":\"${mac}\"}"
    cookie = device.currentValue("cookie")
    csrf = device.currentValue("csrf")
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   Accept: "*/*",
                   Cookie: "${cookie}",
                   'X-CSRF-Token': "${csrf}"
                 ],
        body: payload
	]
 
    try{
    httpPost(requestParams2)
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
def BlockDevice(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/cmd/stamgr"
    payload = "{\"cmd\":\"block-sta\",\"mac\":\"${mac}\"}"
    cookie = device.currentValue("cookie")
    csrf = device.currentValue("csrf")
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   Accept: "*/*",
                   Cookie: "${cookie}",
                   'X-CSRF-Token': "${csrf}"
                 ],
        body: payload
	]
 
    try{
    httpPost(requestParams2)
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
def GetDevices() {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/stat/sta"
    cookie = device.currentValue("cookie")
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
                 ]

	]
 
    try{
    httpPost(requestParams2)
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
def GetStatus() {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/${settings.api_path}/s/${unifi_site}/self"
    cookie = device.currentValue("cookie")
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${cookie}"
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
def SetURL() {
    
    if(UDMPro) {
        device.updateSetting("api_path", [value: "proxy/network/api", type: "String"])
    } else {
        device.updateSetting("api_path", [value: "api", type: "String"])
    }
    
    
}
def Login() {
    
    def wxURI2 = ""
    if(UDMPro) {
        wxURI2 = "https://${ip_addr}:${url_port}/api/auth/login"
    } else {
        wxURI2 = "https://${ip_addr}:${url_port}/api/login"
    }
    SetURL()
    payload = "{\"username\":\"${username}\",\"password\":\"${password}\",\"remember\":\"true\"}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        requestContentType: "application/json",
        ignoreSSLIssues:  true,
        contentType: "application/json",
        body: payload

	]
    
    try{
    httpPost(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            
            cookie2=""
            csrf=""
            response.getHeaders('Set-Cookie').each {
                if (logEnable) log.info it.value.split(';')[0]
                cookie2 = cookie2 + it.value.split(';')[0] + ";"
            }
            response.getHeaders('X-CSRF-Token').each {
                if (logEnable) log.info it.value.split(';')[0]
                csrf = csrf + it.value.split(';')[0]
            }
            cookie = cookie2
            state.cookie = cookie
            sendEvent(name: "cookie", value: cookie2)
            state.csrf = csrf
            sendEvent(name: "csrf", value: csrf)
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

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}
