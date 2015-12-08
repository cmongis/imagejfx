// Invoking immediate invoking function expression to wrap the angular module code
(function () {
    'use strict';
    // Define a module named interfaceApp
    var app = angular.module('interfaceApp', ['ngRoute','MercuryAngular']);

    app.config(['$routeProvider',
                 function ($routeProvider) {
            $routeProvider.

            when('/module', {
                templateUrl: 'modules/moduleList.html',
                controller: 'DataController'
            }).

            when('/module/:id', {
                template: '<input-output-directive></input-output-directive>',
                controller: 'MoreInputOutputController'
            }).

            otherwise({
                redirectTo: '/module'
            });
}]);

    // Define a directive that take a list of inputs and outputs
    app.directive('inputOutputDirective', function () {
        return {
            restrict: 'E',
            templateUrl: 'modules/inputOutput.html'
        };
    });
	

    // Define a filter to test whehter label of the object is present or not
    app.filter('className', function () {
        return function (item) {
            var className = item.className.split(".");
			return className[className.length-1];
        }
    });

    // Define a controller to get the list of data from the http service
    app.controller('DataController', ['$scope', '$http','ImageJService', function ($scope, $http,ImageJService) {
            
         $scope.dataArray = ImageJService.getModuleList();
            
    }
]);

    // Define a controller to get the list of data from the http service according to the id
    app.controller('MoreInputOutputController', function ($scope, $http, $routeParams) {
        $http.get('modules.json').success(function (data) {
            $scope.dataArray = data;
        });
        $scope.classNameId = $routeParams.id.split(".").pop();
    });

})();