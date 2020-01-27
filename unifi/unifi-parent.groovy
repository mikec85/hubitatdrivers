metadata {
    definition (name: "Unifi", namespace: "unifi", author: "MC", importUrl: "https://raw.githubusercontent.com/mikec85/hubitatdrivers/master/unifi/unifi-parent.groovy") {
        capability "Initialize"
        capability "Switch"
        capability "PresenceSensor"
        
        attribute "cookie", "string"
        attribute "CookieValid", "boolean"
        
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
        command "DeleteChild", ["String"]
        command "GetKnownClients", null
        command "GetKnownClientsDisabled", ["_id"]
        command "GetClientID", ["MAC"]
        
        command "CheckIfCookieValid", null
    }

    preferences {
        section("Device Settings:") {
            input "ip_addr", "string", title:"ip address", description: "", required: true, displayDuringSetup: true
            input "url_port", "string", title:"tcp port", description: "", required: true, displayDuringSetup: true, defaultValue: "8443"
            input "unifi_site", "string", title:"Unifi Site, most likely the default", description: "", required: true, displayDuringSetup: true, defaultValue: "default"
            input "username", "string", title:"Username", description: "", required: true, displayDuringSetup: true, defaultValue: "admin"
            input "password", "string", title:"User Password", description: "", required: true, displayDuringSetup: true
            input "timedelaycookie", "number", title:"Number of seconds before checking if login works", description: "", required: true, displayDuringSetup: true, defaultValue: "600"
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
        }
    }


}
void DeleteChild(String device){
    deleteChildDevice(device)
    String thisId = device.id
    deleteChildDevice("${thisId}-${device}")
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

void initialize(){
    if (autoUpdate) runIn(timedelaycookie.toInteger(), CheckIfCookieValid)
}

void parse(String description) {
    

}

def GetClientID2(String mac) {
    return GetClientID(mac)
}
def GetClientID(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/stat/sta/${mac}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
            if (logEnable) log.info response.data.data[0]._id
            
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
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/rest/user"
    log.info wxURI2
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/rest/user/${id}"
    payload = "{\"type\":[\"disabled\"]}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
    
    if (tempstatus.toString().contains("data=[{name")) {
        sendEvent(name: "CookieValid", value: true)
    } else {
        sendEvent(name: "CookieValid", value: false)
        Login()
    }
    
}
def GetSelf() {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/self"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/stat/sta/${mac}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
                 ],
	]
 
    def status = false
    
    try{
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			status = true
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
        log.info e
        
        if(e.toString().contains( "groovyx.net.http.HttpResponseException:") )  {
            if (logEnable) log.info "check login"
            Login()
        }
    }
    return status
}

def GetDeviceStatus(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/stat/sta/${mac}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
def unBlockDevice(String mac) {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/cmd/stamgr"
    payload = "{\"cmd\":\"unblock-sta\",\"mac\":\"${mac}\"}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/cmd/stamgr"
    payload = "{\"cmd\":\"block-sta\",\"mac\":\"${mac}\"}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "192.168.1.188:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/stat/sta"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/s/${unifi_site}/self"
    
    def requestParams2 =
	[
		uri:  wxURI2,
        ignoreSSLIssues:  true,
        headers: [ 
                   Host: "${ip_addr}:${url_port}",
                   
                   Accept: "*/*",
                   Cookie: "${settings.cookie}"
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

def Login() {
    
    def wxURI2 = "https://${ip_addr}:${url_port}/api/login"
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
            response.getHeaders('Set-Cookie').each {
                log.info it.value.split(';')[0]
                cookie2 = cookie2 + it.value.split(';')[0] + ";"
            }
            
            device.updateSetting("cookie", [value: cookie2, type: "String"])
            
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

