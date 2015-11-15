/**
 * @constructor
 * @param name {string}
 * @param f {function}
 * @param description {string}
 * @param supportedAttrs {string[]}
 */
function Renderer(name, f, description, supportedAttrs) {
    this.name = name
    this.f = f
    this.description = description ? description : name
    this.supportedAttrs = supportedAttrs ? supportedAttrs : []
}

/**
 * @param m {string}
 * @param attr {RendererAttr}
 * @return {string}
 */
Renderer.prototype.render = function(m, attr) {
    if (!m)
        return '<span class="dNull">null</span>'

    if (!attr)
        attr = new RendererAttr()

    var res = this.f(m, attr)

    if (res == null) {
        res = hexFormatter(m, attr)
    }

    return res
}

/**
 * @param m {string}
 * @param attr {RendererAttr}
 * @return {string}
 */
function hexFormatter(m, attr) {
    var trimmed = false

    var maxLength = attr.maxLength

    if (!maxLength)
        maxLength = 30
    else {
        maxLength = parseInt(maxLength)
    }

    if (maxLength > 0) {
        if (m.length > maxLength * 2) {
            m = m.substr(0, maxLength * 2)
            trimmed = true
        }
    }

    var res = []
    res.push("<span class='dHex")

    if (attr.noWrap) {
        res.push(" noWrap")
    }

    res.push("'>")

    res.push(m)

    res.push("</span>")

    if (trimmed)
        res.push('<span class="showMore">...</span>')

    return res.join('')
}

var hexRenderer = new Renderer("hex", hexFormatter, null, ['maxLength', 'noWrap'])

var stringRenderer = new Renderer("string", function(m, attr) {
    var res = []

    var trimmed = false

    var maxLength = attr.maxLength

    if (!maxLength)
        maxLength = 30
    else {
        maxLength = parseInt(maxLength)
    }

    if (maxLength > 0) {
        if (m.length > maxLength * 2) {
            m = m.substr(0, maxLength * 2)
            trimmed = true
        }
    }

    res.push("<span class='dStr")

    if (attr.noWrap) {
        res.push(" noWrap")
    }

    res.push("'>")

    for (var i = 0; i < m.length; i += 2) {
        var x = String.fromCharCode(parseInt(m.substr(i, 2), 16))

        if (x == '<')
            res.push('&lt;')
        else if (x == '>')
            res.push('&gt;')
        else if (x == '&')
            res.push('&amp;')
        else
            res.push(x)
    }

    res.push("</span>")

    if (trimmed) {
        res.push('<span class="showMore">...</span>')
    }

    return res.join('')
}, null, ['maxLength', 'noWrap'])

var boolRenderer = new Renderer("boolean", function(m, attr) {
    if (m == '00') {
        return '<span class="dBool">true</span>'
    }
    else if (m == '01') {
        return '<span class="dBool">false</span>'
    }
    else {
        return null
    }
}, null, [])

var intRenderer = new Renderer("int", function(m, attr) {
    if (m.length != 4 * 2)
        return null;

    var x = parseInt(m, 16)

    return '<span class="dInt">' + x + '</span>'
}, null, [])

var shortRenderer = new Renderer("short", function(m, attr) {
    if (m.length != 2 * 2)
        return null;

    var x = parseInt(m, 16)

    if (attr.phoenixSign) {
        x = x ^ 0x8000
    }

    return '<span class="dInt">' + x + '</span>'
}, null, ['phoenixSign'])

var longRenderer = new Renderer("long", function(m, attr) {
    if (m.length != 8 * 2)
        return null;

    if (attr.phoenixSign) {
        var x1 = parseInt(m.substr(0, 8), 16)
        var x0 = parseInt(m.substr(8, 8), 16)

        x1 = x1 ^ 0x80000000

        var x = x1 * 0x100000000 + x0
    }
    else {
        x = parseInt(m, 16)
    }

    return '<span class="dInt">' + x + '</span>'
}, null, ['phoenixSign'])

var floatRenderer = new Renderer("float", function(m, attr) {
    if (m.length != 4 * 2)
        return null;

    var x = parseInt(m, 16)

    var buffer = new ArrayBuffer(4);
    var intView = new Int32Array(buffer);
    var floatView = new Float32Array(buffer);

    if (attr.phoenixSign) {
        x = x ^ 0x80000000
    }

    intView[0] = x

    var res = floatView[0]

    return '<span class="dFloat">' + res + '</span>'
}, null, ['phoenixSign'])

var doubleRenderer = new Renderer("double", function(m, attr) {
    if (m.length != 8 * 2)
        return null;

    var x1 = parseInt(m.substr(0, 4 * 2), 16)
    var x2 = parseInt(m.substr(4 * 2, 4 * 2), 16)

    var buffer = new ArrayBuffer(8);
    var intView = new Int32Array(buffer);
    var floatView = new Float64Array(buffer);

    if (attr.phoenixSign) {
        x1 = x1 ^ 0x80000000
    }

    intView[1] = x1
    intView[0] = x2

    var res = floatView[0]

    return '<span class="dFloat">' + res + '</span>'
}, null, ['phoenixSign'])

var dateRenderer = new Renderer("date", function(m, attr) {
    if (m.length != 8 * 2)
        return null;

    if (!attr.phoenixSign) {
        var x = parseInt(m, 16)
    } else {
        var x1 = parseInt(m.substr(0, 8), 16)
        var x0 = parseInt(m.substr(8, 8), 16)

        x1 = x1 ^ 0x80000000

        x = x1 * 0x100000000 + x0
    }

    var date = new Date(x);

    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var min = date.getMinutes();
    var sec = date.getSeconds();

    month = (month < 10 ? "0" : "") + month;
    day = (day < 10 ? "0" : "") + day;
    hour = (hour < 10 ? "0" : "") + hour;
    min = (min < 10 ? "0" : "") + min;
    sec = (sec < 10 ? "0" : "") + sec;

    var str = date.getFullYear() + "-" + month + "-" + day + " " +  hour + ":" + min + ":" + sec;

    return '<span class="dDate">' + str + '</span>'
}, null, ['phoenixSign'])

var timestampRenderer = new Renderer("timestamp", function(m, attr) {
    if (m.length != 8 * 2 + 4 * 2)
        return null;

    if (!attr.phoenixSign) {
        var x = parseInt(m, 16)
    } else {
        var x1 = parseInt(m.substr(0, 8), 16)
        var x0 = parseInt(m.substr(8, 8), 16)

        x1 = x1 ^ 0x80000000

        x = x1 * 0x100000000 + x0
    }

    var date = new Date(x);

    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var min = date.getMinutes();
    var sec = date.getSeconds();

    month = (month < 10 ? "0" : "") + month;
    day = (day < 10 ? "0" : "") + day;
    hour = (hour < 10 ? "0" : "") + hour;
    min = (min < 10 ? "0" : "") + min;
    sec = (sec < 10 ? "0" : "") + sec;

    var str = date.getFullYear() + "-" + month + "-" + day + " " +  hour + ":" + min + ":" + sec;

    return '<span class="dDate">' + str + '</span>'
}, "timestamp (date with nanoseconds)", ['phoenixSign'])

var allRenderers = [hexRenderer, stringRenderer, boolRenderer,
    intRenderer, shortRenderer, longRenderer,
    floatRenderer, doubleRenderer,
    dateRenderer, timestampRenderer]

var renderersMap = {}

function initRenders() {
    for (var i = 0; i < allRenderers.length; i++) {
        renderersMap[allRenderers[i].name] = allRenderers[i]
    }
}

initRenders()

/**
 * @param name {string}
 * @param def {Renderer}
 * @return {Renderer}
 */
function findRendererByName(name, def) {
    var res = renderersMap[name]
    return res ? res : def
}

/**
 * @constructor
 */
function RendererAttr() {

}

/**
 * @type {number}
 */
RendererAttr.prototype.maxLength = null

/**
 * @type {number}
 */
RendererAttr.prototype.noWrap = null
