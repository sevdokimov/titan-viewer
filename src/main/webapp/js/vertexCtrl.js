titanViewApp.controller('vertexCtrl', function ($scope, $http, $routeParams) {
    $scope.table = $routeParams.table
    $scope.vId = $routeParams.vId

    $http.get("/data/vertex", {params: {vId: $routeParams.vId, table: $routeParams.table}}).then(function (response) {
        $scope.v = response.data
    }, httpErrorHandler)

    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "in", table: $routeParams.table}})
        .then(function (response) {
            $scope.inE = response.data

        }, httpErrorHandler)
    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "out", table: $routeParams.table}})
        .then(function (response) {
            $scope.outE = response.data
        }, httpErrorHandler)
});

titanViewApp.controller('selectGraphCtrl', function ($scope, $http, $location) {
    $http.get("/data/tableList").then(function (response) {
        $scope.titanTables = response.data.titanTables

        $scope.hbaseTables = response.data.hbaseTables
    }, httpErrorHandler)
});
