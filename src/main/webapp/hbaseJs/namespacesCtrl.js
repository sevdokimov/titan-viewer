hbaseViewer.controller('namespaceCtrl', function ($scope, $http, $routeParams) {

    $http.get("/hbasedata/listNamespaces").then(function (response) {
        $scope.nsList = response.data
    }, httpErrorHandler)
});
