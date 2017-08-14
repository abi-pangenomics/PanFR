
/**
 * Just rebinds the data and restarts layout.
 * Should be used when something is added to he graph
 * */
var restart = function () {
    if (checkpoints)
        console.log("checkpoint : restart");

    link = link.data(graph.links);
    link.exit().remove();
    link.enter()
        .insert("line", ".node")
        .attr("class", "link")
        .style("stroke-width", "2px")
        .style("marker-end",  "url(#suit)");

    node = node.data(graph.nodes);
    node.enter()
        .insert("circle", ".cursor")
        .attr("class", "node")
        .attr("r", 5)
        .call(force.drag);

    force.start();
};

/**
 * Should be called only once - during the start for initial graph rendering
 *
 * Prepares graph for rendering
 * binds data (links, nodes, labels) to DOM elements
 * starts the force layout for rendering
 * */
var startForce = function () {
    if (checkpoints)
        console.log("checkpoint : startForce");

    graph.refreshLinks();
    graph.addNodesFromLinks();
    graph.cleanNodes(undefined);
    graph.refreshThreshScale();

    if (debug) {
        console.log("nodes");
        console.log(graph.nodes);
        console.log("links");
        console.log(graph.links);
    }

    // create virtual selection placeholders for links
    link = link.data(graph.links);

    link.enter().append('line')
        .attr('class', 'link')
        .style("stroke-width", "2px")
        .style("marker-end",  "url(#suit)"); // Modified line - arrowheads

    link.each(function (d) {
        // add the selections to the global array
        edgeSelections[d.source.name + "-" + d.target.name] = this;

        // add classes for path highlight
        var temp_paths = d.paths.split(" ");
        for (var i = 0; i < temp_paths.length; i++){
            d3.select(this).classed("path-" + temp_paths[i], true);
        }
    });

    // create virtual selection placeholders for nodes
    node = node.data(graph.nodes);

    node.enter()
        .append("circle")
        .attr('class', "node")
        .attr('r', function (d) {
            var numnodes = force.nodes().length;
            var size = Math.ceil((1 / numnodes) * 20);
            if (size < 8)
                size = 8;
            return size;
        })
        .on("dblclick", function(d, i){expand(this, d);})
        .on("click", function (d) {
            console.log(this);
            console.log(d);
            logNodeData(this, d);
        })
        .each(function (d) {
            nodeSelections[d.name] = this;
        })
        .style("outline", "white")
        .style("fill", "orange");

    if (debug)
        console.log(force);
    force.alpha(1).start();


    // lets add some labels
    label = label.data(force.nodes());

    label.enter()
        .append("text")
        .text(function (d) { return d.name; })
        .attr("class", "label")
        .style("text-anchor", "end")
        .style("background", "white")
        .style("fill", "#000")
        .style("font-weight", "thick")
        .style("font-family", "Arial")
        .style("font-size", 12);

    svg.selectAll("text")
        .text(function (d) { return d.name; });

    // any other calls to this function will see it as calls to update the existing graph
    update = true;
    initialized = true;

};
