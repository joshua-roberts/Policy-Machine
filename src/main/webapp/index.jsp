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

<div style="margin: 2% 15%;">
    <div class="card" style="margin-bottom: 5%">
        <form action="Reset" method="post">
            <h5 class="card-header">Reset Configuration</h5>
            <div class="card-body">
                <p class="text-danger">
                    Reset the Policy Machine configuration. This will delete any existing data in the
                    database and memory structure, including the super configuration. To reload the super
                    configuration select the <strong>Load Super Configuration</strong> option below
                </p>
                <button type="submit" class="btn btn-primary">Reset</button>
            </div>
        </form>
    </div>
    <div class="card" style="margin-bottom: 5%">
        <form action="LoadSuper" method="post">
            <h5 class="card-header">Load Super Configuration</h5>
            <div class="card-body">
                <p>Load the super configuration into the Policy Machine. If the configuration already exists, then nothing will happen.</p>
                <button type="submit" class="btn btn-primary">Load</button>
            </div>
        </form>
    </div>
    <div class="card" style="margin-bottom: 5%">
        <h5 class="card-header">Load Configuration</h5>
        <div class="card-footer">
            <form enctype="multipart/form-data" action="load" method="post">
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
            </form>
        </div>
    </div>
    <div class="card" style="margin-bottom: 5%">
        <h5 class="card-header">Save Configuration</h5>
        <div class="card-body">
            <form action="save" method="post">
                <p>Save the current policy configurations in the Policy Machine.</p>
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
            </form>
        </div>
    </div>
</div>
</body>
</html>