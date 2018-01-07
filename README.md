README CLOUDATLAS

Before running Cloudatlas a remote object registry should be started by rmiregistry command.
Note: In some cases the rmiregistry should be run from the same directory where the cloudatlas.jar is.
After registry is started, cloudatlas.jar can be executed.

To run Cloudatlas simply execute cloudatlas.jar with one of the following arguments SERVER, CLIENT, FETCHER or INTERPRETER.

In order to run Server, Client and Fetcher a policy file should be specified. Default policy files are included in project (server.policy, client.policy, fetcher.policy)
Example: java -Djava.security.policy={pathToPolicyFile} -jar cloudatlas.jar SERVER

For Client and Interpreter a second program argument can be specified.
For Client a second argument is the Servers ip, if not specified, Client will try to connect to localhost
Example: java -Djava.security.policy={pathToPolicyFile} -jar cloudatlas.jar CLIENT optional_host_ip

For Interpreter a second argument is a path to the file containing attribute names and queries in format specified in the Assignment 1.
A policy file is not required to run Interpreter.
Example: java -jar cloudatlas.jar INTERPRETER pathToFileWithQueries

Available HTTP endpoints
  localhost:8000/client/install
  localhost:8000/client/uninstall
  localhost:8000/client/printZMI
  localhost:8000/client/printAttribute
  localhost:8000/client/execute

For information on HTTP request body content see respectively
  InstallController
  UninstallController
  PrintZMIController
  PrintAttributeController
  QueryExecutorController

In case the above method to run the program is not working
1: run rmiregistry from target/classes directory
2: If intellij idea IDE is present just open the project and run Server, Client, Fetcher or Main configurations
3: If intellij is not present, run java -Djava.security.policy=client.policy Fetcher
                                   java -Djava.security.policy=client.policy pl.edu.mimuw.cloudatlas.cloudatlasClient.Client
                                   java -Djava.rmi.server.hostname=localhost -Djava.security.policy=server.policy pl.edu.mimuw.cloudatlas.cloudatlasServer.Server