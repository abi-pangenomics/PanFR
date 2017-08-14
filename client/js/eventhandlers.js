/**
 * Change later on to add specific nodes for annotation.
 * */
var onclickAnnotate = function () {
    if (checkpoints)
        console.log("checkpoint : onClickAnnotate");
  graph.addNode(100,200);
};

var viewHierarchies = function () {
    if (checkpoints)
        console.log("Checkpoint : viewHierarchies");
    d3.select("#a")
        .style("width", function () { return hierarchies_displayed ? "12%" : "25%"});
    d3.select("#b")
        .style("width", function () {return hierarchies_displayed ? "87%" : "74%"});
    hierarchies_displayed = !hierarchies_displayed;
};

var clickRequest = function (clickednode, d) {
    if (checkpoints)
        console.log("checkpoint : clickRequest");

    var myrequest = new XMLHttpRequest();
    myrequest.open('GET', 'http://localhost:9997/server/clicked?node='+d.name);
    myrequest.onload = function () {
        if (checkpoints)
            console.log("checkpoint : onloadExpand");

        if (!debug)
            console.log(myrequest.responseText);

        graph.updateGraph(myrequest.responseText, clickednode, d);
        d3.selectAll(".node")
            .each(function (d) {
                console.log(d);

                var temp = d3.select(this);
                for (var i = 0; i < this.classList.length; i++){
                    if (this.classList[i] == "node")
                        continue;
                   temp.classed(this.classList[i], false);
                }

                for(var i = 0; i < nodepaths.length; i++){
                    if (+nodepaths[i].node == d.name)
                        temp.classed("path-"+ nodepaths[i].path, true);
                }
            });
        console.log("done");
    };
    myrequest.send();
};

var expand = function (clickednode, d) {
    if (checkpoints)
        console.log("checkpoint : expand");
    console.log("[debug : node #]" + d.name + " clicked...");
    clickRequest(clickednode, d);
};

/**
 * @event - #restore-default clicked
 *
 * @description
 * restores all the nodes and edges to default color and opacity
 * */
var restoreDefault = function () {
    if (checkpoints)
        console.log("checkpoint : restoreDefault");
    if (!initialized)
        return;
    d3.selectAll("circle")
        .transition()
        .duration(500)
        .style("fill", "orange")
        .style("opacity", 1);
    d3.selectAll(".link")
        .transition()
        .duration(300)
        .style("stroke", "grey")
        .style("opacity", 1)
        .style("stroke-width", "2px");
};

/**
 * populates global pathNodesList array with path index - nodes mapping
 * adds path-x classes to nodes for highlighting
 * */
var onloadNodePaths = function (responsetext) {
    nodepaths = d3.csv.parse(responsetext, function (d) {
        return {
            node : d.node,
            path : d.path,
            positions : d.positions
        };
    });

    d3.selectAll(".node")
        .each(function (d, i) {
            for (var i = 0; i < nodepaths.length; i++){
                var path = +nodepaths[i].path;
                var positions = nodepaths[i].positions.split(" ");
                if (pathNodesList[path] == undefined)
                    pathNodesList[path] = [];
                positions.forEach(function (position) {
                    pathNodesList[path][+position] = +nodepaths[i].node;
                });
                if (+nodepaths[i].node == d.name) {
                    d3.select(this).classed("path-"+ path, true);
                }
            }
        });
};

var getNodePathsRequest = function () {
    if (checkpoints)
        console.log("checkpoint : getNodePathsRequest");

    var nodepathsrequest = new XMLHttpRequest();
    nodepathsrequest.addEventListener("error", function (error) {console.log("Error : Server Request");});
    nodepathsrequest.open('GET', 'http://localhost:9997/server/getnodepaths');
    nodepathsrequest.onload = function () {onloadNodePaths(nodepathsrequest.responseText);};
    nodepathsrequest.send();
};

var onloadHierachies = function (responsetext) {
    if (checkpoints)
        console.log("checkpoint : onLoadHierarchies")
    if (debug)
        console.log(responsetext);

    var hierarchies = responsetext.split("|");
    var leftconsoledisplay = d3.select("#left-console-display");
    leftconsoledisplay.append("p");
    hierarchies.forEach(function (line) {
        leftconsoledisplay
            .append("p")
            .text(line)
            .style("margin-left", "60%");
    });
};

var hierarchiesRequest = function () {
    if (checkpoints)
        console.log("checkpoint : hierarchiesRequest");
    var hierarchiesrequest = new XMLHttpRequest();
    hierarchiesrequest.addEventListener("error", function (error) {console.log("Error : Server Request - Hierarchies");});
    hierarchiesrequest.open('GET', 'http://localhost:9997/server/gethierarchies');
    hierarchiesrequest.onload = function () {onloadHierachies(hierarchiesrequest.responseText);};
    hierarchiesrequest.send();
};

/**
 * @deprecated
 * */
var onloadPathNodes = function (responsetext, p_index) {
    if (checkpoints)
        console.log("checkpoint : onloadPathNodes");
    var temp_path = responsetext.split(" ");

    if (pathNodesList[p_index] == undefined)
        pathNodesList[p_index] = [];
    // add the path nodes to the global array
    for (var i = 0; i < temp_path.length; i++) {
        pathNodesList[p_index][i] = +temp_path[i];
    }
    logPath(p_index);
};

/**
 * @deprecated
 * */
var pathNodesRequest = function (path_index, clicked) {
    if (checkpoints)
        console.log("checkpoint : pathNodesRequest");
    var pathnodesrequest = new XMLHttpRequest();
    pathnodesrequest.addEventListener("error", function (error) {console.log("Error : Server Request - Path Nodes");});
    pathnodesrequest.open('GET', 'http://localhost:9997/server/getpathnodes?pathid='+d3.select(clicked).attr("data-name"));
    pathnodesrequest.onload = function() { onloadPathNodes(pathnodesrequest.responseText, path_index); };
    pathnodesrequest.send();
};

/**
 * All nodes/edges selection necessary
 * as required nodes/edges are highlighted while others are dimmed
 * */
var onclickPathName = function (clickedName) {
    console.log(clickedName);

    if (checkpoints)
        console.log("checkpoint : onclickPathName");
    var p_index = +clickedName.dataset.name;

    d3.selectAll(".node")
        .transition()
        .duration(400)
        .style("fill", function (d) {
            // if first node in the path
            if (pathNodesList[p_index]!= undefined && d.name == pathNodesList[p_index][0])
                return "brown";

            if (d3.select(this).classed("path-" + p_index))
                return "teal";
            else
                return "orange";
        })
        .style("opacity", function () {
            if (d3.select(this).classed("path-" + p_index))
                return 1;
            else
                return 0.3;
        });

    d3.selectAll(".link")
        .transition()
        .duration(300)
        .style("stroke", function (d) {
            return d3.select(this).classed("path-" + p_index) ? "green" : "grey";
        })
        .style("opacity", function (d) {
            return d3.select(this).classed("path-" + p_index) ? 1 : 0.3;
        })
        .style("stroke-width", function (d) {
            return d3.select(this).classed("path-" + p_index) ? "4px" : "2px";
        });

   /* if (pathNodesList[p_index] == undefined)
        pathNodesRequest(p_index, clickedName);
    else */

    logPath(p_index);
};

var onloadPathNames = function (responsetext) {
    if (checkpoints)
        console.log("checkpoint : onloadPathNames");
    if (debug)
        console.log(responsetext);

    var names = d3.csv.parse(responsetext, function (d) {
        return {
            id : +d.id,
            name : d.name
        };
    });
    fr_pathnames = names;
    if (debug)
        console.log(names);

    for (var i = 0; i < names.length; i++) {
        d3.select("#path-names")
            .append("li")
            .attr("data-name", +names[i].id)
            .text(names[i].name)
            .on("click", function (){ onclickPathName(this);});
    }
};

var pathNamesRequest = function () {
    if (checkpoints)
        console.log("checkpoint : pathNamesRequest");
    var pathnamesrequest = new XMLHttpRequest();
    pathnamesrequest.addEventListener("error", function (error) {console.log("Error : Server - Path Names");});
    pathnamesrequest.open('GET', 'http://localhost:9997/server/getpathnames');
    pathnamesrequest.onload = function() { onloadPathNames(pathnamesrequest.responseText); };
    pathnamesrequest.send();
};

var onloadInitGraph = function (responsetext) {
    if (debug)
        console.log(responsetext);
    if (checkpoints)
        console.log("checkpoint : onloadInitGraph");
    parseGraph(responsetext);
    startForce();
    pathNamesRequest();
    hierarchiesRequest();
    getNodePathsRequest();
};

var graphInitRequest = function () {
    if (checkpoints)
        console.log("checkpoint : graphInitRequest");
    var myrequest = new XMLHttpRequest();
    myrequest.addEventListener("error", function (error) {console.log("Error : Server - Graph Init");});
    myrequest.open('GET', 'http://localhost:9997/server/init-graph');
    myrequest.onload = function(){
        if (debug)
            console.log(myrequest.responseText);
        onloadInitGraph(myrequest.responseText);
    };
    myrequest.send();
};
