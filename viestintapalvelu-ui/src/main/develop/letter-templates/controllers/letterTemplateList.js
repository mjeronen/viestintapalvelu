/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

'use strict';

angular.module('letter-templates')
    .controller('LetterTemplateListCtrl', ['$scope', '$modal', '$filter', '$state', 'TemplateService', '_',
        function ($scope, $modal, $filter, $state, TemplateService, _) {

            $scope.radioSelection = 'default';
            $scope.treeTemplateId = {};

            $scope.fetchDefaultTemplates = function () {
                TemplateService.getDefaultTemplates().success(function (data) {
                    $scope.defaultTemplates = data;
                });
            };

            $scope.fetchDefaultTemplates();
            $scope.changeRadioSelection = function() {
                if($scope.radioSelection != 'applicationTarget') {
                    TemplateService.setApplicationTarget("");
                }
            };

            $scope.$on("treeTemplateChanged", function(event, args) {
                $scope.treeTemplate = args;
            });

            TemplateService.getApplicationTargets().then(function (data) {
                var list = _.chain(data)
                    .map(function(item) {
                        var name = TemplateService.getNameFromHaku(item);
                        return {name: name, value: item.oid};
                    })
                    .filter(function(item){
                        return item.name;
                    })
                    .value();
                $scope.letterTypes[1].list = list;
                $scope.applicationTargets = list;
            });

            function updateTarget(applicationTarget) {
                $scope.currentApplicationTarget = applicationTarget;
                TemplateService.getTemplatesByApplicationPeriod(applicationTarget.oid)
                    .success(function (data) {
                        $scope.$parent.applicationTemplates = data.publishedTemplates.concat(data.draftTemplates).concat(data.closedTemplates);
                    }).error(function (e) {

                    });
                TemplateService.setApplicationTarget(applicationTarget);
                TemplateService.setTarget(applicationTarget);
            }

            $scope.getTemplates = function() {
                if ($scope.radioSelection === 'default') {
                    return $scope.defaultTemplates;
                } else if ($scope.radioSelection === 'applicationTarget') {
                    return $scope.applicationTemplates;
                }
                return '';
            };

            $scope.updateTemplatesList = function () {
                if ($scope.radioSelection === 'default') {
                    return $scope.fetchDefaultTemplates();
                } else if ($scope.radioSelection === 'applicationTarget') {
                    return updateTarget($scope.currentApplicationTarget);
                }
            };

            $scope.openCreateDialog = function () {
                $modal.open({
                    size: 'lg',
                    templateUrl: 'views/letter-templates/views/partials/new.html',
                    controller: 'TemplateDialogCtrl'
                });
            };
            
            $scope.letterTypes = [
                {
                    value: 'default',
                    text: 'Oletuskirjepohjat'
                },
                {
                    value: 'applicationTarget',
                    text: 'Hakukohtaiset kirjepohjat',
                    update: updateTarget
                },
                {
                    value: 'organization',
                    text: 'Organisaatioiden kirjeluonnokset'
                }
            ];
            
            $scope.translateTypes = function(types) {
                var translated = [];
                angular.forEach(types, function(value) {
                    this.push($scope.contentTypes[value]);
                }, translated);
                return translated;
            };
            
            $scope.contentTypes = {
                'letter': $filter('i18n')('template.contenttype.letter'),
                'email': $filter('i18n')('template.contenttype.email'),
                'asiointitili': $filter('i18n')('template.contenttype.asiointitili')
            };

            $scope.letterStates = {
                'julkaistu': $filter('i18n')('template.state.published'),
                'luonnos': $filter('i18n')('template.state.draft'),
                'suljettu': $filter('i18n')('template.state.closed')
            };
            
            $scope.languages = {
                'FI': $filter('i18n')('common.language.fi'),
                'SV': $filter('i18n')('common.language.sv'),
                'EN': $filter('i18n')('common.language.en')
            };

            $scope.editTemplate = function(branch) {
                if(branch.isDraft) {
                    $state.go('letter-templates_draft_edit',
                        {'templatename': branch.templateName,
                            'language' : branch.language,
                            'applicationPeriod': branch.applicationPeriod,
                            'orgoid' : branch.organizationOid,
                            'fetchTarget': branch.fetchTarget});
                } else {
                	var templateId = branch.id? branch.id : branch;
                    $state.go('letter-templates_edit', {'templateId': templateId});
                }
            };
            
            $scope.viewTemplate = function(templateId, state) {
                $state.go('letter-templates_view', {'templateId': templateId, 'state': state});
            };

            $scope.noTemplates = function() {
                return ($scope.radioSelection === 'applicationTarget') && $scope.applicationTarget !== "" && ($scope.getTemplates() && $scope.getTemplates().length === 0);
            };

            $scope.removeTemplate = function (templateId, state) {
                var modalInstance = $modal.open({
                    templateUrl: 'views/letter-templates/views/partials/removedialog.html',
                    controller: 'RemoveTemplateModalFromUse',
                    size: 'sm',
                    resolve: {
                        templateId: function () {
                            return templateId
                        },
                        state: function () {
                            return state
                        }
                    }
                });

                modalInstance.result.then(function () {
                    $scope.updateTemplatesList();
                });
            };

            $scope.publishTemplate = function (templateId, state) {
                var modalInstance = $modal.open({
                    templateUrl: 'views/letter-templates/views/partials/publishdialog.html',
                    controller: 'PublishTemplate',
                    size: 'sm',
                    resolve: {
                        templateId: function () {
                            return templateId
                        },
                        state: function () {
                            return state
                        },
                        template: function () {
                            return null
                        }
                    }
                });

                modalInstance.result.then(function () {
                    $scope.updateTemplatesList();
                });
            };

        }
    ]);

angular.module('letter-templates').controller('RemoveTemplateModalFromUse', ['$scope', '$modalInstance', 'TemplateService', 'templateId', 'state', function ($scope, $modalInstance, TemplateService, templateId, state) {
    $scope.templateIdToRemove = templateId;
    $scope.state = state;

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.remove = function () {
        TemplateService.getTemplateByIdAndState($scope.templateIdToRemove, $scope.state).then(function (result) {
            var template = result.data;
            template.state = 'suljettu';
            TemplateService.updateTemplate().put({}, template, function () {
                $modalInstance.close();
            });
        });
    };

}]);

angular.module('letter-templates').controller('PublishTemplate', ['$scope', '$modalInstance', 'TemplateService', 'templateId', 'state', function ($scope, $modalInstance, TemplateService, templateId, state) {
    $scope.templateIdToPublish = templateId;
    $scope.state = state;

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.remove = function () {
        TemplateService.getTemplateByIdAndState($scope.templateIdToPublish, $scope.state).then(function (result) {
            var template = result.data;
            template.state = 'julkaistu';
            TemplateService.updateTemplate().put({}, template, function () {
                $modalInstance.close();
            });
        });
    };

}]);
