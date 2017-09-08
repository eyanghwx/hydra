/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var controllers = angular.module("controllers", []);

controllers.controller("AppListController", [ '$scope', '$rootScope', '$http',
    function($scope, $rootScope, $http) {
      $scope.appList = [];

      function successCallback(response) {
        $scope.appList = response.data;
        $rootScope.$emit("hideLoadScreen", {});
      }

      function errorCallback(response) {
        $rootScope.$emit("hideLoadScreen", {});
        console.log("Error in downloading application list");
      }

      $rootScope.$on("RefreshAppList", function() {
        $scope.refreshList();
      });

      $scope.refreshList = function() {
        $http({
          method : 'GET',
          url : '/v1/app_list'
        }).then(successCallback, errorCallback);
      }

      $scope.deleteApp = function(id, name) {
        $rootScope.$emit("showLoadScreen", {});
        $http({
          method: 'DELETE',
          url: '/v1/app_list/' + id + '/' + name
        }).then(function(response) {
          $rootScope.$emit("RefreshAppList", {});
        }, function(response) {
          console.log(response);
        });
      }
      $http({
        method : 'GET',
        url : '/v1/app_list'
      }).then(successCallback, errorCallback);
    } ]);

controllers.controller("AppStoreController", [ '$scope', '$rootScope', '$http',
    function($scope, $rootScope, $http) {
      $scope.canDeployApp = function() {
        return false;
      };
      $scope.appStore = [];
      $scope.searchText = null;

      function successCallback(response) {
        $scope.appStore = response.data;
      }

      function errorCallback(response) {
        console.log("Error in downloading AppStore information.");
      }

      $scope.deployApp = function(id) {
        $rootScope.$emit("showLoadScreen", {});
        $http({
          method : 'POST',
          url : '/v1/app_list/' + id
        }).then(function(data, status, headers, config) {
          $rootScope.$emit("RefreshAppList", {});
          window.location = '/#!/app/' + data.data.id;
        }, function(data, status, headers, config) {
          console.log('error', data, status);
        });
      }

      $http({
        method : 'GET',
        url : '/v1/app_store/recommended'
      }).then(successCallback, errorCallback);

      $scope.change = function(text) {
        var q = $scope.searchText;
        $http({
          method : 'GET',
          url : '/v1/app_store/search?q=' + q
        }).then(successCallback, errorCallback);
      }
    } ]);

controllers.controller("AppDetailsController", [ '$scope', '$rootScope', '$http',
    '$routeParams', function($scope, $rootScope, $http, $routeParams) {
      $scope.canDeployApp = function() {
        return true;
      };
      $scope.details = {"yarnfile":{"state":"UNKNOWN"}};
      $scope.appName = $routeParams.id;

      $rootScope.$on("RefreshAppDetails", function() {
        $scope.refreshAppDetails();
      });

      $scope.refreshAppDetails = function() {
        $http({
          method : 'GET',
          url : '/v1/app_details/status/' + $scope.appName
        }).then(successCallback, errorCallback);
      }

      $scope.stopApp = function(id) {
        $http({
          method : 'POST',
          url : '/v1/app_details/stop/' + id
        }).then(function(data, status, header, config) {
          $rootScope.$emit("RefreshAppDetails", {});
        }, errorCallback);
      }

      $scope.restartApp = function(id) {
        $http({
          method : 'POST',
          url : '/v1/app_details/restart/' + id
        }).then(function(data, status, header, config) {
          $rootScope.$emit("RefreshAppDetails", {});
        }, errorCallback);
      }

      function successCallback(response) {
        if (response.data.yarnfile.components.length!=0) {
          $scope.details = response.data;
        } else {
          // When application is in accepted or failed state, it does not
          // have components detail, hence we update states only.
          $scope.details.yarnfile.state = response.data.yarnfile.state;
        }
      }

      function errorCallback(response) {
        console.log("Error in getting application detail");
      }

      $http({
        method : 'GET',
        url : '/v1/app_details/config/' + $scope.appName
      }).then(successCallback, errorCallback);

      $rootScope.$emit("RefreshAppDetails", {});
    } ]);

controllers.controller("NewAppController", [ '$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
    $scope.details = {
        "name" : "",
        "organization" : "",
        "description" : "",
        "icon" : "",
        "components" : [
          {
            "name" : "",
            "number_of_containers" : 1,
            "artifact" : {
              "id": "centos:latest"
            },
            "resource" : {
              "cpus" : 1,
              "memory" : 2048
            },
            "run_privileged_container" : false,
            "dependencies" : [],
            "placement_policy" : {
              "label" : ""
            },
            "configuration" : {
              "env" : {
              }
            }
          }
        ]
    };
    
    $scope.template = {
        "name" : "",
        "number_of_containers" : 1,
        "artifact" : {
          "id": "centos:latest"
        },
        "resource" : {
          "cpus" : 1,
          "memory" : 2048
        },
        "run_privileged_container" : false,
        "dependencies" : [],
        "placement_policy" : {
          "label" : ""
        },
        "configuration" : {
          "env" : {
          }
        }
    };
    
    $scope.message = null;
    $scope.error = null;
    
    $scope.save = function() {
      $http({
        method : 'POST',
        url : '/v1/app_store/register',
        data : JSON.stringify($scope.details)
      }).then(successCallback, errorCallback)
    }
    
    $scope.add = function() {
      $scope.details.components.push($scope.template);
    }
    
    $scope.remove = function(index) {
      $scope.details.components.splice(index, 1);
    }

    function successCallback(response) {
      $scope.message = "Application published successfully.";
    }

    function errorCallback(response) {
      $scope.error = "Error in registering application configuration.";
    }

    } ]);

controllers.controller("LoadScreenController", [ '$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
  $scope.loadScreen = "hide";
  
  $rootScope.$on("showLoadScreen", function() {
    $scope.loadScreen = "show";
  });

  $rootScope.$on("hideLoadScreen", function() {
    $scope.loadScreen = "hide";
  });

}]);
