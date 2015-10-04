titanViewApp.controller('consoleCtrl', function ($scope, $http, $routeParams) {
    $scope.table = $routeParams.table

    $scope.loading = true
    $scope.currentQuery = 'g.V'

    $http.get("/data/vertexList", {params: {table: $routeParams.table}}).then(function (response) {
        $scope.res = response.data
        $scope.loading = false
    }, function(response) {
        window.location = "/remoteError.html"
    })

    $scope.executeQuery = function() {
        var query = $('#queryEditor')[0].editorInstance.getValue().trim()

        if (query == "" || $scope.loading) {
            return
        }

        $scope.currentQuery = query
        $scope.loading = true

        $scope.res = null

        $http.get("/data/executeQuery", {params: {table: $routeParams.table, query: query}})
            .then(function (response) {
                $scope.res = response.data
                $scope.loading = false
            }, function (response) {
                window.location = "/remoteError.html"
            })
    }

    $scope.typeOf = function(o) {
        if (o === undefined || o === null) return 'null';
        if (typeof o === 'number') return 'number';
        if (typeof o === 'string') return 'string';
        if (typeof o === 'boolean') return 'boolean';

        if (typeof o === 'object') {
            var type = o._type_
            return type ? type : 'o'
        }

        throw "Unknown type";
    }

    $scope.stringify = function(o) {
        return JSON.stringify(o)
    }
});
