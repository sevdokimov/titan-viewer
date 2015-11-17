hbaseViewer.controller('tableComparatorCtrl', function ($scope, $http, $routeParams) {
    $scope.ns1 = $routeParams.ns1
    $scope.ns2 = $routeParams.ns2

    $scope.tableName1 = $routeParams.tableName1
    $scope.tableName2 = $routeParams.tableName2

    $http.get("/hbasedata/compare", {params: {table1: $scope.ns1 + ':' + $scope.tableName1,
        table2: $scope.ns2 + ':' + $scope.tableName2}}).then(function (response) {
        $scope.result = response.data
    }, function(response) {
        window.location = "/remoteError.html"
    })
});
