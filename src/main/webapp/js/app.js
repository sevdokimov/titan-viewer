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
            when('/g/:table/vertexList', {
                templateUrl: 'vertexList.html',
                controller: 'vertexListCtrl'
            }).
            otherwise({
                redirectTo: '/'
            });
    }]);
