/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
/*
 * Copyright (C) 2024-2025 Amr Aldeeb @Eluea
 * 
 * This file is part of KGPT - a fork of KeyboardGPT.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 */
package tn.eluea.kgpt.text.parse.result;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tn.eluea.kgpt.instruction.command.InlineAskCommand;

/**
 * Factory for creating InlineCommandParseResult.
 * Handles any /command that preserves text before it.
 * 
 * Usage: "Some text /command prompt$"
 * Result: Only "prompt" is processed with the command, "Some text " remains
 */
public class InlineCommandParseResultFactory {

    /**
     * Parse text for inline command
     * 
     * @param text              The full text to parse
     * @param triggerSymbol     The trigger symbol (default $)
     * @param availableCommands Set of available command prefixes
     * @return InlineCommandParseResult if matched, null otherwise
     */
    public static InlineCommandParseResult parse(String text, String triggerSymbol, Set<String> availableCommands) {
        if (text == null || text.isEmpty() || availableCommands == null || availableCommands.isEmpty()) {
            return null;
        }

        String escapedSymbol = Pattern.quote(triggerSymbol);

        // Sort commands by length (descending) to match longest triggers first
        // e.g. match "fixer" before "fix"
        List<String> sortedCommands = new java.util.ArrayList<>(availableCommands);
        sortedCommands.sort((s1, s2) -> s2.length() - s1.length());

        StringBuilder cmdPattern = new StringBuilder();
        for (String cmd : sortedCommands) {
            if (cmdPattern.length() > 0)
                cmdPattern.append("|");
            cmdPattern.append(Pattern.quote(cmd));
        }

        // Regex Explanation:
        // (?si) : Dot matches newline, Case insensitive
        // (.*) : Group 1 - Preserved text (Greedy, so it finds the last command match)
        // (?:\\s+/|\\s+|(?<=^)/|(?<=^)) : Separator (Space+slash, Space, Start+slash,
        // Start)
        // (" + cmdPattern + ") : Group 2 - The Command
        // \\s+ : Required whitespace after command
        // (.+) : Group 3 - The Prompt
        // escapedSymbol + "$" : End identifier

        String separatorPattern = "(?:\\s+/|\\s+|(?<=^)/|(?<=^))";

        String regex = "(?si)(.*)" + separatorPattern + "(" + cmdPattern.toString() + ")\\s+(.+)" + escapedSymbol + "$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String preservedText = matcher.group(1);
            String command = matcher.group(2);
            String prompt = matcher.group(3);

            // Skip if this is the InlineAskCommand (handled separately)
            if (InlineAskCommand.isInlineAskCommand(command)) {
                return null;
            }

            // Identify the exact matched command string
            String matchedCommand = command;
            for (String cmd : availableCommands) {
                if (cmd.equalsIgnoreCase(command)) {
                    matchedCommand = cmd;
                    break;
                }
            }

            // Calculate start index.
            // We can check text between group 1 end and group 2 start to find if slash was
            // used.
            int g1End = matcher.end(1);
            int g2Start = matcher.start(2);
            String separator = text.substring(g1End, g2Start);

            int commandStartPos = g2Start;
            if (separator.contains("/")) {
                commandStartPos = text.lastIndexOf("/", g2Start);
                if (commandStartPos < g1End)
                    commandStartPos = g2Start;
            }

            // Return result
            return new InlineCommandParseResult(
                    List.of(matcher.group(0), command, prompt),
                    0,
                    text.length(),
                    matchedCommand,
                    prompt.trim(),
                    preservedText != null ? preservedText : "",
                    commandStartPos);
        }

        return null;
    }
}
