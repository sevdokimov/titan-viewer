'use strict';

var titanViewApp = angular.module('titanViewApp', ['ngRoute', 'ngSanitize']);

titanViewApp.config(['$routeProvider',
    function($routeProvider) {

        $routeProvider.
            when('/', {
                templateUrl: 'selectGraph.html',
                controller: 'selectGraphCtrl'
            }).
            when('/g/:table/vertex/:vId', {
                templateUrl: 'vertex.html',
                controller: 'vertexCtrl'
            }).
            when('/g/:table/console', {
                templateUrl: 'console.html',
                controller: 'consoleCtrl'
            }).
            otherwise({
                redirectTo: '/'
            });
    }]);

var wasHttpError

var httpErrorHandler = function(response) {
    if (wasHttpError)
        return

    wasHttpError = true

    $('body').html(response.data)
}
