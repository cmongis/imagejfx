angular.module("ngJSONTable", [])
	.directive("tableLine", function () {
		return {
			restrict: "A"
				//,replace:true
				,
			template: "<tr><td ng-repeat='str in values'>{{str}}</td></tr>",
			scope: {
				values: "="
			}
		};
	})
	.directive("tableHeader", function () {
		return {
			restrict: "A"
				//,replace:true
				,
			template: "<tr><th ng-repeat='str in values'>{{str}}</th></tr>",
			scope: {
				values: "="
			}
		};
	})
	.directive("jsonTable", function () {
		return {
			restrict: "E",
			templateUrl: ["../partials/ng-json-table.html"],
			controller: "TableCtrl",
			scope: {
				data: "="
			}

		}
	})
	.controller("TableCtrl", function ($scope) {


		

		console.log("$scope.data",$scope.data);
		
		$scope.updateData = function (newValue) {
			console.log("updating table data");
			$scope.headers = get_headers_list(newValue);
			$scope.displayedLines = JSONToArray(newValue);

		};
		
		$scope.$watch('data', $scope.updateData,true);
		
	});






function JSONToArray(objectArray) {

	// taking the object with most attributes (hoping it has them all)
	var attrs = get_headers_list(objectArray);


	var result = [];
	if(objectArray == undefined) return result;
	objectArray.forEach(function (o) {

		var line = [];

		attrs.forEach(function (attr) {
			var value = o[attr];
			if (value == undefined) value = "";
			line.push(value);
		});
		result.push(line);
	});
	return result;
}

function get_object_with_most_attributes(objectArray) {

	
	if(objectArray == undefined) return {};
	var max = get_attributes_number(objectArray[0]);
	var object = objectArray[0];
	objectArray.forEach(function (o) {
		var length = 0;
		for (var i in o) {
			length++;
		}

		if (length > max) {
			max = length;
			object = o;
		}
	});
	return object;
}

function get_attributes_number(object) {
	var length = 0;
	for (var i in object) {
		length++;
	}
	return length;
}

function get_attributes_list(object) {

	console.log(object);
	var arr = [];
	for (var attr in object) {
		console.log(attr);

		arr.push(attr);
	}
	return arr;
}

function get_headers_list(objectArray) {
	// taking the object with most attributes (hoping it has them all)
	var attrs = get_object_with_most_attributes(objectArray);
	console.log(attrs);
	attrs = get_attributes_list(attrs);
	return attrs;
}