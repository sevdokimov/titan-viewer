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
                controller: 'tableContentCtrl',
                reloadOnSearch: false
            }).
            when('/compare/:ns1/:tableName1/:ns2/:tableName2', {
                templateUrl: 'hbaseHtml/tableCompare.html',
                controller: 'tableComparatorCtrl'
            }).
            otherwise({
                redirectTo: '/'
            });
    }]);
