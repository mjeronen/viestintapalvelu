"use strict";

var SERVICE_REST_PATH = "../../ajastuspalvelu-service/api/v1/";

var ajastusApp = angular.module('ajastuspalvelu',
    [ 'ngResource', 'ngRoute', 'ngAnimate', 'ui.bootstrap', 'localization']);

ajastusApp.config([ '$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/etusivu', {
            controller: 'TaskListController',
            templateUrl: 'partials/frontpage.html'
        })
        .when('/create', {
            controller: 'CreateTaskController',
            templateUrl: 'partials/createtask.html'
        })
        .when('/edit/:task', {
            controller: 'EditTaskController',
            templateUrl: 'partials/edittask.html'
        })
        .otherwise({
            redirectTo: '/etusivu'
        });
}]);

