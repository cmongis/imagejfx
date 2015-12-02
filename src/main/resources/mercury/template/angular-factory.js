module.factory("ServiceName", ['$rootScope','$q', function ($rootScope, $q) {
    function Service($rootScope, $q) {
        var self = this;
        
  
        
        self._service = function() {
            return mercury.getService("ServiceName").getObject();
        };
        
        //ServicePromises
        
        
        
        
        
        return self;
    };
    return new Service($rootScope, $q);
}])

