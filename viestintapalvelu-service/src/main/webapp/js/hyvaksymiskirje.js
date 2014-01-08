angular.module('app').controller('HyvaksymiskirjeController', ['$scope', 'Generator', 'Printer' , function ($scope, Generator, Printer) {
    $scope.hyvaksymiskirjeet = [];
    $scope.removedColumns = {};

    function generateHyvaksymiskirje(count) {
        $scope.hyvaksymiskirjeet = $scope.hyvaksymiskirjeet.concat(Generator.generateObjects(count, function (data) {
            var postoffice = data.any('postoffice');
            var country = data.prioritize(['FINLAND', 'FI'], 0.95).otherwise(data.any('country'))
            var tulokset = generateTulokset(data.any('hakutoive-lukumaara'));
            tulokset[0]['hylkayksenSyy'] = '';
            return {
                "addressLabel": {
                    "firstName": data.any('firstname'),
                    "lastName": data.any('lastname'),
                    "addressline": data.any('street') + ' ' + data.any('housenumber'),
                    "addressline2": "",
                    "addressline3": "",
                    "postalCode": postoffice.substring(0, postoffice.indexOf(' ')),
                    "city": postoffice.substring(postoffice.indexOf(' ') + 1),
                    "region": "",
                    "country": country[0],
                    "countryCode": country[1]
                },
                "tulokset": tulokset,
                "languageCode": data.prioritize('FI', 0.80).prioritize('SE', 0.60).otherwise(data.any('language')),
                "koulu": tulokset[0]['organisaationNimi'],
                "koulutus": tulokset[0]['hakukohteenNimi']
            }
        }))
    }

    function generateTulokset(count) {
        return Generator.generateObjects(count, function (data) {
            return {
                "organisaationNimi": data.any('organisaationNimi'),
                "oppilaitoksenNimi": data.any('oppilaitoksenNimi'),
                "hakukohteenNimi": data.any('hakukohteenNimi'),
                "hyvaksytyt": data.any('hyvaksytyt'),
                "kaikkiHakeneet": data.any('kaikkiHakeneet'),
                "alinHyvaksyttyPistemaara": data.any('alinHyvaksyttyPistemaara'),
                "omatPisteet": data.any('pisteetvajaa'),
                "paasyJaSoveltuvuuskoe": data.any('koe'),
                "valinnanTulos": data.any('valinnanTulos'),
                "selite": data.any('valinnanTulos')
            }
        })
    }

    $scope.updateGenerated = function () {
        if ($scope.count > $scope.hyvaksymiskirjeet.length) {
            generateHyvaksymiskirje($scope.count - $scope.hyvaksymiskirjeet.length)
        } else if ($scope.count < $scope.hyvaksymiskirjeet.length) {
            $scope.hyvaksymiskirjeet.splice($scope.count, $scope.hyvaksymiskirjeet.length)
        }
    }
    $scope.removeHyvaksymiskirje = function (index) {
        $scope.hyvaksymiskirjeet.splice(index, 1)
        $scope.count--;
    }

    $scope.isRemoved = function (index, columnName) {
        var removed = $scope.removedColumns[index];
        return removed && removed[columnName];
    }

    $scope.toggleColumn = function (index, columnName) {
        if (!$scope.isRemoved(index, columnName)) {
            _.forEach($scope.hyvaksymiskirjeet[index].tulokset, function (haku) {
                delete haku[columnName];
            })
            var removed = {}
            removed[index] = {};
            removed[index][columnName] = true;
            _.merge($scope.removedColumns, removed);
        } else {
            _.forEach($scope.hyvaksymiskirjeet[index].tulokset, function (haku) {
                haku[columnName] = "";
            })
            $scope.removedColumns[index][columnName] = false;
        }
    }

    $scope.addHakukohde = function (index) {
        var newHakukohde = {
            "koulu": "",
            "hakutoive": "",
            "ensisijaisetHakijat": "",
            "kaikkiHakijat": "",
            "aloituspaikat": "",
            "varasija": "",
            "alinHyvaksytty": "",
            "omatPisteesi": "",
            "paasyJaSoveltuvuusKoe": "",
            "hylkayksenSyy": ""
        };
        _.forOwn($scope.removedColumns[index], function (num, key) {
            if ($scope.removedColumns[index][key]) {
                delete newHakukohde[key];
            }
        });
        $scope.hyvaksymiskirjeet[index].tulokset.unshift(newHakukohde);
    }

    $scope.removeHakukohde = function (kirjeIndex, hakukohdeIndex) {
        $scope.hyvaksymiskirjeet[kirjeIndex].tulokset.splice(hakukohdeIndex, 1);
    }

    $scope.addHyvaksymiskirje = function () {
        $scope.hyvaksymiskirjeet.unshift({
            "addressLabel": {
                "firstName": $scope.firstName,
                "lastName": $scope.lastName,
                "addressline": $scope.addressline,
                "addressline2": $scope.addressline2,
                "addressline3": $scope.addressline3,
                "postalCode": $scope.postalCode,
                "city": $scope.city,
                "region": $scope.region,
                "country": $scope.country,
                "countryCode": $scope.countryCode
            },
            "tulokset": []
        })
        $scope.addHakukohde(0);
        $scope.count++;
        $scope.firstName = '';
        $scope.lastName = '';
        $scope.addressline = '';
        $scope.addressline2 = '';
        $scope.addressline3 = '';
        $scope.postalCode = '';
        $scope.region = '';
        $scope.city = '';
        $scope.country = '';
        $scope.countryCode = '';
    }
    $scope.generatePDF = function () {
        Printer.hyvaksymiskirjePDF($scope.hyvaksymiskirjeet)
    }
}]);