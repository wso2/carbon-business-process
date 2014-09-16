function authenticate(username, password){
   var 	carbon = require('carbon'),
	process = require('process'),
	localIP = process.getProperty('carbon.local.ip'),
   	httpsPort = process.getProperty('mgt.transport.https.port'),
	httpsUrl = "https://"+localIP+":"+httpsPort,
	srv = new carbon.server.Server({url: httpsUrl});
	return srv.authenticate(username, password);
}

function getRoles(username, password){
   var 	carbon = require('carbon'),
	process = require('process'),
	localIP = process.getProperty('carbon.local.ip'),
   	httpsPort = process.getProperty('mgt.transport.https.port'),
	httpsUrl = "https://"+localIP+":"+httpsPort,
	srv = new carbon.server.Server({url: httpsUrl}),
	tenantId = carbon.server.tenantId(),
	userManager = new carbon.user.UserManager(srv, tenantId),
	user = new carbon.user.User(userManager, username),
	roles = user.getRoles();
	return roles;
}
