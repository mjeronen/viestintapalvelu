var app = angular.module('app', ['ngResource', 'ui.tinymce', 'ui.bootstrap']);

window.CONFIG = {};
window.CONFIG.env = {};
window.CONFIG.app = {};

function getCurrentHost() {
    var url = window.location.href;
    var arr = url.split("/");
    return arr[0] + "//" + arr[2]; // should return e.g. http(s)://host:port
}

app.value("globalConfig", window.CONFIG || {});

app.config(function ($locationProvider) {
    $locationProvider.html5Mode(true).hashPrefix('!');
});

app.config(function($sceProvider) {
	// Control SCE. Do not disable SCE ever in new projects.
	// This project had some problems when migrating from angular 1.0.x -> 1.2
	// and disabling SCE helped during migration. Now enabled again. 
	
	$sceProvider.enabled(true); // 
}).factory('NoCacheInterceptor', function () {
    return {
        request: function (config) {
            if (config.method && config.method == 'GET' && config.url.indexOf('html') === -1){
                var separator = config.url.indexOf('?') === -1 ? '?' : '&';
                config.url = config.url+separator+'noCache=' + new Date().getTime();
            }
            return config;
        }
    };
}).config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('NoCacheInterceptor');
}]);