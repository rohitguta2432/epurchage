(function() {
    'use strict';

    angular
        .module('epurchaseApp')
        .controller('EmployeeDetailController', EmployeeDetailController);

    EmployeeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Employee'];

    function EmployeeDetailController($scope, $rootScope, $stateParams, previousState, entity, Employee) {
        var vm = this;

        vm.employee = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('epurchaseApp:employeeUpdate', function(event, result) {
            vm.employee = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
