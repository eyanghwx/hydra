//'use strict';

/* jasmine specs for controllers go here */

describe('Controller tests', function () {

  // Unit test for listing, and start/stop applications.
  describe('AppListController', function() {
    var scope, ctrl, http, httpBackend;

    beforeEach(module('app'));
    beforeEach(inject(function ($controller, $rootScope, $http, $httpBackend) {
      scope = $rootScope.$new();
      rootScope = $rootScope;
      http = $http;
      httpBackend = $httpBackend;
      ctrl = $controller('AppListController', {$scope: scope});
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should contain appList', function () {
      expect(scope.appList.length).toBe(0);
    });

    it('should test to delete app', function () {
      httpBackend.expectDELETE('/v1/app_list/aabbccdd/aabbccdd').respond(200, {data:"Application Deleted."});
      spyOn(rootScope, '$emit');
      scope.$apply(function() {
        scope.deleteApp("aabbccdd","aabbccdd");
      });
      httpBackend.flush();
      expect(rootScope.$emit).toHaveBeenCalledWith('RefreshAppList', {});
    });

    it('should test to refresh appList', function() {
      httpBackend.expectGET('/v1/app_list').respond(200, [{id:"jenkins",name:"jenkins",app:"",yarnfile:{}}]);
      spyOn(rootScope, '$emit');
      scope.$apply(function() {
        scope.refreshList();
      });
      httpBackend.flush();
      expect(rootScope.$emit).toHaveBeenCalledWith('hideLoadScreen', {});
    })
  });

  // Unit test for inspect YARN application details.
  describe('AppDetailsController', function() {
    var scope, ctrl, http, routeParams, httpBackend;

    beforeEach(module('app'));
    beforeEach(inject(function ($controller, $rootScope, $http, $routeParams, $httpBackend) {
      scope = $rootScope.$new();
      rootScope = $rootScope;
      http = $http;
      routeParams = $routeParams;
      httpBackend = $httpBackend;
      ctrl = $controller('AppDetailsController', {$scope: scope});
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should contain unknown state', function () {
      expect(scope.details.yarnfile.state).toBe("UNKNOWN");
    });

    it('should run test to refrshed details', function () {
      httpBackend.expectGET('/v1/app_details/status/aabbccdd').respond(200, {yarnfile:{state: "ACCEPTED", components:[]}});
      scope.$apply(function() {
        routeParams.id = "aabbccdd";
        scope.appName = "aabbccdd";
        scope.refreshAppDetails();
      });
      httpBackend.flush();
      expect(scope.details.yarnfile.state).toBe("ACCEPTED");
    });

    it('should run test to restart app', function () {
      httpBackend.expectPOST('/v1/app_details/restart/aabbccdd').respond(200, {yarnfile:{state: "ACCEPTED", components:[]}});
      spyOn(rootScope, '$emit');
      scope.$apply(function() {
        scope.restartApp("aabbccdd");
        
      });
      httpBackend.flush();
      expect(rootScope.$emit).toHaveBeenCalledWith('RefreshAppDetails', {});
    });

    it('should run test to stop app', function () {
      httpBackend.expectPOST('/v1/app_details/stop/aabbccdd').respond(200, {yarnfile:{state: "STOPPED", components:[]}});
      spyOn(rootScope, '$emit');
      scope.$apply(function() {
        scope.stopApp("aabbccdd");
      });
      httpBackend.flush();
      expect(rootScope.$emit).toHaveBeenCalledWith('RefreshAppDetails', {});
    });

  });

  // Unit test for deploying app, and search for apps from Yarn Appstore.
  describe('AppStoreController', function() {
    var scope, ctrl, http, httpBackend;

    beforeEach(module('app'));
    beforeEach(inject(function ($controller, $rootScope, $http, $httpBackend) {
      scope = $rootScope.$new();
      http = $http;
      httpBackend = $httpBackend;
      ctrl = $controller('AppStoreController', {$scope: scope});
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should contain appStore', function () {
      expect(scope.appStore.length).toBe(0);
    });

    it('should run test to deploy app', function() {
      httpBackend.expectPOST('/v1/app_list/aabbccdd').respond(204, {data:'ACCEPTED'});
      scope.$apply(function() {
        scope.deployApp("aabbccdd");
      });
      httpBackend.flush();
    });

    it('should run test to search for apps', function() {
      httpBackend.expectGET('/v1/app_store/search?q=aabbccdd').respond(204, {data:'ACCEPTED'});
      scope.$apply(function() {
        scope.searchText = "aabbccdd";
        scope.change("aabbccdd");
      });
      httpBackend.flush();
      expect(scope.appStore.data).toBe('ACCEPTED');
    });

  });

  // Unit test cases for creating a new YARN application.
  describe('NewAppController', function() {
    var scope, ctrl, http, httpBackend;

    beforeEach(module('app'));
    beforeEach(inject(function ($controller, $rootScope, $http, $httpBackend) {
      scope = $rootScope.$new();
      http = $http;
      httpBackend = $httpBackend;
      ctrl = $controller('NewAppController', {$scope: scope});
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should contain details', function () {
      expect(scope.details.name).toBe("");
    });

    it('should run test to register data to backend', function() {
      httpBackend.expectPOST('/v1/app_store/register').respond(204, {data:'ACCEPTED'});
      scope.$apply(function() {
        scope.save();
      });
      httpBackend.flush();
      expect(scope.message).toEqual("Application published successfully.");
    });

    it('should run test to fail register data to backend', function() {
      httpBackend.expectPOST('/v1/app_store/register').respond(500, {data:'INTERNAL SERVER ERROR'});
      scope.$apply(function() {
        scope.save();
      });
      httpBackend.flush();
      expect(scope.error).toEqual("Error in registering application configuration.");
    });

    it('should run test to add more component to details', function() {
      expect(scope.details.components.length).toEqual(1);
      scope.$apply(function() {
        scope.add();
      });
      expect(scope.details.components.length).toEqual(2);
    });

    it('should run test to remove second component', function() {
      expect(scope.details.components.length).toEqual(1);
      scope.$apply(function() {
        scope.add();
        scope.remove(1);
      });
      expect(scope.details.components.length).toEqual(1);
    });
  });

});
