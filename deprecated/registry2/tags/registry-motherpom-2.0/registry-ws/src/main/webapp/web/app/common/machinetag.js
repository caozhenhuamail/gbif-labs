angular.module('machinetag', ['services.notifications'])

.controller('MachinetagCtrl', function ($scope, $state, $stateParams, $resource, notifications) {
  // help provide context with a label to the user
  var typeLabel = $state.current.context;
  $scope.typeLabel = typeLabel.charAt(0).toUpperCase() + typeLabel.slice(1);

  var Machinetag = $resource('../:type/:key/machinetag/:machinetagKey', {
    type : $state.current.context, // this context should be set in the parent statemachine (e.g. node)
    key : $stateParams.key,  
    machinetagKey : '@id'}
  );
  
  // loads the machinetags, and updates the scope
  var refreshScope = function() {
    Machinetag.query(function(data) {
      $scope.machinetags = data;
      $scope.counts.machinetag = data.length; // update parent counts
    });
  }
  
  refreshScope();

  $scope.types = [
    'SOURCE_ID',
    'URL',
    'LSID',
    'HANDLER',
    'DOI',
    'UUID',
    'FTP',
    'URI',
    'UNKNOWN',
    'GBIF_PORTAL',
    'GBIF_NODE',
    'GBIF_PARTICIPANT'
  ];	
  
  $scope.save = function(item) {
    item.createdBy = "TODO: security for machinetag.js";
    Machinetag.save(item,
      function() {
        notifications.pushForCurrentRoute("Machinetag successfully updated", 'info');
        refreshScope();
        $scope.editing = false; // close the form
      },
      function(response) {
        notifications.pushForCurrentRoute(response.data, 'error');
      });  
  }
  
  $scope.delete = function(machinetag) {
    Machinetag.delete({machinetagKey : machinetag.key},
      function() {
        notifications.pushForCurrentRoute("Machinetag successfully deleted", 'info');
        refreshScope();
        $scope.editing = false; // close the form
      },
      function(response) {
        notifications.pushForCurrentRoute(response.data, 'error');
      });  
  
  }
});