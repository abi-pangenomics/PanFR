/**
 * Set up event handlers
 * */
d3.select("#init-graph-button")
    .on("click", function () {
        if (!initialized)
            graphInitRequest();
    });
d3.select('#view-hierarchies')
    .on("click", viewHierarchies);
/*d3.select("#get-node-paths")
    .on("click", getNodePathsRequest);*/  /// - has been automated to request data during graph initialization
d3.select("#restore-default")
    .on("click", restoreDefault);
d3.select("#add-node")
    .on("click", onclickAnnotate);

// create a new svg
var svg = d3.select("#canvas")
    .append("svg")
    .attr("class", "graph-canvas")
    .attr("width", width)
    .attr("height", height)
    .style("outline", "thick solid grey")
    .style("background", "#E8E8E8")
    .attr("overflow", "visible")
    .style("margin", "10px");

// for arrow markers
svg.append("defs").selectAll("marker")
    .data(["suit", "licensing", "resolved"])
    .enter().append("marker")
    .attr("id", function(d) { return d; })
    .attr("viewBox", "2 -5 10 10")
    .attr("refX", 25)
    .attr("refY", 0)
    .attr("markerWidth", 4)
    .attr("markerHeight", 4)
    .attr("orient", "auto")
    .append("path")
    .attr("d", "M0,-5L10,0L0,5 L10,0 L0, -5")
    .style("stroke", "blue")
    .style("opacity", "0.6");

// set up the force layout
var force = d3.layout.force()
    .size([width, height])
    .nodes([{}])            // keep in mind the one node.
    .on('tick', tick)
    .linkDistance(80)
    .gravity(0.05)   // gravity+charge tweaked to ensure good 'grouped' view (e.g. green group not smack between blue&orange, ...
    .charge(-600)    // ... charge is important to turn single-linked groups to the outside
    .friction(0.5);

// Initialize force layout with nodes and links pointing to those of the force layout
graph = new Graph(force.nodes(), force.links());

// Global Selectors
var node = svg.selectAll("circle"),
    link = svg.selectAll('.link'),
    label = svg.selectAll("text");

if (checkpoints)
    console.log("checkpoint : Application Initialized");

// force layout's tick function
function tick(e) {
    // update nodes (uncomment for bounded force layout)
    node.attr('cx', function(d) { return d.x /*= Math.max(8, Math.min(width - 8, d.x))*/; })
        .attr('cy', function(d) { return d.y /*= Math.max(8, Math.min(height - 8, d.y)) */; })
        .call(force.drag);

    // update links
    link.attr('x1', function(d) { return d.source.x; })
        .attr('y1', function(d) { return d.source.y; })
        .attr('x2', function(d) { return d.target.x; })
        .attr('y2', function(d) { return d.target.y; });

    // update label co-ordinates
    d3.selectAll("text").attr("x", function (d) { return d.x; })
        .attr("y", function (d) { return d.y; });
}
