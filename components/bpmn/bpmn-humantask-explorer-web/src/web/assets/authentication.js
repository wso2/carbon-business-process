function authenticate(username, password){
	var carbon = require('carbon');
	srv = new carbon.server.Server({url: "https://localhost:9443/admin"});
	return srv.authenticate(username, password);
}