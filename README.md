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
