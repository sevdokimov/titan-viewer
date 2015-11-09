titanViewApp.controller('vertexCtrl', function ($scope, $http, $routeParams) {
    $scope.table = $routeParams.table
    $scope.vId = $routeParams.vId

    $http.get("/data/vertex", {params: {vId: $routeParams.vId, table: $routeParams.table}}).then(function (response) {
        $scope.v = response.data
    }, function(response) {
        window.location = "/remoteError.html"
    })

    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "in", table: $routeParams.table}})
        .then(function (response) {
            $scope.inE = response.data

        }, function(response) {
            window.location = "/remoteError.html"
        })
    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "out", table: $routeParams.table}})
        .then(function (response) {
            $scope.outE = response.data
        }, function(response) {
            window.location = "/remoteError.html"
        })
});

titanViewApp.controller('selectGraphCtrl', function ($scope, $http, $location) {
    $http.get("/data/tableList").then(function (response) {
        $scope.titanTables = response.data.titanTables

        $scope.hbaseTables = response.data.hbaseTables
    }, function(response) {
        window.location = "/remoteError.html"
    })
});
