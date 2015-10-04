titanViewApp.controller('consoleCtrl', function ($scope, $http, $routeParams, $window) {
    $scope.table = $routeParams.table

    if (!$scope.table || $scope.table.length == 0) throw "Empty table name"

    var savedScript = $window.localStorage['gremlinScript_' + $scope.table]

    if (!savedScript) {
        savedScript = 'g.V'
    }

    $window.editor.setValue(savedScript)
    $window.editor.selection.clearSelection()

    if (savedScript == 'g.V') {
        $scope.loading = true
        $scope.currentQuery = 'g.V'

        $http.get("/data/vertexList", {params: {table: $routeParams.table}}).then(function (response) {
            $scope.res = response.data
            $scope.loading = false
        }, function(response) {
            window.location = "/remoteError.html"
        })
    }

    $scope.executeQuery = function() {
        var editor = $window.editor

        if (!editor) return

        var query = editor.getValue().trim()

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

    $window.addEventListener('beforeunload', function() {
        var editor = $window.editor

        if (editor) {
            $window.localStorage['gremlinScript_' + $scope.table] = editor.getValue()
        }
    });
});
