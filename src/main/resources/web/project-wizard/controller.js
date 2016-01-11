angular.module("ProjectCreator", ["MercuryAngular", "ngRoute", "ngAnimate", "MetadataExtractor", "dndLists"])

.controller("MainCtrl", function ($scope, ContextService, $location) {
	$scope.enterImageJ = function () {
		ContextService.enter("imagej always");
		ContextService.update();
	};
	var self = $scope;



	/* Page related function */
	self.pages = [
		{
			title: "Introduction",
			url: "/"
		}
		, {
			title: "Import images",
			url: "/project-creator"
		}
		, {
			title: "Extract metadata from name",
			url: "/metadata-extractor"
		}
		, {
			title: "Edit data organization",
			url: "/hierarchy-editor"
		}
	];


	self.direction = "";

	$scope.animation = "slide-left";

	self.getCurrentPageIndex = function (url) {
		for (var i = 0; i != self.pages.length; i++) {
			if (self.pages[i].url == url) return i;
		};
		return 0;
	};

	self.currentPage = self.getCurrentPageIndex($location.path());

	self.isBeginning = function () {
		return self.currentPage == 0;
	};
	self.isEnd = function () {
		return self.currentPage == self.pages.length - 1;
	};

	self.goToNextPage = function () {
		self.animation = "slide-right";
		self.currentPage++;
		self.updatePage();
	};



	self.goToPreviousPage = function () {
		self.animation = "slide-left";
		self.currentPage--;
		self.updatePage();
	};

	self.updatePage = function () {
		$location.path(self.pages[self.currentPage].url);
	};

	self.getNextPage = function () {

		if (self.isEnd()) return {
			title: ""
		};



		return self.pages[self.currentPage + 1];
	};

	self.getPreviousPage = function () {
		if (self.isBeginning()) return {
			title: ""
		};
		return self.pages[self.currentPage - 1];
	};

	self.$on("$nextPage", self.goToNextPage);

})

.controller("ImageImporter", function ($scope, ProjectService) {
		$scope.service = ProjectService;

		$scope.data = [];

		$scope.fields = ["File name", "Plane count"];

		$scope.rev = true;

		// Default orderBy
		$scope.orderBy = $scope.fields[0];

		$scope.setOrderBy = function (column) {

			if (column == $scope.orderBy) {
				$scope.rev = $scope.rev == false;
				return;
			}

			$scope.orderBy = column;
		}

		$scope.predicate = function (rows) {
			return rows[$scope.orderBy];
		}

		$scope.refresh = function () {
			console.log("refreshing");
			ProjectService.getFileList().then(function (data) {
				console.log("got data");
				$scope.data = data;
			});
		}

		$scope.loading = false;

		$scope.deleteImage = function (filePath) {
			console.log(filePath);
			ProjectService.deleteImage(filePath);
			$scope.refresh();
		}

		$scope.importImages = function () {
			$scope.loading = true;
			ProjectService
				.importFile()
				.then(function () {
					console.log("the importation is over !!!!");
					$scope.loading = false;
					//$scope.$apply();
				})
				.then(undefined, undefined, $scope.refresh)
				.finally(function () {
					$scope.loading = false;

					$scope.refresh();
				});
		};
	
		$scope.importFolder = function() {
			$scope.loading = true;
			ProjectService
				.importFolder()
				.then($scope.refresh)
				.finally(function() {
					$scope.loading = false;
				})
		};

		$scope.refresh();


	})
	.controller("MetadataExtractor.OverCtrl", function ($scope, ProjectService,$rootScope) {
		$scope.fileList = ["we will", "see"];

		ProjectService.getFileList().then(function (files) {
			$scope.fileList = [];
			files.forEach(function (file) {
				$scope.fileList.push(file['File name'])
			});
		});


		$scope.addRule = function (selectedEntry) {

			var selector = '"File Name" = "/{0}/"'
				.format(selectedEntry.compile()); // format the entry with the regular expression

			var extracted_fields = [];
			selectedEntry.variableRegions.forEach(function (region) {
				extracted_fields.push(region.name);
			});


			var modifier = 'regexp(/{0}/,"File Name")={1}'
				.format(
					selectedEntry.compile(), JSON.stringify(extracted_fields)
				);

			var promise = ProjectService.addRule({
					selector: selector,
					modifier: modifier
				})
				.then(function () {
					console.log("finish");

					$rootScope.$broadcast("$nextPage");

				});


			return promise;

		};

	})
	.controller("HierarchyEditorCtrl", function ($scope, ContextService, ProjectService) {

		$scope.save = function () {
			console.log(JSON.stringify($scope.hierarchy));
			ProjectService.saveHierarchy(JSON.stringify($scope.hierarchy));
			
			$scope.quit();
			
		};

		$scope.quit = function() {
			ContextService.enter("project-manager")
				.leave("webapp")
				.update();
			
		}

		$scope.possibilities = [];
		$scope.hierarchy = [];
		
		$scope.query = "";
	
		// retrieving the hierarchy from the ProjectService
		ProjectService.getPossibleMetadata()
			.then(function (data) {
				console.log(JSON.stringify(data));
				$scope.possibilities = data;
			});



		ProjectService.getProjectHierarchy()
			.then(function (data) {
				$scope.hierarchy = data;
			});




	})
	.factory("ProjectDataService", function () {

		return new ProjectDataService();
	})


.config(['$routeProvider',
  function ($routeProvider) {
		$routeProvider.
		when('/', {
			templateUrl: 'partials/home.html'
				//,controller:"ImageImporter"
		}).
		when('/project-creator', {
			templateUrl: 'partials/image-importer.html',
			controller: 'ImageImporter'
		}).
		when('/metadata-extractor', {
			template: '<div metadata-extractor file-list="fileList" callback="addRule"></div>',
			controller: 'MetadataExtractor.OverCtrl'
		}).
		when('/hierarchy-editor', {
				templateUrl: "partials/hierarchy-editor.html",
				controller: 'HierarchyEditorCtrl'
			})
			.otherwise({
				redirectTo: '/'
			});
  }]);



function ProjectDataService() {
	var self = this;
	self.getImageList = function () {

	};
	self.addRule = function (rule) {

	};

	self.addFile = function () {

	};
	self.addFolder = function () {

	};



};