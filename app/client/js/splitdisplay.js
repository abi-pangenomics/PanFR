Split(['#display-wrapper'], {
    gutterSize: 8,
    cursor: 'col-resize'
});


Split(['#canvas', '#console'], {
    direction: 'vertical',
    sizes: [75, 25],
    gutterSize: 8,
    cursor: 'row-resize'
});

Split(['#a', '#b'], {
    sizes : [12, 88],
    gutterSize: 8,
    cursor: 'col-resize'
});

Split(['#left-console'], {
    direction: 'vertical',
    sizes: [100],
    gutterSize: 8,
    cursor: 'row-resize'
});

Split(['#right-console'], {
    direction: 'vertical',
    sizes: [100],
    gutterSize: 8,
    cursor: 'row-resize'
});
