
function Family(name) {
    this.name = name
    this.columns = []
    this.columnMap = {}
}

/**
 * @type {boolean}
 */
Family.prototype.shown = true

/**
 * @type {Column[]}
 */
Family.prototype.columns = []

/**
 * @type {Object}
 */
Family.prototype.columnMap = {}

/**
 * @param name {string}
 * @return {Column}
 */
Family.prototype.createColumnIfAbsent = function(name) {
    var col = this.columnMap[name]
    if (col)
        return null

    col = new Column(name, this)
    this.columnMap[name] = col
    this.columns.push(col)
    this.columns.sort(function (c1, c2) {
        if (c1.q > c2.q)
            return 1
        else if (c1.q < c2.q)
            return -1
        return 0
    })
    return col
}

/**
 * @param q {string}
 * @param family {Family}
 * @constructor
 */
function Column(q, family) {
    this.q = q
    this.family = family
    this.rendererAttr = new RendererAttr()
}

/**
 * @type {boolean}
 */
Column.prototype.shown = true

/**
 * @type {Renderer}
 */
Column.prototype.renderer = hexRenderer

/**
 * @param m {string}
 * @return {string}
 */
Column.prototype.render = function(m) {
    return this.renderer.render(m, this.rendererAttr)
}

/**
 * @param family {Family}
 */
function nonEmptyFamily(family) {
    return family.columns.length > 0
}

function safeParseJson(json, def) {
    if (!json)
        return null

    return JSON.parse(json)
}