var app = angular.module('app', [
   'ngRoute',
   'filters',
   'controllers'
]);

app.config(['$routeProvider',
  function ($routeProvider) {
    $routeProvider.when('/', {
      templateUrl: 'partials/home.html',
      controller: 'AppStoreController'
    }).when('/app/:id', {
      templateUrl: 'partials/details.html',
      controller: 'AppDetailsController'
    }).otherwise({
      redirectTo: '/'
    });
}]);
