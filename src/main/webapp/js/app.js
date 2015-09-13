'use strict';

var titanViewApp = angular.module('titanViewApp', ['ngRoute', 'ngSanitize']);

titanViewApp.config(['$routeProvider',
    function($routeProvider) {

        $routeProvider.
            when('/vertex/:vId', {
                templateUrl: 'vertex.html',
                controller: 'vertexCtrl'
            }).
            when('/vertexList', {
                templateUrl: 'vertexList.html',
                controller: 'vertexList'
            }).
            otherwise({
                redirectTo: '/vertexList'
            });
    }]);
