function authenticate(username, password){
   var 	carbon = require('carbon'),
	process = require('process'),
	localIP = process.getProperty('carbon.local.ip'),
   	httpsPort = process.getProperty('mgt.transport.https.port'),
	httpUrl = "https://"+localIP+":"+httpsPort,
	srv = new carbon.server.Server({url: httpUrl});
	return srv.authenticate(username, password);
}
