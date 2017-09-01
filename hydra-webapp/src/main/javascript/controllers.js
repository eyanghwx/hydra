var controllers = angular.module("controllers", []);

controllers.controller("HomeController", [ '$scope', function($scope) {
  $scope.debug = false;
  $scope.title = 'Hello ';
} ]);

controllers.controller("AppListController", [ '$scope', '$rootScope', '$http',
    function($scope, $rootScope, $http) {
      $scope.appList = [];

      function successCallback(response) {
        $scope.appList = response.data;
      }

      function errorCallback(response) {
        console.log("Error in downloading application list");
      }

      $rootScope.$on("RefreshAppList", function() {
        $scope.refreshList();
      });

      $scope.refreshList = function() {
        $http({
          method : 'GET',
          url : '/v1/appList'
        }).then(successCallback, errorCallback);
      }

      $scope.deleteApp = function(id, name) {
        $http({
          method: 'DELETE',
          url: '/v1/appList/' + id + '/' + name
        }).then(function(response) {
          $rootScope.$emit("RefreshAppList", {});
        }, function(response) {
          console.log(response);
        });
      }
      $http({
        method : 'GET',
        url : '/v1/appList'
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
        $http({
          method : 'POST',
          url : '/v1/appList/' + id
        }).then(function(data, status, headers, config) {
          $rootScope.$emit("RefreshAppList", {});
          window.location = '/#!/app/' + data.data.id;
        }, function(data, status, headers, config) {
          console.log('error', data, status);
        });
      }

      $http({
        method : 'GET',
        url : '/v1/appStore/recommended'
      }).then(successCallback, errorCallback);

      $scope.change = function(text) {
        var q = $scope.searchText;
        $http({
          method : 'GET',
          url : '/v1/appStore/search?q=' + q
        }).then(successCallback, errorCallback);
      }
    } ]);

controllers.controller("AppDetailsController", [ '$scope', '$rootScope', '$http',
    '$routeParams', function($scope, $rootScope, $http, $routeParams) {
      $scope.canDeployApp = function() {
        return true;
      };
      $scope.details = [];
      $scope.appName = $routeParams.id;

      $rootScope.$on("RefreshAppDetails", function() {
        $scope.refreshAppDetails();
      });

      $scope.refreshAppDetails = function() {
        $http({
          method : 'GET',
          url : '/v1/appDetails/status/' + $scope.appName
        }).then(successCallback, errorCallback);
      }

      $scope.stopApp = function(id) {
        $http({
          method : 'POST',
          url : '/v1/appDetails/stop/' + id
        }).then(function(data, status, header, config) {
          $rootScope.$emit("RefreshAppDetails", {});
        }, errorCallback);
      }

      $scope.restartApp = function(id) {
        $http({
          method : 'POST',
          url : '/v1/appDetails/restart/' + id
        }).then(function(data, status, header, config) {
          $rootScope.$emit("RefreshAppDetails", {});
        }, errorCallback);
      }

      function successCallback(response) {
        $scope.details = response.data;
      }

      function errorCallback(response) {
        console.log("Error in getting application detail");
      }

      $http({
        method : 'GET',
        url : '/v1/appDetails/config/' + $scope.appName
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
      console.log(JSON.stringify($scope.details));
      $http({
        method : 'POST',
        url : '/v1/appStore/register',
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
