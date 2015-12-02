angular.module("IndexApp",["MercuryAngular"])
	.controller("MainCtrl",function($scope,AppService) {
		
	$scope.launchApp = function(app) {
		AppService.showApp(app);
	};
	
		
});