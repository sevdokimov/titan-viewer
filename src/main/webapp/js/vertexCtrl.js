titanViewApp.controller('vertexCtrl', function ($scope, $http, $routeParams) {
    $scope.vId = $routeParams.vId

    $http.get("/data/vertex", {params: {vId: $routeParams.vId}}).then(function (response) {
        $scope.v = response.data
    })

    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "in"}})
        .then(function (response) {
            $scope.inE = response.data

        })
    $http.get("/data/vertexEdgesAllLabels", {params: {vId: $routeParams.vId, dir: "out"}})
        .then(function (response) {
            $scope.outE = response.data
        })
});

titanViewApp.controller('vertexList', function ($scope, $http) {
    $http.get("/data/vertexList").then(function (response) {
        var data = response.data

        $scope.vertexes = data.vertexes
    })
});
