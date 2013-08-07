angular.module('dataset', [
  'restangular', 
  'services.notifications', 
  'contact', 
  'endpoint',
  'identifier', 
  'tag', 
  'machinetag', 
  'comment'])

/**
 * Nested stated provider using dot notation (item.detail has a parent of item) and the 
 * nested view is rendered into the parent template ui.view div.  A single controller
 * governs the actions on the page. 
 */
.config(['$stateProvider', function ($stateProvider, $stateParams, Dataset) {
  $stateProvider.state('dataset-search', {
    abstract: true,
    url: '/dataset-search',  
    templateUrl: 'app/dataset/dataset-search.tpl.html',
    controller: 'DatasetSearchCtrl',
  })
  .state('dataset-search.search', {  
    url: '',
    templateUrl: 'app/dataset/dataset-results.tpl.html'
  })
  .state('dataset-search.deleted', {  
    url: '/deleted',   
    templateUrl: 'app/dataset/dataset-deleted.tpl.html'
  })
  .state('dataset-search.duplicate', {  
    url: '/duplicate',   
    templateUrl: 'app/dataset/dataset-duplicate.tpl.html'
  })
  .state('dataset-search.subDataset', {  
    url: '/subDataset',   
    templateUrl: 'app/dataset/dataset-subDataset.tpl.html'
  })
  .state('dataset-search.withNoEndpoint', {  
    url: '/withNoEndpoint',   
    templateUrl: 'app/dataset/dataset-withNoEndpoint.tpl.html'
  })
  .state('dataset-search.create', {  
    url: '/create',   
    templateUrl: 'app/dataset/dataset-edit.tpl.html',
    controller: 'DatasetCreateCtrl'
  })
  .state('dataset', {
    url: '/dataset/{key}',  // {type} to provide context to things like identifier 
    abstract: true, 
    templateUrl: 'app/dataset/dataset-main.tpl.html',
    controller: 'DatasetCtrl'
  })
  .state('dataset.detail', {  
    url: '',   
    templateUrl: 'app/dataset/dataset-overview.tpl.html'
  })
  .state('dataset.edit', {
    url: '/edit',
    templateUrl: 'app/dataset/dataset-edit.tpl.html',
  })  
  .state('dataset.endpoint', {  
    url: '/endpoint',   
    templateUrl: 'app/common/endpoint-list.tpl.html',
    controller: "EndpointCtrl",  
    context: 'dataset', // necessary for reusing the components
    heading: 'Dataset endpoints', // title for the sub pane     
  })
  .state('dataset.identifier', {  
    url: '/identifier',   
    templateUrl: 'app/common/identifier-list.tpl.html',
    controller: "IdentifierCtrl",  
    context: 'dataset', 
    heading: 'Dataset identifiers', 
  })
  .state('dataset.contact', {  
    url: '/contact',   
    templateUrl: 'app/common/contact-list.tpl.html',
    controller: "ContactCtrl",  
    context: 'dataset', 
    heading: 'Dataset contacts', 
  })
  .state('dataset.tag', {  
    url: '/tag',   
    templateUrl: 'app/common/tag-list.tpl.html',
    controller: "TagCtrl",  
    context: 'dataset', 
    heading: 'Dataset tags', 
  })
  .state('dataset.machinetag', {  
    url: '/machineTag',   
    templateUrl: 'app/common/machinetag-list.tpl.html',
    controller: "MachinetagCtrl",  
    context: 'dataset', 
    heading: 'Dataset machine tags', 
  })
  .state('dataset.comment', {  
    url: '/comment',   
    templateUrl: 'app/common/comment-list.tpl.html',
    controller: "CommentCtrl",  
    context: 'dataset', 
    heading: 'Dataset comments', 
  })
}])

/**
 * The single detail controller
 */
.controller('DatasetCtrl', function ($scope, $state, $stateParams, notifications, Restangular, DEFAULT_PAGE_SIZE) {
  var key = $stateParams.key;
  
  // shared across sub views
  $scope.counts = {}; 
  
  var load = function() {
    Restangular.one('dataset', key).get().then(function(dataset) {
      $scope.dataset = dataset;
      $scope.counts.contacts = _.size(dataset.contacts);
      $scope.counts.identifiers = _.size(dataset.identifiers); 
      $scope.counts.endpoints = _.size(dataset.endpoints); 
      $scope.counts.tags = _.size(dataset.tags); 
      $scope.counts.machinetags = _.size(dataset.machineTags); 
      $scope.counts.comments = _.size(dataset.comments); 
      
      dataset.owningOrganization = Restangular.one('organization', dataset.owningOrganizationKey).get();
      dataset.installation = Restangular.one('installation', dataset.installationKey).get();
      dataset.parentDataset = Restangular.one('dataset', dataset.parentDatasetKey).get();
      dataset.duplicateOfDataset = Restangular.one('dataset', dataset.duplicateOfDatasetKey).get();
      
      dataset.getList('constituents', {limit: DEFAULT_PAGE_SIZE}).then(function(response) {
          $scope.subDatasets = response.results;
          $scope.counts.subDatasets = response.count;
        });
    });
  }
  load();  

  // populate the dropdowns
  $scope.datasetTypes = Restangular.all("enumeration/basic/DatasetType").getList();
  $scope.datasetSubTypes = Restangular.all("enumeration/basic/DatasetSubtype").getList();
  $scope.languages = Restangular.all("enumeration/basic/Language").getList();
  
	// transitions to a new view, correctly setting up the path
  $scope.transitionTo = function (target) {
    $state.transitionTo('dataset.' + target, { key: key, type: "dataset" }); 
  }
  $scope.redirectTo = function (type, key) {
    $state.transitionTo(type + '.detail', { key: key, type: type }); 
  }
	
	$scope.save = function (dataset) {
    dataset.put().then( 
      function() {
        notifications.pushForNextRoute("Dataset successfully updated", 'info');
        $scope.transitionTo("detail");
      },
      function(response) {
        notifications.pushForCurrentRoute(response.data, 'error');
      }
    );
  }
  
  $scope.cancelEdit = function () {
    $scope.dataset = Dataset.get({ key: key });
    $scope.transitionTo("detail");
  }  
})

.controller('DatasetSearchCtrl', function ($scope, $state, Restangular, DEFAULT_PAGE_SIZE) {
  var dataset = Restangular.all("dataset");
  $scope.search = function(q) {
    dataset.getList({q:q, limit:DEFAULT_PAGE_SIZE}).then(function(data) {
      $scope.resultsCount = data.count;
      $scope.results = data.results;
      $scope.searchString = q;
    });
  }
  $scope.search(""); // start with empty search  

  // load quick lists
  dataset.all("deleted").getList({limit:DEFAULT_PAGE_SIZE}).then(function(data) {  
    $scope.deletedCount = data.count;
    $scope.deleted = data.results;
  });
  dataset.all("duplicate").getList({limit:DEFAULT_PAGE_SIZE}).then(function(data) {  
    $scope.duplicateCount = data.count;
    $scope.duplicate = data.results;
  });
  dataset.all("subDataset").getList({limit:DEFAULT_PAGE_SIZE}).then(function(data) {  
    $scope.subDatasetCount = data.count;
    $scope.subDataset = data.results;
  });
  dataset.all("withNoEndpoint").getList({limit:DEFAULT_PAGE_SIZE}).then(function(data) {  
    $scope.withNoEndpointCount = data.count;
    $scope.withNoEndpoint = data.results;
  });
  
  $scope.openDataset = function(dataset) {
    $state.transitionTo('dataset.detail', {key: dataset.key})
  }
})


.controller('DatasetCreateCtrl', function ($scope, $state, notifications, Restangular) {
  $scope.datasetTypes = Restangular.all("enumeration/basic/DatasetType").getList();
  $scope.datasetSubTypes = Restangular.all("enumeration/basic/DatasetSubtype").getList();
  $scope.languages = Restangular.all("enumeration/basic/Language").getList();
	
	// sensible defaults for creation
	$scope.dataset = {};
	$scope.dataset.type="OCCURRENCE"; 
	$scope.dataset.language="ENGLISH"; 

  $scope.save = function (dataset) {
    if (dataset != undefined) {
      Restangular.all("dataset").post(dataset).then(function(data) {
        notifications.pushForNextRoute("Dataset successfully updated", 'info');
        // strip the quotes
        $state.transitionTo('dataset.detail', { key: data.replace(/["]/g,''), type: "installation" }); 
      }, function(error) { 
        notifications.pushForCurrentRoute(error.data, 'error');
      });
    }        
  }
  
  $scope.cancelEdit = function() {
    $state.transitionTo('dataset-search.search'); 
  }  
});