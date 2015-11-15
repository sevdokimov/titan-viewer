hbaseViewer.controller('tableContentCtrl', function ($scope, $http, $routeParams, $uibModal) {
    var table = $routeParams.table

    $scope.table = table

    var idx = table.indexOf(':')
    if (idx >= 0) {
        $scope.namespace = table.substring(0, idx)
        $scope.simpleTableName = table.substring(idx + 1)
    }

    $scope.startKey = null

    $http.get("/hbasedata/firstScan", {params: {table: $routeParams.table}}).then(function (response) {
        var f = response.data.table.families

        /** @type {Family[]} */
        $scope.families = []

        for (var i = 0; i < f.length; i++) {
            $scope.families.push(new Family(f[i]))
        }

        $scope.nextRowKey = response.data.scan.nextRowKey

        $scope.data = []

        $scope.tableView = response.data.tableView

        $scope.keyFormat = {
            renderer: findRendererByName($scope.tableView.key.rendererName, hexRenderer),
            rendererAttr: safeParseJson($scope.tableView.key.rendererAttr)
        }

        if (!$scope.keyFormat.rendererAttr) {
            $scope.keyFormat.rendererAttr = {maxLength: "0", noWrap: true}
        }

        mergeRows($scope, response.data.scan.rows)
    }, function() {
        window.location = "/remoteError.html"
    })

    $scope.loadNext = function() {
        if (!$scope.nextRowKey)
            return

        loadRows($scope, $http, $scope.nextRowKey, $routeParams.table)
    }

    $scope.showSelectTypeDialog = function(col) {
        var modalInstance = $uibModal.open({
            templateUrl: 'selectTypeDialog.html',
            controller: 'columnPropsCtrl',
            resolve: {
                col: function () {
                    return col;
                },
                table: function() {
                    return $scope.table
                }
            }
        });
    }

    $scope.showSelectIdTypeDialog = function() {
        var modalInstance = $uibModal.open({
            templateUrl: 'selectIdTypeDialog.html',
            controller: 'keyPropsCtrl',
            resolve: {
                keyFormat: function () {
                    return $scope.keyFormat;
                },
                table: function() {
                    return $scope.table
                }
            }
        });
    }

    $scope.nonEmptyFamily = nonEmptyFamily

    $scope.showMore = function(event) {
        var target = event.target

        if (target.tagName != 'SPAN' || target.className != 'showMore') return;

        do {
            target = target.parentNode

            if (!target) return
        } while (target.tagName != 'TD')

        var tr = target.parentNode

        var rowIndex = tr.getAttribute('rowIndex')

        var colIdx = 0

        for (var currTd = target; currTd.previousElementSibling != null; currTd = currTd.previousElementSibling) {
            colIdx++
        }

        var data = $scope.data[parseInt(rowIndex)]

        var value
        var col
        var rowKey = data.key

        if (colIdx == 0) {
            value = rowKey
        }
        else {
            col = findColumn($scope.families, colIdx - 1)

            if (!col)
                return

            value = data.data[col.family.name][col.q]
        }

        var modalInstance = $uibModal.open({
            templateUrl: 'showCellDialog.html',
            controller: 'showCellCtrl',
            size: 'lg',
            resolve: {
                table: function () {
                    return $scope.table;
                },
                column: function() {
                    return col
                },
                rowKey: function() {
                    return rowKey
                },
                value: function() {
                    return value
                },
                keyFormat: function() {
                    return $scope.keyFormat
                }
            }
        });
    }
});

/**
 * @param famelies {Family[]}
 * @param index {number}
 * @return {Column}
 */
function findColumn(famelies, index) {
    for (var i = 0; i < famelies.length; i++) {
        if (famelies[i].shown) {
            if (famelies[i].columns.length <= index) {
                index -= famelies[i].columns.length
            }
            else {
                return famelies[i].columns[index]
            }
        }
    }

    return null;
}

function mergeRows($scope, rows) {
    var columnsChanged = false

    for (var i = 0; i < rows.length; i++) {
        var row = rows[i]
        $scope.data.push(row)

        for (var k = 0; k < $scope.families.length; k++) {
            var f = $scope.families[k];

            var fData = row.data[f.name]

            if (fData) {
                for (var q in fData) {
                    if (fData.hasOwnProperty(q)) {
                        var col = f.createColumnIfAbsent(q)

                        if (col) {
                            var colSettings = $scope.tableView.columns[f.name + ':' + q]

                            if (colSettings != null) {
                                if (colSettings.rendererName) {
                                    var renderer = renderersMap[colSettings.rendererName]

                                    if (renderer) {
                                        col.renderer = renderer
                                    }
                                }

                                if (colSettings.rendererAttr) {
                                    col.rendererAttr = JSON.parse(colSettings.rendererAttr)
                                }
                            }

                            columnsChanged = true
                        }
                    }
                }
            }
        }
    }

    if (columnsChanged) {
        var columns = []

        for (i = 0; i < $scope.families.length; i++) {
            columns = columns.concat($scope.families[i].columns)
        }

        $scope.columns = columns
    }
}

function loadRows($scope, $http, startRow, table) {
    $http.get("/hbasedata/scan", {params: {table: table, startRow: startRow}}).then(function (response) {
        var res = response.data

        $scope.nextRowKey = res.nextRowKey

        mergeRows($scope, res.rows)
    }, function() {
        window.location = "/remoteError.html"
    })
}

hbaseViewer.controller('keyPropsCtrl', function ($scope, $http, $uibModalInstance, table, keyFormat) {
    $scope.rendererName = keyFormat.renderer.name
    $scope.rendererAttr = angular.copy(keyFormat.rendererAttr);

    $scope.allRenderers = allRenderers
    $scope.renderersMap = renderersMap

    $scope.ok = function () {
        var renderer = renderersMap[$scope.rendererName]

        if (!renderer)
            throw "Renderer not found: " + $scope.rendererName

        keyFormat.renderer = renderer
        keyFormat.rendererAttr = $scope.rendererAttr

        $http.get("/hbasedata/keyRendererChanged",
            {params: {table: table,
                rendererName: $scope.rendererName, rendererAttr: JSON.stringify($scope.rendererAttr)}})

        $uibModalInstance.close();
    };
    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
})

hbaseViewer.controller('columnPropsCtrl', function ($scope, $http, $uibModalInstance, table, col) {
    $scope.col = col

    $scope.rendererName = col.renderer.name
    $scope.rendererAttr = angular.copy(col.rendererAttr);

    $scope.allRenderers = allRenderers
    $scope.renderersMap = renderersMap

    $scope.ok = function () {
        var renderer = renderersMap[$scope.rendererName]

        if (!renderer)
            throw "Renderer not found: " + $scope.rendererName

        col.renderer = renderer
        col.rendererAttr = $scope.rendererAttr

        $http.get("/hbasedata/columnRendererChanged",
            {params: {table: table,
                family: col.family.name, q: col.q,
                rendererName: $scope.rendererName, rendererAttr: JSON.stringify($scope.rendererAttr)}})

        $uibModalInstance.close();
    };
    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
});

hbaseViewer.controller('showCellCtrl', function ($scope, $http, $uibModalInstance, table, keyFormat, rowKey, column, value) {
    $scope.column = column

    $scope.value = value

    $scope.render = function(value) {
        var attr = {maxLength: '0'}

        if (!column) {
            return keyFormat.renderer.render(value, attr)
        }
        else {
            return column.renderer.render(value, attr)
        }
    }

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
});
