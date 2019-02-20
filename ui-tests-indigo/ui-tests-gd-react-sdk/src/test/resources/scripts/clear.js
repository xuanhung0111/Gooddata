'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});
exports.clearTermial = clearTermial;
exports.clearLine = clearLine;
function clearTermial(clr = true) {
    if (clr !== false) {
        process.stdout.write('\x1b[2J\x1b[H');
    }
    process.stdout.write('\x1b[2K\x1b[G');
}

const readline = require('readline');
function clearLine() {
    readline.clearLine(process.stdout, 0)
    readline.cursorTo(process.stdout, 0)
}
