/**
 * @param table {String}
 * @return {QueryHistory}
 */
function loadHistory(table) {
    return new QueryHistory(localStorage['gremlinScript_' + table])
}

/**
 * @param table {String}
 * @param history {QueryHistory}
 */
function saveHistory(table, history) {
    localStorage['gremlinScript_' + table] = history.toString()
}

titanViewApp.controller('consoleCtrl', function ($scope, $http, $routeParams, $window) {
    $scope.table = $routeParams.table

    if (!$scope.table || $scope.table.length == 0) throw "Empty table name"

    $scope.history = loadHistory($scope.table)
    $scope.historyPos = $scope.history.size()

    var query = $scope.history.currentQuery;

    $window.editor.setValue(query)
    $window.editor.selection.clearSelection()

    if (query == 'g.V') {
        $scope.loading = true
        $scope.currentQuery = 'g.V'

        $http.get("/data/vertexList", {params: {table: $routeParams.table}}).then(function (response) {
            $scope.res = response.data
            $scope.loading = false
        }, function(response) {
            window.location = "/remoteError.html"
        })
    }
    else {
        $http.get("/data/openGraph", {params: {table: $routeParams.table}}).then(function (response) {

        })
    }

    $scope.executeQuery = function() {
        var editor = $window.editor

        if (!editor) return

        var query = editor.getValue().trim()

        if (query == "" || $scope.loading) {
            return
        }

        $scope.history.addQuery(query)
        $scope.historyPos = $scope.history.size()

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
            $scope.history.currentQuery = editor.getValue()

            var oldHistory = loadHistory($scope.table)

            $scope.history.mergeHistory(oldHistory)
            $scope.history.trimHistory(30)
            saveHistory($scope.table, $scope.history)
        }
    });

    $scope.prevQuery = function() {
        if ($scope.historyPos == 0)
            return

        var editor = $window.editor
        var q = editor.getValue()

        if ($scope.historyPos < $scope.history.size()) {
            if (q != $scope.history.list[$scope.historyPos]) {
                $scope.history.currentQuery = q;
            }

            $scope.historyPos--
        }
        else {
            $scope.history.currentQuery = q;

            $scope.historyPos = $scope.history.size() - 1

            if ($scope.historyPos > 0 && $scope.history.list[$scope.historyPos] == q) {
                $scope.historyPos--
            }
        }

        editor.setValue($scope.history.list[$scope.historyPos])
        editor.selection.clearSelection()
    }

    $scope.nextQuery = function() {
        if ($scope.historyPos >= $scope.history.size())
            return

        var editor = $window.editor

        var q = editor.getValue()

        if (q != $scope.history.list[$scope.historyPos]) {
            $scope.history.currentQuery = q;
        }

        if ($scope.historyPos == $scope.history.size() - 1) {
            $scope.historyPos = $scope.history.size()

            editor.setValue($scope.history.currentQuery)
            editor.selection.clearSelection()
        }
        else {
            $scope.historyPos++

            editor.setValue($scope.history.list[$scope.historyPos])
            editor.selection.clearSelection()
        }
    }

    $scope.showHistory = function() {
        var hTable = $('#historyTable').empty()

        var list = $scope.history.list;

        if (list.length == 0) {
            hTable.append("History is empty")
        }
        else {
            for (var i = list.length - 1; i >= 0; i--) {
                var query = list[i]

                var a = $('<a href="#">select</a>')
                a.click(function() {
                    var selectedQuery = $('.query', $(this).parent()).text()

                    $('#historyDialog').modal('hide')

                    var editor = $window.editor

                    editor.setValue(selectedQuery)
                    editor.selection.clearSelection()

                    $scope.historyPos = $scope.history.size()

                    return false
                })

                var line = $("<div class='historyLine'></div>");
                line.append(a).append(' ').append($("<span class='query'></span>").text(query))

                hTable.append(line)
            }
        }

        $('#historyDialog').modal()
    }
});
