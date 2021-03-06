'use strict';

angular.module('infinitetorrentApp')
    .factory('Tag', function ($resource, DateUtils) {
        return $resource('api/tags/:id', {}, {
            query: { method: 'GET', isArray: true},
            get: {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            update: { method:'PUT' },
            getAll: {
                method: 'GET',
                url: 'api/tags/torrent',
                isArray: true
            }
        });
    });
