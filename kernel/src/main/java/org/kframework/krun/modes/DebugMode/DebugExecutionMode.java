// Copyright (c) 2015 K Team. All Rights Reserved.
package org.kframework.krun.modes.DebugMode;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.MultiCompletor;
import jline.SimpleCompletor;
import org.kframework.Rewriter;
import org.kframework.attributes.Source;
import org.kframework.backend.unparser.OutputModes;
import org.kframework.debugger.DebuggerState;
import org.kframework.debugger.KDebug;
import org.kframework.debugger.KoreKDebug;
import org.kframework.definition.Rule;
import org.kframework.kompile.CompiledDefinition;
import org.kframework.kore.K;
import org.kframework.krun.KRunDebuggerOptions;
import org.kframework.krun.KRunOptions;
import org.kframework.krun.modes.ExecutionMode;
import org.kframework.utils.debugparser.DebuggerCommandParser;
import org.kframework.utils.errorsystem.KEMException;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import static org.kframework.krun.KRun.*;
import static org.kframework.krun.modes.DebugMode.Commands.*;

/**
 * Created by Manasvi on 6/10/15.
 * <p>
 * Execution mode class for the Kore based Debugger
 */
public class DebugExecutionMode implements ExecutionMode<Void> {

    private final KRunOptions kRunOptions;
    private final KExceptionManager kem;
    private final FileUtil files;
    private int checkpointInterval;

    private static Object command(JCommander jc) {
        return jc.getCommands().get(jc.getParsedCommand()).getObjects().get(0);
    }

    @Inject
    public DebugExecutionMode(KRunOptions kRunOptions, KExceptionManager kem, FileUtil files, @Named("checkpointIntervalValue") Integer checkpointInterval) {
        this.kRunOptions = kRunOptions;
        this.kem = kem;
        this.files = files;
        this.checkpointInterval = checkpointInterval;
    }

//
//    @Override
//    public Void execute(K k, Rewriter rewriter, CompiledDefinition compiledDef) {
//        /* Development Purposes Only, will go away in production */
//        KDebug debugger = new KoreKDebug(k, rewriter, checkpointInterval);
//        ConsoleReader reader;
//        try {
//            reader = new ConsoleReader();
//        } catch (IOException e) {
//            throw KEMException.internalError("IO error detected interacting with console", e);
//        }
//        reader.setBellEnabled(false);
//
//        List<Completor> argCompletor = new LinkedList<Completor>();
//        argCompletor.add(new SimpleCompletor(new String[]{"help",
//                "exit", "step", "jump-to", "back-step", "resume", "run"}));
//        argCompletor.add(new FileNameCompletor());
//        List<Completor> completors = new LinkedList<Completor>();
//        completors.add(new ArgumentCompletor(argCompletor));
//        reader.addCompletor(new MultiCompletor(completors));
//        while (true) {
//            System.out.println();
//            String input;
//            try {
//                input = reader.readLine("Command > ");
//            } catch (IOException e) {
//                throw KEMException.internalError("IO error detected interacting with console", e);
//            }
//            if (input == null) {
//                // probably pressed ^D
//                System.out.println();
//                return null;
//            }
//            if (input.equals("")) {
//                continue;
//            }
//
//            KRunDebuggerOptions options = new KRunDebuggerOptions();
//            JCommander jc = new JCommander(options);
//            jc.addCommand(options.help);
//            jc.addCommand(options.step);
//            jc.addCommand(options.exit);
//            jc.addCommand(options.setCheckpoint);
//            jc.addCommand(options.backStep);
//            jc.addCommand(options.jumpTo);
//            jc.addCommand(options.search);
//            jc.addCommand(options.resume);
//            try {
//                jc.parse(input.split("\\s+"));
//
//                if (jc.getParsedCommand().equals("help")) {
//                    if (options.help.command == null || options.help.command.size() == 0) {
//                        jc.usage();
//                    } else {
//                        for (String command : options.help.command) {
//                            if (jc.getCommands().containsKey(command)) {
//                                jc.usage(command);
//                            }
//                        }
//                    }
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandExit) {
//                    return null;
//
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandStep) {
//                    DebuggerState result = debugger.step(options.step.numSteps);
//                    K finalK = result.getCurrentK();
//                    if (finalK instanceof K)
//                        prettyPrint(compiledDef, OutputModes.PRETTY, s -> System.out.println(s), finalK);
//                    else
//                        System.out.printf("Invalid Operation");
//
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandSetCheckpoint) {
//
//                    debugger.setCheckpointInterval(options.setCheckpoint.checkpointInterval);
//
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandJumpTo) {
//                    DebuggerState result = debugger.jumpTo(options.jumpTo.stateNum);
//                    if (result != null) {
//                        K finalK = result.getCurrentK();
//                        prettyPrint(compiledDef, OutputModes.PRETTY, s -> System.out.println(s), finalK);
//                    } else
//                        System.out.println("Invalid Operation");
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandBackStep) {
//                    DebuggerState result = debugger.backStep(options.backStep.backSteps);
//                    if (result != null) {
//                        K finalK = result.getCurrentK();
//                        prettyPrint(compiledDef, OutputModes.PRETTY, s -> System.out.println(s), finalK);
//                    } else
//                        System.out.println("Invalid Operation");
//
//
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandSearch) {
//                    Rule parsedPattern = pattern(files, kem, options.search.patternStr, kRunOptions, compiledDef, Source.apply("<Console>"));
//                    //List<Map<KVariable, K>> results = debugger.search()
//
//
//                } else if (command(jc) instanceof KRunDebuggerOptions.CommandResume) {
//                    DebuggerState result = debugger.resume();
//                    K finalK = result.getCurrentK();
//                    if (finalK instanceof K)
//                        prettyPrint(compiledDef, OutputModes.PRETTY, s -> System.out.println(s), finalK);
//                    else
//                        System.out.printf("Invalid Operation");
//                } else {
//                    assert false : "Unexpected krun debugger command " + jc.getParsedCommand();
//                }
//            } catch (ParameterException e) {
//                System.err.println(e.getMessage());
//                jc.usage();
//            } catch (IllegalArgumentException | IllegalStateException e) {
//                System.err.println(e.getMessage());
//            }
//        }
//    }
    private ConsoleReader getConsoleReader() {
        ConsoleReader reader;
        try {
            reader = new ConsoleReader();
        } catch (IOException e) {
            throw KEMException.internalError("IO error detected interacting with console", e);
        }
        reader.setBellEnabled(false);

        List<Completor> argCompletor = new LinkedList<Completor>();
        argCompletor.add(new SimpleCompletor(new String[]{"help",
                "exit", "step", "jump-to", "back-step", "resume", "run"}));
        argCompletor.add(new FileNameCompletor());
        List<Completor> completors = new LinkedList<Completor>();
        completors.add(new ArgumentCompletor(argCompletor));
        reader.addCompletor(new MultiCompletor(completors));
        return reader;
    }

    private static Runnable parseCommand(String command, KDebug session, CompiledDefinition compiledDef) throws Exception
    {
        DebuggerCommandParser parser = new DebuggerCommandParser(new StringReader(command));
        return parser.command(session, compiledDef);
    }

    @Override
    public Void execute(K k, Rewriter rewriter, CompiledDefinition compiledDefinition) {
        KDebug debugger = new KoreKDebug(k, rewriter, checkpointInterval);
        ConsoleReader reader = getConsoleReader();

    }
}
