function safeParseJson(json) {
    if (!json)
        return null

    return JSON.parse(json)
}

var wasHttpError

var httpErrorHandler = function(response) {
    if (wasHttpError)
        return

    wasHttpError = true

    $('body').html(response.data)
}

/**
 * @param x {number}
 * @return {string}
 */
//function toHex(x) {
//    if (x >= 16)
//        throw "Invalid number code: " + x;
//
//    return x < 10 ? ('0' + x) : ('a' + x - 10)
//}
//

/**
 * @param s {string}
 * @return {string}
 */
function rubyStrToHex(s) {
    var res = []

    for (var i = 0; i < s.length; ) {
        var a = s.charAt(i)

        var len = null;

        if (a == '\\') {
            var next = s.charAt(i + 1)

            if (next == 'x') {
                if (i + 4 > s.length || !s.substr(i + 2, 2).match(/[0-9a-zA-Z]{2}/)) {
                    res.push("2F78") // '\x'

                    len = 2;
                }
                else {
                    res.push(s.substr(i + 2, 2).toUpperCase())
                    len = 4;
                }
            } else if (next == '\\') {
                res.push("2F")

                len = 2
            } else {
                res.push("2F")

                len = 1
            }
        }
        else {
            res.push((a.charCodeAt(0) & 0xFF).toString(16))

            len = 1
        }

        if (!len)
            throw "Internal error"

        i += len
    }

    return res.join('')
}

/**
 * @param res {string[]}
 * @param m {string}
 */
function hexToRubyStrBuff(res, m) {
    for (var i = 0; i < m.length; i += 2) {
        var charCode = parseInt(m.substr(i, 2), 16);

        var x = String.fromCharCode(charCode)

        if (x == '<')
            res.push('&lt;')
        else if (x == '>')
            res.push('&gt;')
        else if (x == '&')
            res.push('&amp;')
        else if (charCode <= 0x1f) {
            res.push('\\x')
            res.push(charCode.toString(16))
        }
        else {
            res.push(x)
        }
    }
}


/**
 * @param s {string}
 * @return {string}
 */
function hexToRubyStr(s) {
    var res = []

    hexToRubyStrBuff(res, s)

    return res.join('')
}