/* 
 * Copyright (C) 2015 Cyril MONGIS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


angular.module("MyApp", ['MercuryAngular'])
        .controller("MainCtrl",function ($scope, DummyService) {

        mercury.log("loading ?");
            //$scope.name = DummyService.getServiceName();
            $scope.result = DummyService.getFileInfoSync("./").getAbsolutePath();
            $scope.progress = 0;
            $scope.message = "Doing nothing...";
            
            $scope.doSomethingLong = function () {
                
                $scope.message = "Running !";
                
                //returns a promise
                DummyService.doAsyncThings({})
                        
                        .then(
                        // function executed when the processed is over (result of deferred.resolve())
                        function (data) {
                            
                            $scope.message = "Finished !";
                            $scope.result = data.name; // the result object has already been transformed to JSON Object
                            $scope.progress = 0;
                        },
                        // nothing happens when it fails (for instance)
                        null, 
                        // function to execute when progressing (result of deferred.notify())
                        function (data) {
                            // the data is the count object sent by the deferred.notifyJSON('count',i);
                            // and arrives in the form {'count':2}
                            $scope.progress = data.count * 10;
                            $scope.message = data.count + "/"+10;
                        });
                        
                        
            };



        });

