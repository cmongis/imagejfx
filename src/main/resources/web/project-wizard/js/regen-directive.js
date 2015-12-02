angular.module("RRG")
	.directive("regionSelection", function () {
		return {
			restrict: "E",
			scope: {
				"rrgEntry": "=",
				"callback": "="
			},
			templateUrl: "partials/region-selection.html"
		}
	})
	.directive("letter", function () {
		return {
			restrict: "E",
			scope: {
				rrgModel: "=",
				rrgEntry: "=",
				callback: "="
			},
			link: function ($scope, element) {

				//if($scope.rrgModel == undefined) return;

				var letter = element;
				var rrgmodel = $scope.rrgModel;
				//console.log($scope);

				var index = rrgmodel.index;
				var entry = $scope.rrgEntry;

				if (index == NaN || index == undefined) return;
				var elongate = function (event, element) {
					try {
						
						var condition = (event.type == "mousedown" || entry.isSelectionStarted);
						
						if (condition) {
							console.log("elongating", index);

							entry.elongateRegion(index);
							$scope.$apply();
						}
					} catch (e) {
						console.log(JSON.stringify(e))
					}
				};
				var stopElongation = function () {
					entry.stopSelection();
					if ($scope.callback != null) $scope.callback(entry.letters[index].region);

					$scope.$apply();

				}
				$(letter).on("mouseenter", elongate);
				$(letter).click(function (event) {
					console.log(event.button);
				});
				$(letter).hover(elongate);
				$(letter).mousedown(elongate);
				$(letter).mouseup(stopElongation);
				$(letter).hide();
				$(letter).fadeIn(1000);

				$scope.$broadcast('$regionEdited');


			}
		}
	})
	.directive("regionEditor", function () {
		return {
			restrict: "E",
			scope: {
				region: "=",
				entry: "="
			},
			templateUrl: "partials/region-editor.html"
		}
	})

.filter("noBrackets", function () {
	return deleteBrackets;
})

.filter("reInput", function () {
	return resultInput;
})

.filter("reMatches", function () {
	return resultMatches;
});

function deleteBrackets(text) {
	if (text == undefined) return "";
	return text.substring(1, text.length - 1);
};

function resultInput(result) {
	if (result.length == 0) return "...";
	return result[0];
}

function resultMatches(result) {
	console.log(result.remove(0));
	if (result.length == 0) return "...";
	return result.remove(0).join(", ");
}