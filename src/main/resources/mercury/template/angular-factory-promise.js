
self.PromiseName = function (params) {
    var deferred = $q.defer();
    mercury.log("(JS) executing action : PromiseName");
    if (params === undefined) {
        params = {};
    }

    //if (window.params === undefined)
      //  window.params = [];

    //window.params.push(params);

    mercury.executeAction("ServiceName", "PromiseName", deferred, params);
    return deferred.promise;
};


