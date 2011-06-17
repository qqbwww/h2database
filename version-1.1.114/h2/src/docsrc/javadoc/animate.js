/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

function on(id) {
    return switchTag(id, 'titleOff', 'detailOn');
}

function off(id) {
    return switchTag(id, '', 'detail');
}

function allDetails() {
    for (i = 0;; i++) {
        x = document.getElementById('m' + i);
        if (x == null) {
            break;
        }
        switchTag('m' + i, 'titleOff', 'detailOn');
    }
    return false;
}

function switchTag(id, title, detail) {
    document.getElementById('t' + id).className = title;
    document.getElementById(id).className = detail;
    return false;
}

function openLink() {
    page = new String(self.document.location);
    var pos = page.lastIndexOf("#") + 1;
    if (pos == 0) {
        return;
    }
    var ref = page.substr(pos);
    link = decodeURIComponent(ref);
    el = document.getElementsByName(link)[0].parentNode.parentNode;
    on(el.id);
    window.scrollTo(0, el.offsetTop);
    return true;
}