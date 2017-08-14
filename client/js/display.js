/**
 * logs a path out to the right console display
 *
 * @param p_index - index of the path in the global fr_pathnames and pathNodesList array
 * */
var logPath = function (p_index) {
    if (checkpoints)
        console.log("checkpoint : logPath");

    var path_display_text = "[ " + fr_pathnames[p_index].name + " ] : ";
    for (var i = 0; i < pathNodesList[p_index].length; i++){
        path_display_text += pathNodesList[p_index][i] + " --> ";
    }
    // get rid of the  last arrow
    path_display_text = path_display_text.substr(0, path_display_text.length - 5);
    d3.select("#console-display")
        .append("p")
        .text(path_display_text);

    d3.select("#console-display")
        .append("hr");
};

/**
 * Logs node data out to the console
 *
 * Node data includes : Node number, supporting paths, positions in those paths
 * */
var logNodeData = function (clickednode, d) {
    var node_display_text = "";
    node_display_text += "node : " + d.name + " | Supporting Subpaths : ";

    var indices = [];

    for (var i = 0; i < nodepaths.length; i++){
        if (nodepaths[i].node == d.name) {
            node_display_text += nodepaths[i].path + ", ";
            indices.push(i)
        }
    }

    // get rid of leading space and comma
    node_display_text = node_display_text.substr(0, node_display_text.length - 2);

    var console = d3.select("#console-display");
    // flush out to console
    console.append("p")
        .text(node_display_text);

    node_display_text = "";
    for (var i = 0; i < indices.length; i++) {
        node_display_text += "path : " + nodepaths[indices[i]].path + " | positions = ";
        var paths = nodepaths[indices[i]].positions.split(" ");
        for (var j = 0; j < paths.length; j++) {
            node_display_text += paths[j] + ", ";
        }
        node_display_text = node_display_text.substr(0, node_display_text.length-2);
        console.append("p")
            .text(node_display_text);
        node_display_text = "";
    }
    console.append("hr")
        .style("stroke", "thick black");
};
