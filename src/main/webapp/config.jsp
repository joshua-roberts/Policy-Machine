<html>
<head>
    <title>Home</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="./css/bootstrap.min.css">
    <link rel="stylesheet" href="./css/theme.css">
    <style>

    </style>

    <script type="text/javascript">
        function showForm(name) {
            document.getElementById(name + 'Form').style.display = 'block';

            switch (name) {
                case 'neo4j':
                    hideForm('mysql');
                    break;
                case 'mysql':
                    hideForm('neo4j');
                    break;
            }
        }

        function hideForm(name) {
            document.getElementById(name + 'Form').style.display = 'none';
        }
    </script>
</head>
<body>
<div id="messageDiv" style="display: <%= request.getParameter("display") != null ? request.getParameter("display") : "none" %>">
    <div class="alert alert-dismissible alert-<%= request.getParameter("result") %>">
        <button type="button" class="close" data-dismiss="alert" onclick="document.getElementById('messageDiv').style.display='none'">&times;</button>
        <span id="message"><%= request.getParameter("message") %></span>
    </div>
</div>

<div style="margin: 2% 15%; background-color: white; padding: 10px">
    <h2>Connect to a database.</h2>
    <div class="row" style="margin: 0; padding: 20px">
        <button class="btn btn-success col-lg-3" style="margin-right: 10px" onclick="showForm('neo4j')">Neo4j</button>
        <button class="btn btn-primary col-lg-3" onclick="showForm('mysql')">MySQL</button>
    </div>
    <div id="neo4jForm" class="col-lg-12 card-body" style="display: none; background-color: white;">
        <form action="SetConnection" method="post">
            <input type="hidden" name="database" value="neo4j">
            <fieldset>
                <div class="row">
                    <div class="form-group col-lg-6">
                        <label>Host</label>
                        <input class="form-control" name="host" placeholder="Host" type="text" value="">
                    </div>
                    <div class="form-group col-lg-6">
                        <label>Port</label>
                        <input class="form-control" name="port" placeholder="Port" type="text" value="7687">
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-lg-6">
                        <label>Username</label>
                        <input class="form-control" name="username" placeholder="Username" type="text" value="">
                    </div>
                    <div class="form-group col-lg-6" style="margin: 0">
                        <label>Password</label>
                        <input class="form-control" name="password" placeholder="Password" type="text" value="">
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-lg-3">
                        <button type="submit" class="btn btn-success">Submit</button>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
    <div id="mysqlForm" class="col-lg-12 card-body" style="display: none; background-color: white;">
        <form action="SetConnection" method="post">
            <input type="hidden" name="database" value="mysql">
            <fieldset>
                <div class="row">
                    <div class="form-group col-lg-4">
                        <label>Host</label>
                        <input class="form-control" name="host" placeholder="Host" type="text">
                    </div>
                    <div class="form-group col-lg-4">
                        <label>Port</label>
                        <input class="form-control" name="port" placeholder="Port" type="text">
                    </div>
                    <div class="form-group col-lg-4" style="margin: 0">
                        <label>Database</label>
                        <input class="form-control" name="schema" placeholder="Database" type="text">
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-lg-6">
                        <label>Username</label>
                        <input class="form-control" name="username" placeholder="Username" type="text">
                    </div>
                    <div class="form-group col-lg-6">
                        <label>Password</label>
                        <input class="form-control" name="password" placeholder="Password" type="text">
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-lg-3">
                        <button type="submit" class="btn btn-primary">Submit</button>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
    <h2 style="margin-top: 10px">Configuration</h2>
    <div class="row" style="margin: 0; padding: 20px">
        <div id="resetForm" class="col-lg-4" style="background-color: white; color: #008cba">
            <form action="Reset" method="post">
                <fieldset>
                    <legend>Reset</legend>
                    <p class="text-danger">This will delete any existing data in the PM, including Policy Classes.</p>
                    <div class="row">
                        <div class="form-group col-lg-3">
                            <button type="submit" class="btn btn-primary">Reset</button>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <div id="loadForm" class="col-lg-4" style="background-color: white; color: #008cba">
            <form enctype="multipart/form-data" action="load" method="post">
                <fieldset>
                    <legend>Load</legend>
                    <p>Load a previously saved configuration into the PM.</p>
                    <p class="text-danger">This will delete any existing data in the PM.</p>
                    <div class="row">
                        <div class="form-group col-lg-6">
                            <input type="file" name="configFile" accept=".pm">
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-lg-3">
                            <button type="submit" class="btn btn-primary">Submit</button>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <div id="saveForm" class="col-lg-4" style="background-color: white; color: #008cba">
            <form action="save" method="post">
                <fieldset>
                    <legend>Save</legend>
                    <p class="text-muted">Save the current policy configurations in the Policy Machine.</p>
                    <div class="row" style="margin: 0">
                        <div class="form-group">
                            <input class="form-control" name="configName" placeholder="Configuration Name" type="text">
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-lg-3">
                            <button type="submit" class="btn btn-primary">Submit</button>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
</body>
</html>