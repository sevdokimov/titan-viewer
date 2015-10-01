titanViewApp.controller('vertexCtrl', function ($scope, $http, $routeParams) {
    $scope.vId = $routeParams.vId

    $http.get("/data/vertex", {params: {vId: $routeParams.vId, table: $routeParams.table}}).then(function (response) {
        $scope.v = response.data
    })

    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "in", table: $routeParams.table}})
        .then(function (response) {
            $scope.inE = response.data

        })
    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "out", table: $routeParams.table}})
        .then(function (response) {
            $scope.outE = response.data
        })
});

titanViewApp.controller('vertexListCtrl', function ($scope, $http, $routeParams) {
    $scope.vertexes = null

    $http.get("/data/vertexList", {params: {table: $routeParams.table}}).then(function (response) {
        var data = response.data

        $scope.vertexes = data.vertexes
    })
});

titanViewApp.controller('selectGraphCtrl', function ($scope, $http) {
    $http.get("/data/tableList").then(function (response) {
        $scope.tableList = response.data
        $scope.errorMsg = null
    })
});
