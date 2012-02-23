<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Scrivener</title>

    <script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"></script>
    <script type="text/javascript" src="http://mbostock.github.com/d3/d3.js"></script>
    <script type="text/javascript" src="http://documentcloud.github.com/underscore/underscore-min.js"></script>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
        var APP_ID = 'adp-rick-test';

        var lastStat;
        google.load('visualization', '1', {'packages':['motionchart']});
        google.setOnLoadCallback(getStats);

        function getStats() {
            var data = new google.visualization.DataTable();
            data.addColumn('string', 'Key');
            data.addColumn('date', 'Date');
            data.addColumn('number', 'Value');
            //data.addColumn('number', 'Timestamp');
            var newLastStat = 0;

            $.get('/stat', {'appid':APP_ID}, function (couchData) {
                var rows = new Array(couchData.rows.length);
                for (var i = 0; i < rows.length; i++) {
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
        }

        $(document).ready(function () {
            $('input:radio').click(function () {
                var select = $('input:radio[name=viz]:checked').val();
                $('#projects').children('div').each(function (div) {
                    if ($(this).attr('id') == select) {
                        $(this).show();
                    } else {
                        $(this).hide();
                    }
                });
            });
            $('input:checkbox').click(function (src) {
                $('link').attr('href', $('input:checkbox').is(':checked') ? 'hacker.css' : 'default.css');
            });
            setInterval(function () {
                getLogs();
                getStats();
            }, 1000);
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
                                row[i] = new Object();
                                row[i]['key'] = columns[i];
                                row[i]['value'] = doc[columns[i]];
                            }
                            return row;
                        }).enter().append('td').html(
                        function (cell) {
                            if (cell.key == 'timestamp') {
                                return d3.time.format.iso(new Date(Number(cell.value)));
                            } else if (cell.key == 'type') {
                                if (cell.value == 'ERROR') {
                                    return '<b style="background-color:red;">' + cell.value + '</b>';
                                } else if (cell.value == 'WARN') {
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
            });

            function query() {
                var sql = '';
                var filter = new Array();
                $.each($('.logs select'), function (idx, col) {
                    if (col.value && col.value.length) {
                        sql += col.name + '=\'' + col.value + '\' and ';
                        filter[col.name] = col.value;
                    }
                });
                $.each($('.logs input'), function (idx, col) {
                    if (col.value && col.value.length) {
                        sql += col.name + ' contains \'' + col.value + '\' and ';
                        filter[col.name] = col.value;
                    }
                });
                $('#query').text(sql = sql.replace(/ and $/, ''));
                execute(filter);
            }

            function execute(filter) {
                alert(filter);
                $('.logs > tbody tr').each(function (idx, row) {

                });
            }
        }
    </script>

    <link href="default.css" rel="stylesheet" type="text/css"/>
    <style type="text/css">@import url("default.css");</style>
    <!-- link href="hacker.css" title="hacker" rel="alternate stylesheet" type="text/css" -->

</head>
<body>

<h2>
    <input type="radio" name="viz" value="logs">logs</input>
    <input type="radio" name="viz" value="stats">stats</input>
    <input type="radio" name="viz" value="terminal">terminal</input>
</h2>

<p><input type="checkbox" value="hacker">Hacker</p>

<div id="projects">
    <div id="logs">
        <p>
            Query:

            <b id='query'>
            </b>
        </p>
        <table class='logs'>
            <thead></thead>
            <tbody></tbody>
        </table>
    </div>

    <div id="stats" style="display: none">
        <div id="chart_div"></div>
    </div>

    <div id="terminal" style="display: none">
        <iframe src="http://192.168.1.137:443" style="width: 90%; height: 300px"></iframe>
    </div>
</div>

</body>
</html>
