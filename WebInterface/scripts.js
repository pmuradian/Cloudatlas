var xhr;

function install() {
    var query = document.getElementById("1").value;
    if (query != "") {
        document.getElementById("6").value = "not empty";
        xhr = new XMLHttpRequest();
        xhr.open('POST', getIPAddress() + "/client/install", true);
        xhr.send(query);
        xhr.onreadystatechange = processRequest;
    }
    // document.getElementById("6").innerText = "empty";
}

function uninstall() {
    var attributeName = document.getElementById("1").value;
    if (attributeName != "") {
        xhr = new XMLHttpRequest();
        xhr.open('POST', getIPAddress() + "/client/uninstall", true);
        xhr.send(attributeName);
        xhr.onreadystatechange = processRequest;
    }
}

function printZMI() {
    var zmiName = document.getElementById("2").value;
    document.getElementById("6").innerText = getIPAddress() + " calling";
    if (zmiName != "") {
        xhr = new XMLHttpRequest();
        xhr.open('POST', getIPAddress() + "/client/printZMI", true);
        xhr.send(zmiName);
        xhr.onreadystatechange = processRequest;
    }
}

function printAttribute() {
    var attribute = document.getElementById("3").value;
    if (attribute != "") {
        xhr = new XMLHttpRequest();
        xhr.open('POST', getIPAddress() + "/client/printAttribute", true);
        xhr.send(attribute);
        xhr.onreadystatechange = processRequest;
    }
}

function getIPAddress() {
    var ip = document.getElementById("ip").value;
    if (ip != "") {
        return "http://" + ip + ":8000";
    }
    document.getElementById("6").value = "IP address line is empty";
}

function processRequest(e) {p
    if (xhr.readyState == 4) {
        document.getElementById("6").value = xhr.responseText;
    }
}