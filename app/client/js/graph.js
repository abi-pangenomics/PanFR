/**
 * Graph Class - Holds the graph data
 * */
function Graph(nodes, links) {
    this.nodes = nodes;
    this.links = links;
    this.links_thresh_scale;
    this.min;
    this.max;
    this.remove_clicked_node = false;

    this.refreshLinks = function () {
        if (checkpoints)
            console.log("checkpoint : graph.refreshLinks");

        this.links.splice(0, this.links.length);
        for (var i = 0; i < graph_edges.length; i++) {
            this.links.push(graph_edges[i]);
        }
    };

    this.addNodesFromLinks = function () {
        if (checkpoints)
            console.log("checkpoint : graph.addNodesFromLinks");

        var min = Number.MAX_VALUE, max = Number.MIN_VALUE;
        if(!initialized)
            nodes.splice(0,1);

        var temp = this.nodes;
        this.links.forEach(function(link) {
            min = Math.min(link.value, min);
            max = Math.max(link.value, max);

            link.source = temp[link.source] ||
                (temp[link.source] = {name: link.source});
            link.target = temp[link.target] ||
                (temp[link.target] = {name: link.target});
        });
        this.min = min;
        this.max = max;
    };

     this.cleanNodes = function (deleteValue) {
         if (checkpoints)
             console.log("checkpoint : graph.cleanNodes");
        this.nodes.clean(deleteValue)
    };

     this.spliceToThreshold = function (thresh) {
         if (checkpoints)
             console.log("checkpoint : graph.spliceToThreshold");

         this.links.splice(0, this.links.length);

         // read all the edges from the global array, but according to the threshold
         for (var i = 0; i < graph_edges.length; i++) {
             if (this.links_thresh_scale(graph_edges[i].value) >= thresh)
                 this.links.push(graph_edges[i]);
         }
         restart();
     };

     this.refreshThreshScale = function () {
         if (checkpoints)
             console.log("checkpoint : graph.refreshThreshScale");

         links_thresh_scale = d3.scale.linear()
             .domain([this.min, this.max])
             .range([0,9])
             .clamp(true);
     };

     this.addNode = function (id, name) {
         if (checkpoints)
             console.log("checkpoint : graph.addNode");

         this.nodes.push({id : id, name : name});

         node = node.data(this.nodes);

         node.enter().insert("circle", ".cursor")
             .attr("class", "node")
             .attr("r", 5)
             .call(force.drag);

         force.start();
     };

     /**
      * sets remove_clicked_node to false/true depending on whether there exists an edge with the clicked node in the updates
      * only adds the edges which have not been already added to the graph. - handle this server side later on. may save the global array space
      *
      * */
     this.parseUpdates = function (edge_text, clicked_data, connectedEdges) {
         this.remove_clicked_node = true;
         var temp = this;

         return d3.csv.parse(edge_text, function (d) {
            // console.log(d);
             //console.log(clicked_data);

             if (temp.remove_clicked_node && (+d.target == clicked_data.name || +d.source == clicked_data.name)){
                 console.log("hello");
                 temp.remove_clicked_node = false;
             }

             if (connectedEdges[d.source + "-" + d.target] == undefined) {
                 console.log(d);
                 return {
                     source: +d.source,
                     target: +d.target,
                     paths: d.paths
                 };
             } else {
                 connectedEdges[d.source + "-" + d.target] = undefined;
                 return undefined;
             }
         });
     };

     this.getConnectedEdges = function (node) {
         var ret = [];
       d3.selectAll(".link")
           .each(function (d) {
               if ((+d.source.name == +node.name || +d.target.name == +node.name) && this != undefined){
                   console.log(d);
                   console.log(this);
                   ret[d.source.name + "-" + d.target.name] = this;
               }
           });
       return ret;
       console.log(ret);
     };

     this.updateGraph = function (edge_text, clickednode, d) {
         // get edges connecetd to the node
         var connectedEdges = this.getConnectedEdges(d);
         console.log(connectedEdges);

         // parse the text
         var updates = this.parseUpdates(edge_text, d, connectedEdges);
         console.log(connectedEdges);
         console.log(updates)

         updates = updates.filter(function (d) {
            return d != undefined;
         });
        /* var newconnectedEdges = connectedEdges.filter(function (d) {
             console.log(d);
             console.log(this);
             return d != undefined;
         });
*/
         console.log(updates);
         console.log(connectedEdges);

         console.log(this.nodes);

         // remove the clicked node from force if necessary
         if (this.remove_clicked_node) {
             for (var i = 0; i < this.nodes.length; i++) {
                 if (+this.nodes[i].name == d.name) {
                     this.nodes.splice(i, 1);
                     break;
                 }
             }
         }

         console.log(connectedEdges);
         console.log(this.links);

         // remove the edges corresponding to the clicked node
         for (var i = 0; i < this.links.length; i++){
            var edge = this.links[i].source.name + "-" + this.links[i].target.name;
            if(connectedEdges[edge] != undefined){
                console.log("removing "+edge);
                this.links.splice(i,1);
                i--;
            }
         }
         console.log(this.links);

         var min = this.min;
         var max = this.max;

         var tempnodes = this.nodes;
         var templinks = this.links;

         // Add the new nodes to the nodes array
         updates.forEach(function(link) {

             min = Math.min(link.value, min);
             max = Math.max(link.value, max);

             var sourcenode = contains(tempnodes, link.source);
             var targetnode = contains(tempnodes, link.target);

             templinks.push({
                 source : sourcenode,
                 target : targetnode,
                 paths : link.paths
             });
         });

         //console.log(this.nodes);

         // update min and max
         this.min = min;
         this.max = max;
         this.refreshThreshScale();

         this.nodes.clean(undefined);
         this.links.clean(undefined);

         node = node.data(this.nodes);

         node.exit().remove();

         node.enter().insert("circle", ".cursor")
             .attr("class", "node")
             .attr("r", 5)
             .style("outline", "white")
             .style("fill", "orange")
             .on("dblclick", function(d, i){expand(this, d);})
             .on("click", function (d) {
                 console.log(this);
                 console.log(d);
                 logNodeData(this, d);
             })
             .call(force.drag);

         link = link.data(this.links);

         link.enter().append('line')
             .attr('class', 'link')
             .each(function(d){
                 var thislink = d3.select(this);
                 var paths = d.paths.split(" ");
                 for (var i = 0 ; i < paths.length; i++) {
                     thislink.classed("path-" + paths[i], true);
                 }
                 console.log(this);
             })
             .style("stroke-width", "2px")
             .style("marker-end",  "url(#suit)");
             //.merge(link); // Modified line - arrowheads

         link.exit().remove();

         console.log(force.nodes());
         console.log(force.links());

         force.start();

         d3.selectAll(".link")
             .each(function(d) {
                 var thislink = d3.select(this);
                 for (var i = 0; i < fr_pathnames.length; i++) {
                     thislink.classed("path-" + i, false);
                 }
                 var paths = d.paths.split(" ");
                 for (var i = 0 ; i < paths.length; i++) {
                     thislink.classed("path-" + paths[i], true);
                 }
             });

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

        label.exit().remove();

         svg.selectAll("text")
             .text(function (d) { return d.name; });

     };

}

// Helper Function
Array.prototype.clean = function(deleteValue) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == deleteValue) {
            this.splice(i, 1);
            i--;
        }
    }
    return this;
};

var contains = function (arr, target) {
 //   console.log(arr, target);
    var index = -1;
    var flag = false;
    for (var i = 0; i < arr.length; i++) {
        if (arr[i].name == target) {
            index = i;
            flag = true;
            break;
        }
    }
    if (flag){
        return arr[index];
    } else {
        var paths = "";
        for (var i = 0; i < nodepaths.length; i++){
            if (nodepaths[i].name == target)
                paths += nodepaths[i].paths + " ";
        }
        if (paths.length > 0)
            paths = paths.substr(0, paths.length - 1); // get rid of the last whitespace

        arr.push({name : target, paths : paths});
        return arr[arr.length - 1];
    }
};

/**
 * @description
 * parses the graph csv data recieved from server.
 * stores the parsed data of graph edges in the global graph_edges array,
 * this array acts as an interface between force.edges and the server response.
 *
 * @param graph_csv_text - graph edges in csv format
 * */
var parseGraph = function (graph_csv_text) {
    if (checkpoints)
        console.log("checkpoint : parseGraph");
    graph_edges = d3.csv.parse(graph_csv_text, function (d) {
            return {
                source : +d.source,
                target : +d.target,
                value : +d.value,
                paths : d.paths
            };
        }
    );
    if (debug)
        console.log(graph_edges);
};
