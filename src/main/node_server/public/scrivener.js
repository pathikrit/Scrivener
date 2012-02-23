/*global $, google, _, d3*/
"use strict";
var APP_ID = 'adp-rick-test';

var lastStat;
function getStats() {
    var data = new google.visualization.DataTable(), newLastStat = 0;
    data.addColumn('string', 'Key');
    data.addColumn('date', 'Date');
    data.addColumn('number', 'Value');

    $.get('/stat', {'appid':APP_ID}, function (couchData) {
        var rows = [];
        for (var i = 0; i < couchData.rows.length; i++) {
            rows[i] = new Array(3);
            var doc = couchData.rows[i].doc;
            rows[i][0] = doc.key;
            var timestamp = Number(doc.timestamp);
            newLastStat = Math.max(newLastStat, timestamp);
            rows[i][1] = new Date(timestamp);
            rows[i][2] = doc.value;
            //rows[i][3] = timestamp;
        }
        if (newLastStat <= lastStat) {
            return;
        }
        lastStat = newLastStat;
        data.addRows(rows);
        var chart = new google.visualization.MotionChart(document.getElementById('chart_div'));
        chart.draw(data, {width:1000, height:600});
    });

    setTimeout(getStats, 2000);
}

google.load('visualization', '1', {'packages':['motionchart']});
google.setOnLoadCallback(getStats);

$(document).ready(function () {
    $('input:radio').click(function () {
        var select = $('input:radio[name=viz]:checked').val();
        $('#projects').children('div').each(function (div) {
            if ($(this).attr('id') === select) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    });
    $('input:checkbox').click(function (src) {
        $('link').attr('href', $('input:checkbox').is(':checked') ? 'hacker.css' : 'default.css');
    });
    getLogs();
    getStats();
});

var lastLog;
function getLogs() {
    $.get('/log', {'appid':APP_ID}, function (data) {
        var columns = [];
        var newLastLog = 0;
        var docs = _.map(data.rows, function (row) {
            var doc = row.doc;
            delete doc._id;
            delete doc._rev;
            columns = _.union(columns, _.keys(doc));
            newLastLog = Math.max(newLastLog, Number(doc.timestamp));
            return doc;
        });
        if (newLastLog <= lastLog) {
            return;
        }
        docs = _.sortBy(docs, function (doc) {
            return -Number(doc.timestamp);
        });
        $('.logs > thead').empty();
        $('.logs > tbody').empty();
        lastLog = newLastLog;

        d3.select('.logs > thead').append('tr').selectAll('tr').data(columns).enter().append('th').html(
            function (col) {
                var choices = _.uniq(_.pluck(docs, col));
                if (choices.length <= 1) {
                    return col; // nothing to choose from, just return the column name
                }
                if (choices.length < 5) { // a dropdown box is nice for these choices
                    var dropdown = $('<select></select>');
                    dropdown.append('<option></option>');
                    _.each(choices, function (choice) {
                        dropdown.append('<option>' + (choice ? choice : '') + '</option>');
                    });
                    return col + '<br/><select name=\'' + col + '\'>' + dropdown.html() + '</select>';
                }
                return col + "<br/><input type='text' name='" + col + "'/>"; // lots of data, create a text area
            }
        );

        d3.select('.logs > tbody').selectAll('tr').data(docs).enter().append('tr').selectAll('td').data(
            function (doc) {
                var row = new Array(columns.length);
                for (var i = 0; i < columns.length; i++) {
                    row[i] = {};
                    row[i].key = columns[i];
                    row[i].value = doc[columns[i]];
                }
                return row;
            }).enter().append('td').html(
            function (cell) {
                if (cell.key === 'timestamp') {
                    return d3.time.format.iso(new Date(Number(cell.value)));
                } else if (cell.key === 'type') {
                    if (cell.value === 'ERROR') {
                        return '<b style="background-color:red;">' + cell.value + '</b>';
                    } else if (cell.value === 'WARN') {
                        return '<b style="background-color:#b8860b;">' + cell.value + '</b>';
                    }
                }
                return cell.value;
            }).attr('name', function (cell) {
                return cell.key;
            });

        $('.logs select').change(function (src) {
            query();
        });
        $('.logs input').change(function (src) {
            query();
        });
        populateFilters();
    });
    setTimeout(getLogs, 1000);
}

var filters = [];

function query() {
    var sql = '';
    $.each($('.logs select'), function (idx, col) {
        if (col.value && col.value.length) {
            sql += col.name + '=\'' + col.value + '\' and ';
            filters[col.name] = col.value;
        }
    });
    $.each($('.logs input'), function (idx, col) {
        if (col.value && col.value.length) {
            sql += col.name + ' contains \'' + col.value + '\' and ';
            filters[col.name] = col.value;
        }
    });
    $('#query').text(sql = sql.replace(/ and $/, ''));
    filter();
}

function populateFilters() {
    $.each($('.logs select'), function (idx, col) {
        if (filters[col.name]) {
            col.value = filters[col.name];
        }
    });
    $.each($('.logs input'), function (idx, col) {
        if (filters[col.name]) {
            col.value = filters[col.name];
        }
    });
    query();
}

function filter() {
    $('.logs > tbody tr').each(function (idx, row) {

    });
}

