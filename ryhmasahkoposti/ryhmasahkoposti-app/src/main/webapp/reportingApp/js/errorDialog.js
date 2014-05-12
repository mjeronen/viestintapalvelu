'use strict';

reportingApp.factory('ErrorDialog', function($modal) {
	return {
		showError : function(msg) {
			return $modal.open({
				templateUrl: '../html/errorDialog.html',
				controller: 'ErrorDialogController',
				size: 'lg',
				resolve: {
					msg: function() {
						return angular.copy(msg.data);
					}
				}
			});
		}
	};
});