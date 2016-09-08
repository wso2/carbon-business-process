/*
 ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
*/
function authenticate(username, password, bpsUrl){
   var 	carbon = require('carbon'),
	process = require('process'),
	srv = new carbon.server.Server({url: bpsUrl});
	return srv.authenticate(username, password);
}

function getRoles(username, password, bpsUrl){
   var 	carbon = require('carbon'),
	process = require('process'),
	srv = new carbon.server.Server({url: bpsUrl}),
	tenantId = carbon.server.tenantId(),
	userManager = new carbon.user.UserManager(srv, tenantId),
	user = new carbon.user.User(userManager, username),
	roles = user.getRoles();
	return roles;
}

function isUserAuthorized(username, permission, bpsUrl){
   var 	carbon = require('carbon'),
	process = require('process'),
	srv = new carbon.server.Server({url: bpsUrl}),
	tenantId = carbon.server.tenantId(),
	userManager = new carbon.user.UserManager(srv, tenantId);

	//get all permissions that 
	var permissionList = userManager.getAllowedUIResources(username, permission);
	if (permissionList != null && permissionList.length > 0) {
		return true;
	}
	return false;

}

function getAllowedUIResources (username, permissionRootPath, bpsUrl) {
	var 	carbon = require('carbon'),
	process = require('process'),
	srv = new carbon.server.Server({url: bpsUrl}),
	tenantId = carbon.server.tenantId(),
	userManager = new carbon.user.UserManager(srv, tenantId);

	//get all permissions that 
	return userManager.getAllowedUIResources(username, permissionRootPath);
}

function isPermissionExist(permissionArray, path) {
	for (var i = 0; i < permissionArray.length; i++) {
		if (permissionArray[i] == path || permissionArray[i] === path) {
			return true;
		}
	}
	return false;
}

