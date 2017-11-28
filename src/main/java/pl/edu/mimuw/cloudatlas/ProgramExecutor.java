package pl.edu.mimuw.cloudatlas;

import pl.edu.mimuw.cloudatlas.cloudatlasClient.Client;
import pl.edu.mimuw.cloudatlas.cloudatlasServer.Server;
import pl.edu.mimuw.cloudatlas.fetcher.Fetcher;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;

import java.util.function.Consumer;

public class ProgramExecutor {

    enum Programs {
        SERVER(Server::main),
        CLIENT(Client::main),
        FETCHER(Fetcher::main),
        INTERPRETER(Main::main);

        Consumer<String[]> runner;

        Programs(Consumer<String[]> runner) {
            this.runner = runner;
        }
    }

    public static void main(String[] args) {
        Programs.valueOf(args[0]).runner.accept(args);
    }
}
