function QueryHistory(text) {
    if (text) {
        var data = JSON.parse(text)

        if (data.list && data.cur) {
            this.list = data.list
            this.loadedHistorySize = this.list.length
            this.currentQuery = data.cur
        }
    }
}

/**
 * @type {int}
 */
QueryHistory.prototype.loadedHistorySize = 0

/**
 * @type {Array}
 */
QueryHistory.prototype.list = []

/**
 * @type {string}
 */
QueryHistory.prototype.currentQuery = "g.V"

/**
 * @return {int}
 */
QueryHistory.prototype.size = function() {
    return this.list.length
}

/**
 * @return {string}
 */
QueryHistory.prototype.lastQuery = function() {
    if (this.list.length == 0)
        return null;

    return this.list[this.list.length - 1]
}

/**
 * @return {string}
 */
QueryHistory.prototype.toString = function() {
    var data = {list: this.list, cur: this.currentQuery}

    return JSON.stringify(data)
}

/**
 * @param query {string}
 */
QueryHistory.prototype.addQuery = function(query) {
    var idx = this.list.indexOf(query)

    if (idx != -1) {
        this.list.splice(idx, 1);
    }

    this.list.push(query)
}

/**
 * @param oldHistory {QueryHistory}
 */
QueryHistory.prototype.mergeHistory = function(oldHistory) {
    var savedList = this.list;

    var newQueryCount = this.list.length - this.loadedHistorySize

    this.list = oldHistory.list.slice()

    for (var i = this.loadedHistorySize; i < savedList.length; i++) {
        this.addQuery(savedList[i])
    }

    this.loadedHistorySize = this.list.length - newQueryCount
}

/**
 * @param maxQueryCount {int}
 */
QueryHistory.prototype.trimHistory = function(maxQueryCount) {
    if (this.list.length > maxQueryCount) {
        var deleteCount = this.list.length - maxQueryCount

        this.list.splice(0, deleteCount)

        this.list.loadedHistorySize -= deleteCount

        if (this.list.loadedHistorySize < 0) {
            this.list.loadedHistorySize = 0
        }
    }
}
