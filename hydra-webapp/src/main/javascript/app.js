var app = angular.module('app', [
   'ngRoute',
   'filters',
   'controllers'
]);

app.directive('jsonText', function() {
  return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {
        function into(input) {
          console.log(JSON.parse(input));
          return JSON.parse(input);
        }
        function out(data) {
          return JSON.stringify(data);
        }
        ngModel.$parsers.push(into);
        ngModel.$formatters.push(out);
      }
  };
});

app.config(['$routeProvider',
  function ($routeProvider) {
    $routeProvider.when('/', {
      templateUrl: 'partials/home.html',
      controller: 'AppStoreController'
    }).when('/app/:id', {
      templateUrl: 'partials/details.html',
      controller: 'AppDetailsController'
    }).when('/new', {
      templateUrl: 'partials/new.html',
      controller: 'NewAppController'
    }).otherwise({
      redirectTo: '/'
    });
}]);
