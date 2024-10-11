package biz.karms;

import org.aesh.readline.terminal.impl.ExecPty;
import org.aesh.readline.terminal.impl.Pty;
import org.aesh.terminal.Attributes;
import org.aesh.terminal.utils.ANSI;
import org.fusesource.jansi.internal.Kernel32;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.fusesource.jansi.Ansi.ansi;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private Attributes attributes;
    private Pty pty;
    private boolean windowsAttributesSet;
    private int windowsAttributes;
    private boolean windowsColorSupport = true;


    private void saveTerminalState() {
        try {
            if (windowsAttributes > 0) {
                long hConsole = Kernel32.GetStdHandle(Kernel32.STD_INPUT_HANDLE);
                if (hConsole != (long) Kernel32.INVALID_HANDLE_VALUE) {
                    int[] mode = new int[1];
                    windowsAttributes = Kernel32.GetConsoleMode(hConsole, mode) == 0 ? -1 : mode[0];
                    windowsAttributesSet = true;

                    final int VIRTUAL_TERMINAL_PROCESSING = 0x0004; //enable color on the windows console
                    if (Kernel32.SetConsoleMode(hConsole, windowsAttributes | VIRTUAL_TERMINAL_PROCESSING) != 0) {
                        windowsColorSupport = true;
                    }
                }
            }
        } catch (Throwable t) {
            //this only works with a proper PTY based terminal
            //Aesh creates an input pump thread, that will steal
            //input from the dev mode process
            try {
                Pty pty = ExecPty.current();
                attributes = pty.getAttr();
                Main.this.pty = pty;
            } catch (Exception e) {
                System.out.println("Failed to get a local tty" + e);
            }
        }
    }

    private void restoreTerminalState() {
        if (windowsAttributesSet) {
            long hConsole = Kernel32.GetStdHandle(Kernel32.STD_INPUT_HANDLE);
            if (hConsole != (long) Kernel32.INVALID_HANDLE_VALUE) {
                Kernel32.SetConsoleMode(hConsole, windowsAttributes);
            }
        } else {
            if (attributes == null || pty == null) {
                return;
            }
            Pty finalPty = pty;
            try (finalPty) {
                finalPty.setAttr(attributes);
                int height = finalPty.getSize().getHeight();
                String sb = ANSI.MAIN_BUFFER +
                        ANSI.CURSOR_SHOW +
                        "\u001B[0m" +
                        "\033[" + height + ";0H";
                finalPty.getSlaveOutput().write(sb.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Error restoring console state:" + e);
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        System.out.println("This is normal text.");
        main.saveTerminalState();
        System.out.println(ansi().fgRed().a("This is red text!").reset());
        main.restoreTerminalState();
        System.out.println("This is normal text.");
    }
}
