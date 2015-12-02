var module = angular.module('MercuryAngular',[]);
module.factory("DocumentationService", ['$rootScope','$q', function ($rootScope, $q) {
    function Service($rootScope, $q) {
        var self = this;
        
self.getServiceDocumentation = function (params) {
    var deferred = $q.defer();
    mercury.log("(JS) executing action : getServiceDocumentation");
    if (params === undefined) {
        params = "";
    }

    //if (window.params === undefined)
      //  window.params = [];

    //window.params.push(params);

    mercury.executeAction("DocumentationService", "getServiceDocumentation", deferred, params);
    return deferred.promise;
};



self.getServiceList = function (params) {
    var deferred = $q.defer();
    mercury.log("(JS) executing action : getServiceList");
    if (params === undefined) {
        params = "";
    }

    //if (window.params === undefined)
      //  window.params = [];

    //window.params.push(params);

    mercury.executeAction("DocumentationService", "getServiceList", deferred, params);
    return deferred.promise;
};



    };
    return new Service($rootScope, $q);
}])

;