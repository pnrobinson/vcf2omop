package org.monarchinitiative.oncembobulator;

import org.apache.log4j.Logger;
import org.monarchinitiative.oncembobulator.command.Command;
import org.monarchinitiative.oncembobulator.io.Commandline;

public class Oncembobulator {
    static Logger logger = Logger.getLogger(Oncembobulator.class.getName());
    private Command command = null;

    public static void main(String args[]) {
        Oncembobulator oncembobulator = new Oncembobulator(args);
    }

    public Oncembobulator(String args[]) {
        Commandline clp = new Commandline(args);
        this.command = clp.getCommand();
        command.execute();

    }


}
