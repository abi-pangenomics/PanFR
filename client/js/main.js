// Some Global Data
var graph_edges;    // should hold all the graph edges after graph rendering.
var update = false;

var graph; // global graph object, acts as an interface for the force layout

var width = window.innerWidth - 30,
    height = window.innerHeight - 30;

var initialized = false; // set to true once graph is rendered
var hierarchies_displayed = false; // for togglng the hierarchy display

var nodepaths; // list of node-path key value pairs - inefficient for memory
var pathNodesList = []; // list of all nodes in each path
var fr_pathnames; // list of all path names

// --------- Possible speed efficiency booster - level of indirection
var nodeSelections = []; // maps node fr number (node name) to its selection
var edgeSelections = []; // maps edge to selection
// ---------

// debug flags
var debug = false;   // add to all debug statements
var checkpoints = true; // add to all checkpoints

