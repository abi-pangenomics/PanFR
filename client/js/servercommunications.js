/*
var graphInitRequest = function () {
    var myrequest = new XMLHttpRequest();
    myrequest.addEventListener("error", function (error) {console.log("Error : Server - Graph Init");});
    myrequest.open('GET', 'http://localhost:9997/server/init-graph');
    myrequest.onload = onloadInitGraph(myrequest.responseText);
    myrequest.send();
};

var pathNamesRequest = function () {
    var pathnamesrequest = new XMLHttpRequest();
    pathnamesrequest.addEventListener("error", function (error) {console.log("Error : Server - Path Names");});
    pathnamesrequest.open('GET', 'http://localhost:9997/server/getpathnames');
    pathnamesrequest.onload = onloadPathNames(pathnamesrequest.responseText);
    pathnamesrequest.send();
};

var pathNodesRequest = function (path_index) {
    var pathnodesrequest = new XMLHttpRequest();
    pathnodesrequest.addEventListener("error", function (error) {console.log("Error : Server Request - Path Nodes");});
    pathnodesrequest.open('GET', 'http://localhost:9997/server/getpathnodes?pathid='+d3.select(this).attr("data-name"));
    pathnodesrequest.onload = onloadPathNodes(pathnodesrequest.responseText, path_index);
    pathnodesrequest.send();
};

var hierarchiesRequest = function () {
    var hierarchiesrequest = new XMLHttpRequest();
    hierarchiesrequest.addEventListener("error", function (error) {console.log("Error : Server Request - Hierarchies");});
    hierarchiesrequest.open('GET', 'http://localhost:9997/server/gethierarchies');
    hierarchiesrequest.onload = onloadHierachies(hierarchiesrequest.responseText);
    hierarchiesrequest.send();
};
*/
