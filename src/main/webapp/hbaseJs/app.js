'use strict';

var hbaseViewer = angular.module('hbaseViewer', ['ngRoute', 'ngSanitize', 'ui.bootstrap']);

hbaseViewer.config(['$routeProvider',
    function($routeProvider) {

        $routeProvider.
            when('/', {
                templateUrl: 'hbaseHtml/namespaces.html',
                controller: 'namespaceCtrl'
            }).
            when('/n/:ns', {
                templateUrl: 'hbaseHtml/tables.html',
                controller: 'tablesCtrl'
            }).
            when('/t/:table', {
                templateUrl: 'hbaseHtml/tableContent.html',
                controller: 'tableContentCtrl'
            }).
            otherwise({
                redirectTo: '/'
            });
    }]);
