hbaseViewer.controller('tablesCtrl', function ($scope, $http, $routeParams) {
    $scope.ns = $routeParams.ns

    $http.get("/hbasedata/listTables", {params: {ns: $routeParams.ns}}).then(function (response) {
        $scope.tableList = response.data
    }, httpErrorHandler)
});
