self.MethodName = function() {
    var service = self._service();
    
    var method = service.MethodName;
    var args = [];
    for(var i = 0; i < arguments.length;i++) {
        args.push(arguments[i]);
    }
    
    while(args.length < method.length) {
        args.push(null);
    }
    
    return service.MethodName.apply(service,args);
};
