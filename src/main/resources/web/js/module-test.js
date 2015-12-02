


angular.module("ModuleTester", ["MercuryAngular"])
	.controller("TestController", function ($scope, TestService, DocumentationService) {

		var self = $scope;

		self.messages = "";

		self.test = function () {

			mercury.log("launching the test");
			TestService.testJSONParsing({
				aInt: 1,
				aList: ["I", "want", 4, 4.3],
				aString: "il voulait du pain",
				aDouble: 0.242
			}).then(function (result) {
				self.messages = JSON.stringify(result);
				mercury.log("they contacted me !");
				$scope.$apply();
			}).then(undefined, undefined, function (p) {
				mercury.log("they even send me messages");
				self.messages = JSON.stringify(p);
				$scope.$apply();
			});
		};

	


	});

angular.module("Kn.DocNavigation", ["ngRoute", "MercuryAngular"])


.config(["$routeProvider", function ($routeProvider) {

	$routeProvider
		.when("/", {
			templateUrl: "partials/service-list.html",
			controller: "Kn.ServiceListCtrl"
		})
		.when("/service/:serviceName", {
			templateUrl: function () {
				return "partials/module.html"
			},
			controller: "Kn.ServiceDisplayCtrl"
		})
		.otherwise({
			redirectTo: "/"
		});
	}])


// display the list of services available
.controller("Kn.ServiceListCtrl", function ($scope, DocumentationService) {
		$scope.data = ["Service1", "Service2"];

	
		DocumentationService.getServiceList({}).then(function (data) {
			//$scope.test = JSON.stringify(data);
			$scope.data = data;

		});
		$scope.test = "it works";
		mercury.log("end of the controller");
	 //throw {name:"suck my dick",message:"you heard me"};

	})
	// display the list of methods for a service
	.controller("Kn.ServiceDisplayCtrl", function ($scope, $routeParams, DocumentationService, $injector) {

		
		$scope.serviceName = $routeParams.serviceName;

		$scope.methods = [];
	
		$scope.currentMethod = undefined;

		mercury.log("they are creating me !");

		DocumentationService.getMethodList({
			serviceName: $routeParams.serviceName
		}).then(function (data) {

			$scope.methods = data;
		});

		$scope.testMethod = function (m) {
			$scope.currentMethod = m;
			$scope.testInput = m.inputExample;
			mercury.log(m.inputExample);
			$scope.testOutput = "";
			$scope.msg = "";
		};


		$scope.testInput = "{}";


		$scope.testOutput = {
			nothing: "for now"
		};

		$scope.msg = "";
		$scope.msgColor = {};


		$scope.displayMessage = function (msg, color) {
			$scope.msg = msg;
			$scope.msgColor = color;
		};



		$scope.launchTest = function () {
			mercury.log("something is happenging");
			mercury.log($scope.serviceName);
			mercury.log($scope.testInput);
			
			var input;
			var isMethodSync = $scope.currentMethod.sync;
			var methodName = $scope.currentMethod.name;
			var serviceName = $scope.serviceName;
			
			
			try {
				if (isMethodSync) {
					//input = JSON.parse("[" + $scope.testInput + "]");
					input = $scope.testInput;
				} else {
					input = JSON.parse($scope.testInput);
				}
			} catch (e) {
				$scope.displayMessage("Couldn't parse your input JSON !", "red");
				return;
			}
			var service = $injector
				.get(serviceName);
			var method = service[methodName];
			
			mercury.log(service);
			mercury.log(serviceName);
			mercury.log(service[methodName]);
			
			var serviceOject = mercury.getService(serviceName).getObject();
			//mercury.getService(serviceName).getObject()[methodName]("","/");
			
			
			
			
			if (isMethodSync) {
				if(input == null || input == undefined) input = "";
				var cmd = "$injector.get('"+serviceName+"')."+methodName+"("+input+");";
				
				var output = eval(cmd);
				
				$scope.testOutput = JSON.stringify(output,null,2);
				

			} else {

				method(input)
					.then(function (data) {

						try {
							$scope.testOutput = JSON.stringify(data, null, 2);
							$scope.displayMessage("Done", "green");
							return;

						} catch (e) {
							$scope.testOutput = data;
							$scope.displayMessage("Can not stringify the result", "pink");
						}


					});
			}
		};





	})
	.controller("Kn.ServiceTesterCtrl", function ($scope) {



	})




.filter("hightlightJson", function () {
	return syntaxHightlight;
})

;


angular.module('ModuleTester', []).factory('$exceptionHandler', function() {
  return function(exception, cause) {
	  
    exception.message += ' (caused by "' + cause + '")';
	  mercury.log(exception.message);
    throw exception;
  };
});

function syntaxHighlight(json) {
	json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
		var cls = 'number';
		if (/^"/.test(match)) {
			if (/:$/.test(match)) {
				cls = 'key';
			} else {
				cls = 'string';
			}
		} else if (/true|false/.test(match)) {
			cls = 'boolean';
		} else if (/null/.test(match)) {
			cls = 'null';
		}
		return '<span class="' + cls + '">' + match + '</span>';
	});
}