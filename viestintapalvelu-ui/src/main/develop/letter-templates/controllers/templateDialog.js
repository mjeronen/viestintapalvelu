'use strict';

angular.module('letter-templates')
    .controller('TemplateDialogCtrl', ['$scope', '$modalInstance', 'TemplateService', '$state', '_', '$filter', 'Global',
        function($scope, $modalInstance, TemplateService, $state, _, $filter, Global) {

            //Default values
            $scope.languageSelection = 'FI';

            TemplateService.getApplicationTargets().then(function(data) {
                $scope.applicationTargets = _.chain(data)
                    .map(function(elem) {
                        var name = TemplateService.getNameFromHaku(elem);
                        return {name: name, value: elem.oid};
                    })
                    .filter(function(elem) {
                        return elem.name;
                    })
                    .value();
            });

            (function initApplicationTarget() {
                var target = TemplateService.getApplicationTarget();
                if(target) {
                    $scope.applicationTarget = {name: TemplateService.getNameFromHaku(target), value: target.oid};
                }

            })();

            TemplateService.getBaseTemplates().success(function(base) {
                base = retrieveNames(base);
                TemplateService.getDefaultTemplates().success(function(def) {
                    var defaultTemplates = processDefaultTemplates(def);
                    Array.prototype.push.apply(defaultTemplates, base);
                    $scope.baseTemplates = defaultTemplates;
                });
            });

            var retrieveNames = function(baseTemplates) {
                return _.map(baseTemplates, function(template) {
                    var target = _.where($scope.applicationTargets, { 'value': template.oid});
                    if(target[0]) {
                        template.name = target[0].name;
                    } else {
                        template.name = "Tuntematon";
                        template.type = "unknown";
                    }
                    return template;
                });
            };

            var processDefaultTemplates = function(templates) {
                return _.map(templates, function(t) {
                        var d = new Date(t.timestamp);
                        return {id: t.id, name: $filter('i18n')('template.common.default.template'), type: t.name, language: t.language, time: d.toLocaleString(Global.getUserLanguage())};
                    });
            };

            $scope.languages = [
                {value: 'FI', text: 'suomi'},
                {value: 'SV', text: 'ruotsi'},
                {value: 'EN', text: 'englanti'}
            ];
            $scope.applicationTypes = [
                {value: 'koekutsukirje', text: 'Koekutsukirje'},
                {value: 'hyvaksymiskirje', text: 'Hyväksymiskirje'},
                {value: 'jalkiohjauskirje', text: 'Jälkiohjauskirje'}];

            $scope.selectBase = function(base) {
                $scope.baseSelected = base ? true : false;
                TemplateService.setBase(base);
            };

            $scope.selectTarget = function(target) {
                TemplateService.setTarget(target);
            };

            $scope.selectType = function(type) {
                TemplateService.setType(type);
            };

            $scope.selectLanguage = function(language) {
                TemplateService.setLanguage(language);
            };

            $scope.confirm = function() {
                $modalInstance.close();
                $state.go('letter-templates.create');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss();
            };
        }
    ]);