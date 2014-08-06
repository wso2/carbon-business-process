var memInfoInterlObject;
var serverDataNotAvailable;
/**
 * Queue implementation which has limited space and remove excess elements in the front
 * when pushing new members to a fully filled queue.
 * @param sizeLimit size limit of the queue
 */
function BoundedQueue(sizeLimit) {
    // Size limit of the queue
    this.limit = sizeLimit;

    // Array which holds the data
    this.queue = [];

    for (var i = 0; i < 20; i++) {
        this.queue.push(0.0);
    }
}

/**
 * Add value to the bounded queue. If size of the underlying array is equal to the size limit
 * we'll remove the first element and push the new data to end of the array.
 * @param data  data element to put to the queueu
 */
BoundedQueue.prototype.push = function(data) {
    this.queue.shift();
    this.queue.push(data);

    return data;
};

/**
 * Return content of the queue. If length of the underlying array is less than the size limit
 * this will return a array filled with 0.0 to the empty cells.
 */
BoundedQueue.prototype.getContent = function() {
    var size = this.queue.length;
    var content = new Array(size);

    for (var i = 0; i < size; i++) {
        content.push(this.queue[i]);
    }

    if (size < this.limit) {
        for (var j = size; j < this.limit; j++) {
            content.push(0.0);
        }
    }

    return content;
};

var usedMemory = new BoundedQueue(20);
var totalMemory = new BoundedQueue(20);


BPEL.summary.drawServerMemoryConsumptionGraph = function() {
    $.ajax({
        url:"memory_info-ajaxprocessor.jsp",
        dataType: "json",
        success:function(json) {
            if(json.UsedMemoryValue == undefined || json.TotalMemoryValue == undefined){
                $("#server-mem-graph").html(serverDataNotAvailable);
            } else {
                usedMemory.push(json.UsedMemoryValue);
                totalMemory.push(json.TotalMemoryValue);

                function yAxisTickFormatter(val, yaxis){
                    return val + json.UsedMemoryUnit;
                }

                function xAxisTickFormatter(val, xaxis){
                    return val + " s";
                }
                var t = [];
                var start = 10;
                for(var k =0; k < 10; (k = k+0.5)){
                    t.push([k, (start -k)]);
                }


                var flotOptions = {
                    series: {shadowSize: 0, points: { show: true }, lines: {show:true}} ,
                    yaxis: {min: 0, tickFormatter:yAxisTickFormatter},
                    xaxis:{ticks: t, tickDecimals:1},
                    legend: {container: jQuery("#mem-legend"), noColumns:2}
                };

                var totalMemoryData = [];
                var usedMemoryData = [];
                var timeLimit = 0;
                for (var i in totalMemory.getContent()) {
                    totalMemoryData.push([timeLimit, totalMemory.getContent()[i]]);
                    timeLimit += 0.5;
                }
                timeLimit = 0;
                for (var j in usedMemory.getContent()) {
                    usedMemoryData.push([timeLimit, usedMemory.getContent()[j]]);
                    timeLimit += 0.5;
                }

                var dataSets = [
                    {label: "Total Memory", data: totalMemoryData},
                    {label: "Used Memory", data: usedMemoryData}
                ];

                $.plot($("#mem-graph"), dataSets, flotOptions);
            }
        },
        error: function(){
             $("#server-mem-graph").html(serverDataNotAvailable);
            if(memInfoInterlObject){
                clearInterval(memInfoInterlObject);
            }
        }
    });

};

