//Invoking Immediate Invoking Function Expression
(function () {

    //Defining the module called contextServiceApp
    angular.module('contextServiceApp', ['ngRoute'])
        .config(['$routeProvider',
                 function ($routeProvider) {
                $routeProvider.

                when('/widgetList', {
                    templateUrl: 'context-manager/widgetList.htm',
                    controller: 'WidgetController'
                }).

                when('/widgetList/:id', {
                    templateUrl: 'context-manager/moreWidget.htm',
                    controller: 'MoreWidgetController'
                }).

                when('/newWidgetLink/:id', {
                    templateUrl: 'context-manager/newWidgetLink.htm',
                    controller: 'NewWidgetLinkController'
                }).

                when('/contextList', {
                    templateUrl: 'context-manager/contextList.htm',
                    controller: 'ContextController'
                }).

                when('/contextList/:id', {
                    templateUrl: 'context-manager/moreContextList.htm',
                    controller: 'MoreContextController'
                }).

                when('/newContextLink/:id', {
                    templateUrl: 'context-manager/newContextLink.htm',
                    controller: 'NewContextLinkController'
                }).

                otherwise({
                    redirectTo: '/widgetList'
                });

}])

    // Defining WidgetController to get widget list from the ContextService
    .controller('WidgetController', function ($scope, ContextService) {
        $scope.widgetList = ContextService.getWidgetList();

    })

    // Defining MoreWidgetController to see more details about widget
    .controller('MoreWidgetController', function ($scope, ContextService, $routeParams, $location) {
        $scope.linkList = ContextService.getLinkList();
        $scope.widgetId = $routeParams.id;

        // unlink the widget and context
        $scope.unlink = function (widget, context, linkType) {
            ContextService.unlink(widget, context, linkType);
            $scope.linkList = ContextService.getLinkList();
        };
    })

    // Defining NewWidgetLinkController to link the widget and context
    .controller('NewWidgetLinkController', function ($scope, ContextService, $location, $routeParams) {
        $scope.contextList = ContextService.getContextList();
        $scope.widgetId = $routeParams.id;

        // link widget and context
        $scope.link = function (widget, context, linkType) {
            ContextService.link(widget, context, linkType);
            $scope.linkList = ContextService.getLinkList();
            $location.path('/widgetList/' + $scope.widgetId);
        };

        //Defining exists function to check whether the given link is exist or not
        $scope.exists = function (widget, context, linkType) {
            $scope.linkList = ContextService.getLinkList();
            for (var i = 0; i < $scope.linkList.length; i++) {
                var currentLink = $scope.linkList[i];
                if (widget === currentLink.widget && context === currentLink.context && linkType === currentLink.linkType) {
                    return true;
                }
            }
        };

        // Removing the particular context from ContextList
        $scope.delete = function (index) {
            $scope.contextList.splice(index, 1);
        };
    })

    // Defining ContextController to get context list from the  ContextService
    .controller('ContextController', function ($scope, ContextService) {
        $scope.contextList = ContextService.getContextList();
    })

    // Defing MoreContextController to see more details about the context
    .controller('MoreContextController', function ($scope, ContextService, $routeParams) {
        $scope.linkList = ContextService.getLinkList();
        $scope.contextId = $routeParams.id;

        // unlink the widget and context
        $scope.unlink = function (widget, context, linkType) {
            ContextService.unlink(widget, context, linkType);
            $scope.linkList = ContextService.getLinkList();
        };
    })

    // Defining NewContextLinkController to link widget and context
    .controller('NewContextLinkController', function ($scope, ContextService, $location, $routeParams) {

        $scope.widgetList = ContextService.getWidgetList();
        $scope.contextId = $routeParams.id;

        // link widget and context
        $scope.link = function (widget, context, linkType) {
            ContextService.link(widget, context, linkType);
            $scope.linkList = ContextService.getLinkList();
            $location.path('/contextList/' + $scope.contextId);
        };

        // Defining exists function to check whether the given link is exist or not
        $scope.exists = function (widget, context, linkType) {
            $scope.linkList = ContextService.getLinkList();
            for (var i = 0; i < $scope.linkList.length; i++) {
                var currentLink = $scope.linkList[i];
                if (widget === currentLink.widget && context === currentLink.context && linkType === currentLink.linkType) {
                    return true;
                }
            }
        };

        // Removing particular widget from the widget list 
        $scope.delete = function (index) {
            $scope.widgetList.splice(index, 1);
        };
    });

})();