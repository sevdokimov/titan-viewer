/**
 * @constructor
 * @param name {string}
 * @param f {function}
 * @param description {string}
 */
function Renderer(name, f, description) {
    this.name = name
    this.f = f
    this.description = description ? description : name
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

    if (maxLength > 0) {
        if (m.length > maxLength * 2) {
            m = m.substr(0, maxLength * 2)
            trimmed = true
        }
    }

    var res = []
    res.push("<span class='dHex'>")

    res.push(m)

    res.push()
    res.push("</span>")

    if (trimmed)
        res.push('...')

    return res.join('')
}

var hexRenderer = new Renderer("hex", hexFormatter)

var stringRenderer = new Renderer("string", function(m, attr) {
    var res = []

    var trimmed = false

    var maxLength = attr.maxLength
    if (!maxLength)
        maxLength = 30

    if (maxLength > 0) {
        if (m.length > maxLength * 2) {
            m = m.substr(0, maxLength * 2)
            trimmed = true
        }
    }

    res.push("<span class='dStr'>")

    for (var i = 0; i < m.length; i += 2) {
        var x = parseInt(m.substr(i, 2), 16)
        res.push(String.fromCharCode(x))
    }

    res.push("</span>")

    if (trimmed) {
        res.push('...')
    }

    return res.join('')
})

var boolRenderer = new Renderer("boolean", function(m) {
    if (m == '00') {
        return '<span class="dBool">true</span>'
    }
    else if (m == '01') {
        return '<span class="dBool">false</span>'
    }
    else {
        return null
    }
})

var intRenderer = new Renderer("int", function(m) {
    if (m.length != 4 * 2)
        return null;

    var x = parseInt(m, 16)

    return '<span class="dInt">' + x + '</span>'
})

var longRenderer = new Renderer("long", function(m) {
    if (m.length != 8 * 2)
        return null;

    var x = parseInt(m, 16)

    return '<span class="dInt">' + x + '</span>'
})

var floatRenderer = new Renderer("float", function(m) {
    if (m.length != 4 * 2)
        return null;

    var x = parseInt(m, 16)

    var buffer = new ArrayBuffer(4);
    var intView = new Int32Array(buffer);
    var floatView = new Float32Array(buffer);

    intView[0] = x

    var res = floatView[0]

    return '<span class="dFloat">' + res + '</span>'
})

var floatPRenderer = new Renderer("floatP", function(m) {
    if (m.length != 4 * 2)
        return null;

    var x = parseInt(m, 16)

    var buffer = new ArrayBuffer(4);
    var intView = new Int32Array(buffer);
    var floatView = new Float32Array(buffer);

    x ^= 0x80000000

    intView[0] = x

    var res = floatView[0]

    return '<span class="dFloat">' + res + '</span>'
}, 'float (phoenix)')

var doubleRenderer = new Renderer("double", function(m) {
    if (m.length != 8 * 2)
        return null;

    var x1 = parseInt(m.substr(0, 4 * 2), 16)
    var x2 = parseInt(m.substr(4 * 2, 4 * 2), 16)

    var buffer = new ArrayBuffer(8);
    var intView = new Int32Array(buffer);
    var floatView = new Float64Array(buffer);

    intView[1] = x1
    intView[0] = x2

    var res = floatView[0]

    return '<span class="dFloat">' + res + '</span>'
})

var doublePRenderer = new Renderer("doubleP", function(m) {
    if (m.length != 8 * 2)
        return null;

    var x1 = parseInt(m.substr(0, 4 * 2), 16)
    var x2 = parseInt(m.substr(4 * 2, 4 * 2), 16)

    var buffer = new ArrayBuffer(8);
    var intView = new Int32Array(buffer);
    var floatView = new Float64Array(buffer);

    x1 ^= 0x80000000

    intView[1] = x1
    intView[0] = x2

    var res = floatView[0]

    return '<span class="dFloat">' + res + '</span>'
}, 'double (phoenix)')

var dateRenderer = new Renderer("date", function(m) {
    if (m.length != 8 * 2)
        return null;

    var x = parseInt(m, 16)

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
})

//var timestampRenderer = new Renderer("timestamp", function(m) {
//    if (m.length != 8 * 2 + 4 * 2)
//        return null;
//
//    var x = parseInt(m.substr(0, 8 * 2 + 4*2), 16)
//
//    x = Math.floor(x/ 1000000)
//
//    var date = new Date(x);
//
//    var month = date.getMonth() + 1;
//    var day = date.getDate();
//    var hour = date.getHours();
//    var min = date.getMinutes();
//    var sec = date.getSeconds();
//
//    month = (month < 10 ? "0" : "") + month;
//    day = (day < 10 ? "0" : "") + day;
//    hour = (hour < 10 ? "0" : "") + hour;
//    min = (min < 10 ? "0" : "") + min;
//    sec = (sec < 10 ? "0" : "") + sec;
//
//    var str = date.getFullYear() + "-" + month + "-" + day + " " +  hour + ":" + min + ":" + sec;
//
//    return '<span class="dDate">' + str + '</span>'
//})

var allRenderers = [hexRenderer, stringRenderer, boolRenderer, intRenderer, longRenderer, dateRenderer,
    floatRenderer, floatPRenderer, doubleRenderer, doublePRenderer]

var renderersMap = {}

function initRenderers() {
    for (var i = 0; i < allRenderers.length; i++) {
        renderersMap[allRenderers[i].name] = allRenderers[i]
    }
}

initRenderers()

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
RendererAttr.prototype.maxLength = 30