// app.js
// create our angular app and inject ngAnimate and ui-router
// =============================================================================
angular.module('formApp', ['ngAnimate', 'ui.router'])

// configuring our routes
// =============================================================================
.config(function($stateProvider, $urlRouterProvider) {

    $stateProvider
        .state('form', { url: '/form', templateUrl: 'form.html', controller: 'formController' })
        .state('form.about-you', { url: '/about-you', templateUrl: 'form-about-you.html' })
        .state('form.stall-details', { url: '/stall-details', templateUrl: 'form-stall-details.html' })
        .state('form.stall-categories', { url: '/stall-categories', templateUrl: 'form-stall-categories.html' })
        .state('form.payment', { url: '/payment', templateUrl: 'form-payment.html' })
        .state('form.insurance', { url: '/insurance', templateUrl: 'form-insurance.html' })
        .state('form.logistics', { url: '/logistics', templateUrl: 'form-logistics.html' })
        .state('form.terms-and-conditions', { url: '/terms-and-conditions', templateUrl: 'form-terms-and-conditions.html' });

    // catch all route
    // send users to the form page
    $urlRouterProvider.otherwise('/form/about-you');
})

// our controller for the form
// =============================================================================
.controller('formController', function($scope, $http, $location) {

    // we will store all of our form data in this object
    $scope.formData = {};

    // function to process the form
    $scope.processForm = function() {
        $http.post('/complete-form', $scope.formData).then(
            function (response) {
                $location.path('/complete');
            }
        );
    };

});