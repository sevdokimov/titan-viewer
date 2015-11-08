hbaseViewer.controller('namespaceCtrl', function ($scope, $http, $routeParams) {

    $http.get("/hbasedata/listNamespaces").then(function (response) {
        $scope.nsList = response.data
    }, function(response) {
        window.location = "/remoteError.html"
    })
});
