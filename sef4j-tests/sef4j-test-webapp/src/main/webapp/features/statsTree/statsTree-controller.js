'use strict';

testwebapp.controller('StatsTreeController', 
		function ($scope, $filter, $http, ngTableParams, StatsTreeUtils) {
	var vm = this;
	
	vm.message = "";
	vm.depthTreeTableData = 6;
	vm.statsTreeMetrics = {};

	vm.statsTreeMetricsTableData = 
		[
	    {
	    	treePath: "a.b.Class1:method1 / a.b.Class2:method2 / a.b.Class3:method3",
	    	shortTreePath: "method1 / method2 / method3",
	    	rootClassName: "a.b.Class1",
	    	rootMethodName: "method1",
	    	parentClassName: "a.b.Class2",
	    	parentMethodName: "method2",
	    	className: "a.b.Class3",
	    	methodName: "method3",

			pendingCount: 0,
			pendingSum: 0,
			countTotal: 10,
			sumTotal: 2090,
			countFast01: 9,
			sumFast01: 90
	    }
	    ];


	var recursiveStatsTreeToStatsTableData = function(res, tree, 
			rootClassName, rootMethodName,
			parentPath, parentShortPath, parentClassName, parentMethodName, 
			remainDepth) {
		var name = tree.name || '';
		var indexSep = name.indexOf(':');
		var className = (indexSep != -1)? name.substring(0, indexSep) : '';
		var methodName = (indexSep != -1)? name.substring(indexSep+1, name.length) : '';
		var currPath = (parentPath != '' && name != '')? parentPath + " / " + name : name;
		var currShortPath = (parentShortPath != '' && methodName != '')? parentShortPath + " / " + methodName : methodName;
		
		if (rootClassName == null || rootClassName === '') {
			rootClassName = className;
			rootMethodName = methodName;
		}
		
		var perfStats = (tree.propsMap != null)? tree.propsMap['stats'] : null;
		if (perfStats) {
			var elapsed = perfStats.elapsedTimeStats;
			var elapsedCumulatedCounts = elapsed.cumulatedCountSlots;
			var elapsedCumulatedSums = elapsed.cumulatedSumSlots;
			var resElt = {
		    	treePath: currPath,
		    	shortTreePath: currShortPath,
		    	rootClassName: rootClassName,
		    	rootMethodName: rootMethodName,
		    	parentClassName: parentClassName,
		    	parentMethodName: parentMethodName,
		    	className: className,
		    	methodName: methodName,

		    	pendingCount: perfStats.pendingCounts.pendingCount,
				pendingSum: perfStats.pendingCounts.pendingSum,
				countTotal: elapsedCumulatedCounts[9],
				sumTotal: elapsedCumulatedSums[9],
				countFast01: elapsedCumulatedCounts[1],
				sumFast01: elapsedCumulatedSums[1],
				countMedium: elapsedCumulatedCounts[5],
				sumMedium: elapsedCumulatedSums[5],
				
			};
			res.push(resElt);
		}
		
		if (remainDepth == -1 || remainDepth > 0) {
			// recurse child
			var remainDepth = (remainDepth == -1)? -1 : remainDepth - 1;
			jQuery.each(tree.childMap, function(childKey, childValue) {
				// *** recurse ***
				recursiveStatsTreeToStatsTableData(res, childValue, 
						rootClassName, rootMethodName,
						currPath, currShortPath, 
						className, methodName, 
						remainDepth);
			});
		}
		
	}

	var statsTreeToStatsTableData = function(src) {
		var res = [];
		recursiveStatsTreeToStatsTableData(res, src, 
				'', '', 
				'', '',
				'', '',
				vm.depthTreeTableData);
		return res;
	}

	
	vm.loadStats = function() {
		vm.message = "Loading...";
		$http.get('app/rest/metricsStatsTree/stats')
        .success(function(response, status) {
        	vm.statsTreeMetrics = response;
        	var resTableData = statsTreeToStatsTableData(response);
        	
        	vm.statsTreeMetricsTableData = resTableData;
        	vm.statsTreeTableParams.reload();
        	vm.message = "";
        });
	};
	
    
	vm.statsTreeTableParams = new ngTableParams({
        page: 1,            // show first page
        count: 25,          // count per page
        filter: {
            // name: ''       // initial filter
        },
        sorting: {
            //name: 'asc'   // initial sorting
        }
    }, {
        total: vm.statsTreeMetricsTableData.length, // length of data
        getData: function ($defer, params) {
        	var inputData = vm.statsTreeMetricsTableData;
            var filteredData = params.filter() ?
                    $filter('filter')(inputData, params.filter()) :
                    data;
            var orderedData = params.sorting() ?
                    $filter('orderBy')(filteredData, params.orderBy()) :
                    data;
            params.total(orderedData.length); // set total for recalc pagination
            $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        }
    });

	// init
	vm.loadStats();	
});

