angular.module("MetadataExtractor", ["RRG", "ngJSONTable"])
	.controller("MetadataExtractor.Ctrl", function ($scope, PatternService) {
	
	
	//console.log($scope.fileList);
	
		if ($scope.fileList == undefined) {
			$scope.fileList = [
		"00733-actin.DIB"
, "00733-DNA.DIB"
, "00733-pH3.DIB"
, "00734-actin.DIB"
, "00734-DNA.DIB"
, "00734-pH3.DIB"
, "00735-actin.DIB"
, "00735-DNA.DIB"
, "00735-pH3.DIB"
, "00736-actin.DIB"
, "00736-DNA.DIB"
, "00736-pH3.DIB"
, "03997-actin.DIB"
, "03997-DNA.DIB"
, "03997-pH3.DIB"
, "03998-actin.DIB"
, "03998-DNA.DIB"
, "03998-pH3.DIB"
, "03999-actin.DIB"
, "03999-DNA.DIB"
			, "b034-Seeyou3432.DIB"
		];
		}
		$scope.teardowns = [];
		$scope.selectedEntry = new TypicalEntry();
		//$scope.selectedEntry.setText("WellA01_Seq0002.tif");
		//$scope.selectedEntry.addVariableRegion(4,6);
		//$scope.selectedEntry.variableRegions[0].name = "Well";

		$scope.waitForUpdate = false;
	
		$scope.selectEntry = function (entry) {

			$scope.selectedEntry = PatternService.create(entry);
			$scope.selectedRegion = undefined;

		}
		$scope.selectedFilename = undefined;

		$scope.selectEntryById = function (id) {
			$scope.selectEntry($scope.fileList[id]);
		}

		$scope.onRegionCreated = function (region) {
			$scope.selectedRegion = region;
			//$scope.updatePredictions();
		};

		//$scope.selectedRegion = $scope.selectedEntry.variableRegions[0];
		$scope.updateTearDowns = function () {
			$scope.fileList.forEach(function (filename, index) {
				if(index < $scope.filePagination.start || index > $scope.filePagination.start + $scope.filePagination.limit) return;
				$scope.teardowns[index-$scope.filePagination.start] = teardown($scope.selectedEntry, filename,25);
				
			});
		};
		$scope.removeRegion = function () {
			if ($scope.selectedEntry == undefined) return;
			$scope.selectedEntry.removeRegion($scope.selectedRegion);
			$scope.selectedRegion = undefined;
			$scope.updateTearDowns();
		}
		$scope.getEntryList = function () {
			return PatternService.patterns;
		};

		$scope.lastUpdate = new Date();

		$scope.filePagination = {
			start:0
			,limit:10
		};
		
		$scope.moreFiles = function () {
		    var start = $scope.filePagination.start;
			var fileNumber = $scope.fileList.length;
			var limit = $scope.filePagination.limit;
			
			if(start+limit < fileNumber) {
			 	start +=limit;
			}
			else {
				start = 0;
			}
			
			console.log(start);
			$scope.filePagination.start = start;
			$scope.updateTearDowns();
		}
		
		
			
	
		$scope.updatePredictions = function (oldValue, newValue) {

			
			var refreshDelay = 500;

			if ($scope.selectedEntry == undefined) return;

			$scope.lastUpdate = new Date();

			$scope.waitForUpdate = true;

			setTimeout(function () {

				var elapsed = new Date() - $scope.lastUpdate;

				if (elapsed < refreshDelay) {
					return;
				}
			//console.log("updating !");

				$scope.updateTearDowns();
				$scope.updateMetadata();
				$scope.waitForUpdate = false;
				$scope.checkSelectedEntry();
				$scope.$apply();


			}, refreshDelay);

			$scope.selectedEntry.decomposition = undefined;
			$scope.selectedEntry.decompose();

		};

		$scope.$watch("selectedRegion", $scope.updatePredictions, true);
		$scope.$watch("selectedRegion.name", $scope.updatePredictions, true);
		$scope.$watchCollection("fileList",$scope.updatePredictions,true);
		$scope.updateMetadata = function () {


			if ($scope.metadata == null) $scope.metadata = [];

			if ($scope.selectedEntry == undefined) return;

			$scope.fileList.forEach(function (file, index) {

				var metadata = {
					"File name": file
				};
				var extracted = $scope.selectedEntry.search(file);

				for (var i in extracted) {
					metadata[i] = extracted[i];
				}

				$scope.metadata[index] = metadata;
			});
		};

		
		$scope.executeCallback = function() {
			if($scope.selectedEntryValid) {
				var p = $scope.callback($scope.selectedEntry);
				if(p != undefined && p.then != undefined) {
					$scope.waitForUpdate = true;
					$scope.selectedEntryStatus = "Adding rule...";
					$scope.selectedEntryValid = false;
					p.finally(function() {
						$scope.waitForUpdate = true;
						
						$scope.selectedEntryStatus = "Rule added"
					});
						
				}
			}
		}


		$scope.updateTearDowns();

		$scope.entryExists = function (entry) {

			if (!isNaN(entry)) {
				return PatternService.getPattern($scope.fileList[entry]) != undefined;
			}
			return PatternService.getPattern(entry) != undefined;
		};
		$scope.selectedEntry.decomposition = undefined;


		$scope.regionNames = [];

		$scope.selectedEntryStatus = "Add a rule";

		$scope.selectedEntryValid = false;



		$scope.checkSelectedEntry = function () {
		//console.log("checking entry");

			// function checking if all regions are named
			function checkNames(entry) {
				var status = "";

				// for each reach of the entry
				entry.variableRegions.forEach(function (region) {
					if (region.name == "") status = "Name all your selections";
				});
				return status;
			}

			// list of checks
			var checkFunctions = [checkNames, undefined];

			// for each check
			for (var i = 0; i != checkFunctions.length; i++) {

				// retrieving the checking function
				var f = checkFunctions[i];
				if (f == undefined) continue;

				// getting the result of the check
				var result = f($scope.selectedEntry);

				// if the function returns a non empty string,
				// it means there is a problem
				if (result != "") {

					// updating the status
					$scope.selectedEntryStatus = result;
					$scope.selectedEntryValid = false;
					return;
				}
			}

		//console.log("Everything is fine");

			$scope.selectedEntryStatus = "Add rule";
			$scope.selectedEntryValid = true;

		};

	})
	.factory("PatternService", function () {
		return new PatternList();
	})
	.directive("teardown", function () {
		return {
			restrict: 'E',
			scope: {
				data: "="
			},
			template: "<span ng-repeat='part in data' ng-class='part.color'  >{{part.value}}</span>"
		}
	})
	.directive("regionDisplay", function () {
		return {
			restrict: "E",
			scope: {
				partList: "="
			},
			template: "<span ng-repeat='part in partList' ng-class='part.color' class='part'>{{part.text}}</span>"
		};
	})
	.directive("applyRegexp", function () {
		return {
			restrict: 'E',
			scope: {
				rrgEntry: "=",
				fileName: "="
			},
			link: function (scope, element, attrs) {

			//console.log(scope);
				var update = function () {
					$(element).text("");

					var rrgEntry = scope.rrgEntry;
					var filename = scope.fileName;

					if (rrgEntry == undefined) {

						$(element).text(filename);
						return;
					}
					//console.log("updating", $scope.rrgEntry.text);
					teardown(rrgEntry, filename).forEach(function (part) {

						$("<span/>").text(part.value).addClass(part.color).appendTo($(element));

					});
				};
				scope.$watch(function () {
					return scope.rrgEntry;
				}, update);
				scope.$watch('fileName', update);
				scope.$on("$regionEdited", update);
			}
		};

	})
	.directive("metadataExtractor",function() {
		return {
			
			templateUrl:"partials/metadata-extractor.html"
			,controller:"MetadataExtractor.Ctrl"
			,restrict:"A"
			,scope:{
				fileList:"="
				,callback:"="
			}
		}
	});
;


function PatternList() {
	var self = this;

	self.patterns = [];

	self.create = function (entry) {

		var exist = false;
		var pattern = self.getPattern(entry);


		if (pattern != undefined) return pattern;
		else {
			var pattern = new TypicalEntry();
			pattern.setText(entry);

			self.patterns.push(pattern);
			return pattern;
		}
	}

	self.getPattern = function (entry) {
		var pattern = undefined;
		self.patterns.forEach(function (p) {
			if (p.text == entry)
				pattern = p;
		});
		return pattern;
	};

}

var entry = new TypicalEntry();
entry.setText("WellA01_PosB03.tif");
entry.addVariableRegion(4, 6);
entry.variableRegions[0].name = "Well";
entry.addVariableRegion(11, 13);
entry.variableRegions[1].name = "Position";
console.log(entry.compile());
teardown(entry, "WellA02_PosA06.tif");


// tear down a text into parts that contains 
// extracted metadata with their colors 
// from a typical entry
function teardown(entry, text, limit) {

	//text = new String(text);
	
	if(limit == undefined) limit = 500;
	
	var re = new RegExp(entry.compile());
	//console.log(re);
	var results = re.exec(text);
	//console.log("results",results);
	if (results == null) return [{
		value: text.substr(0,limit-1) + "..."
	}];

	var parts = [];

	var create_blank_region = function () {
		return {
			value: ""
		}
	};
	var enlarge_blank_region = function (region, letter) {
		region.value += letter;
	};
	var is_first_of_matches = function (text, matches) {
		return (text.indexOf(matches[0]) == 0);
	};

	var takoff_first_letter = function (text) {
		return text.substr(1, text.length - 1);
	};

	var takeoff_region = function (text, region) {

		return text.substr(region.length, text.length - 1);
	};

	var current_blank_region = undefined;
	results.shift();
	var cursor = 0;
	var resultId = -1;
	var textOriginalLength = text.length;
	
	
	while (text.length > 0) {
		   
		if(textOriginalLength - text.length >= limit) {
			parts.push({value:"..."});
			break;
		}
	//console.log(text,textOriginalLength - text.length);
		//console.log("before",text);
		if (is_first_of_matches(text, results)) {
			resultId++;
			var region = entry.variableRegions[resultId];
			var part = {
				name: region.name,
				value: results[0],
				color: region.class,
				symbol: region.symbol
			};
			parts.push(part);

			results.shift();


			current_blank_region = undefined;
			//console.log("taking off region",part.value);
			text = takeoff_region(text, part.value);



		} else {
			if (current_blank_region == undefined) {
				current_blank_region = create_blank_region();
				parts.push(current_blank_region);
			}
			enlarge_blank_region(current_blank_region, text[0]);
			text = takoff_first_letter(text);
			cursor++;

		}

		//console.log("after",text);	
	}
//console.log(parts);
	return parts;
	//console.log(parts);
}