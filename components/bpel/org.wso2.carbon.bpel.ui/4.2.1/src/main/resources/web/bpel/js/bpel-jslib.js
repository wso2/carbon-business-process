/**
 * Graph widget which shows value of some parameter against time.
 *
 * @param id DOM Element ID where we are going to embed the graph
 * @param timeIntervals Number time intervals we are going to display in graph
 * @param timeInterval milliseconds for a time interval
 */
function TimeVsValueGraph(id,timeIntervals, timeInterval){
    this.domElementId = id;
    this.numberOfTimeIntervals = timeIntervals;
    this.timeInterval = timeInterval;
    this.dataSets = [];
    this.graphOptions = {
        series: {shadowSize: 0},
        yaxis: {min: 0},
        xaxis: {}
    }
}
