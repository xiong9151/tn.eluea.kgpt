/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.ui.main.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.materialswitch.MaterialSwitch;
import tn.eluea.kgpt.text.parse.PatternType;

import java.util.ArrayList;
import java.util.List;

import tn.eluea.kgpt.R;
import tn.eluea.kgpt.SPManager;
import tn.eluea.kgpt.instruction.command.InlineAskCommand;
import tn.eluea.kgpt.text.parse.ParsePattern;
import tn.eluea.kgpt.ui.main.BottomSheetHelper;
import tn.eluea.kgpt.ui.main.FloatingBottomSheet;
import tn.eluea.kgpt.ui.main.adapters.PatternsAdapter;

public class InvocationPatternsFragment extends Fragment {

    private RecyclerView rvPatterns;
    private PatternsAdapter patternsAdapter;
    private List<ParsePattern> patterns = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invocation_patterns, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPatterns = view.findViewById(R.id.rv_patterns);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        rvPatterns.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadPatterns();

        patternsAdapter = new PatternsAdapter(patterns, new PatternsAdapter.OnPatternClickListener() {
            @Override
            public void onPatternClick(ParsePattern pattern, int position) {
                showEditPatternDialog(pattern, position);
            }

            @Override
            public void onPatternDelete(ParsePattern pattern, int position) {
                showResetPatternConfirmation(pattern, position);
            }
        });
        rvPatterns.setAdapter(patternsAdapter);
    }

    public void loadPatterns() {
        if (SPManager.isReady()) {
            patterns = new ArrayList<>(SPManager.getInstance().getParsePatterns());
            if (patternsAdapter != null) {
                patternsAdapter.updatePatterns(patterns);
            }
        }
    }

    // Pattern Info / How to Use action
    public void showHowToUse() {
        // This can be triggered from FAB or Info button
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_ai_usage, null);
        BottomSheetHelper.applyTheme(requireContext(), sheetView);

        String aiTriggerSymbol = "$";
        String italicSymbol = "|";
        String boldSymbol = "@";
        String crossoutSymbol = "~";
        String underlineSymbol = "_";

        for (ParsePattern pattern : patterns) {
            String symbol = PatternType.regexToSymbol(pattern.getPattern().pattern());
            if (symbol == null)
                symbol = pattern.getType().defaultSymbol;

            switch (pattern.getType()) {
                case CommandAI:
                    aiTriggerSymbol = symbol;
                    break;
                case FormatItalic:
                    italicSymbol = symbol;
                    break;
                case FormatBold:
                    boldSymbol = symbol;
                    break;
                case FormatCrossout:
                    crossoutSymbol = symbol;
                    break;
                case FormatUnderline:
                    underlineSymbol = symbol;
                    break;
            }
        }

        // We need commands to show example command name, but this fragment doesn't have
        // commands list directly?
        // Let's just grab from SPManager

        String commandName = "translate";
        if (SPManager.isReady() && !SPManager.getInstance().getGenerativeAICommands().isEmpty()) {
            commandName = SPManager.getInstance().getGenerativeAICommands().get(0).getCommandPrefix();
        }

        String askPrefix = InlineAskCommand.getPrefix(); // Should be up to date via static or SPManager

        TextView tvAiExample = sheetView.findViewById(R.id.tv_ai_trigger_example);
        TextView tvAskExample = sheetView.findViewById(R.id.tv_ask_example);
        TextView tvCommandExample = sheetView.findViewById(R.id.tv_command_example);
        TextView tvFormatExample = sheetView.findViewById(R.id.tv_format_example);

        tvAiExample.setText("What is AI?" + aiTriggerSymbol);
        tvAskExample.setText("Note. /" + askPrefix + " time?" + aiTriggerSymbol + " -> keeps Note.");
        tvCommandExample.setText("Hello /" + commandName + aiTriggerSymbol);
        tvFormatExample.setText(
                "text" + italicSymbol + " text" + boldSymbol + " text" + crossoutSymbol + " text" + underlineSymbol);

        FloatingBottomSheet dialog = new FloatingBottomSheet(requireContext());
        dialog.setContentView(sheetView);

        MaterialButton btnClose = sheetView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditPatternDialog(ParsePattern pattern, int position) {
        // For RangeSelection pattern type, use the specialized dialog
        if (pattern.getType() == PatternType.RangeSelection) {
            showRangeSelectionEditDialog(pattern, position);
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_pattern_symbol, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvPatternType = dialogView.findViewById(R.id.tv_pattern_type);
        TextView tvDescription = dialogView.findViewById(R.id.tv_description);
        TextInputEditText etSymbol = dialogView.findViewById(R.id.et_symbol);
        TextView tvExample = dialogView.findViewById(R.id.tv_example);
        com.google.android.material.materialswitch.MaterialSwitch switchEnabled = dialogView
                .findViewById(R.id.switch_enabled);
        MaterialButton btnReset = dialogView.findViewById(R.id.btn_reset);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        tvPatternType.setText(getString(pattern.getType().titleResId));
        tvDescription.setText(
                getString(pattern.getType().exampleResId, PatternType.regexToSymbol(pattern.getPattern().pattern())));

        switchEnabled.setChecked(pattern.isEnabled());

        String currentSymbol = PatternType.regexToSymbol(pattern.getPattern().pattern());
        if (currentSymbol == null || currentSymbol.isEmpty()) {
            currentSymbol = pattern.getType().defaultSymbol;
        }
        etSymbol.setText(currentSymbol);

        updateExample(tvExample, currentSymbol, pattern.getType());

        etSymbol.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateExample(tvExample, s.toString(), pattern.getType());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        btnReset.setOnClickListener(v -> etSymbol.setText(pattern.getType().defaultSymbol));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String symbol = etSymbol.getText().toString();
            boolean isEnabled = switchEnabled.isChecked();

            if (symbol.isEmpty()) {
                Toast.makeText(requireContext(), R.string.msg_symbol_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (List.of("]", "[", "-", " ", "\n", "\t").contains(symbol)) {
                Toast.makeText(requireContext(), R.string.msg_symbol_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            }

            String newRegex = PatternType.symbolToRegex(symbol, pattern.getType().groupCount);
            if (newRegex == null) {
                Toast.makeText(requireContext(), R.string.msg_pattern_create_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                java.util.regex.Pattern.compile(newRegex);
            } catch (Exception e) {
                Toast.makeText(requireContext(), R.string.msg_pattern_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            ParsePattern newPattern = new ParsePattern(pattern.getType(), newRegex, pattern.getExtras());
            newPattern.setEnabled(isEnabled);
            patterns.set(position, newPattern);
            savePatterns();
            patternsAdapter.updatePatterns(patterns);
            dialog.dismiss();

            String statusMsg = isEnabled ? "enabled" : "disabled";
            Toast.makeText(requireContext(), getString(R.string.msg_trigger_status_format, symbol, statusMsg),
                    Toast.LENGTH_SHORT).show();
        });

        BottomSheetHelper.applyBlur(dialog);
        dialog.show();
    }

    private void updateExample(TextView tvExample, String symbol, PatternType type) {
        if (symbol == null || symbol.isEmpty())
            symbol = type.defaultSymbol;
        String example;
        switch (type) {
            case Settings:
                example = "Example: \"" + symbol + "\" -> Opens settings";
                break;
            case CommandAI:
                example = "Example: \"Hello?" + symbol + "\" -> AI responds";
                break;
            case CommandCustom:
                example = "Example: \"Hello" + symbol + "translate\" -> Translates";
                break;
            case FormatItalic:
                example = "Example: \"text" + symbol + "\" -> italic";
                break;
            default:
                example = "Type text, add \"" + symbol + "\" at end";
        }
        tvExample.setText(example);
    }

    private void showResetPatternConfirmation(ParsePattern pattern, int position) {
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_delete_confirm, null);
        BottomSheetHelper.applyTheme(requireContext(), sheetView);
        FloatingBottomSheet dialog = new FloatingBottomSheet(requireContext());
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tv_delete_title);
        TextView tvMessage = sheetView.findViewById(R.id.tv_delete_message);
        MaterialButton btnCancel = sheetView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = sheetView.findViewById(R.id.btn_delete);

        tvTitle.setText(R.string.btn_reset_pattern);
        tvMessage.setText(getString(R.string.msg_reset_pattern_confirm, getString(pattern.getType().titleResId),
                pattern.getType().defaultSymbol));
        btnDelete.setText("Reset");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            patterns.set(position, new ParsePattern(pattern.getType(), pattern.getType().defaultPattern));
            savePatterns();
            patternsAdapter.updatePatterns(patterns);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.msg_pattern_reset, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void savePatterns() {
        if (SPManager.isReady()) {
            SPManager.getInstance().setParsePatterns(patterns);
            syncConfig();
        }
    }

    private void showRangeSelectionEditDialog(ParsePattern pattern, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_range_selection_edit, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvPatternType = dialogView.findViewById(R.id.tv_title);
        TextView tvDescription = dialogView.findViewById(R.id.tv_description);
        TextInputEditText etStartSymbol = dialogView.findViewById(R.id.edit_start_symbol);
        TextInputEditText etEndSymbol = dialogView.findViewById(R.id.edit_end_symbol);
        TextView tvExample = dialogView.findViewById(R.id.tv_example);
        com.google.android.material.materialswitch.MaterialSwitch switchEnabled = dialogView
                .findViewById(R.id.switch_enabled);
        MaterialButton btnReset = dialogView.findViewById(R.id.btn_reset);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        tvPatternType.setText(getString(pattern.getType().titleResId));
        tvDescription.setText(getString(R.string.hint_range_selection_description));

        switchEnabled.setChecked(pattern.isEnabled());

        // Extract current symbols from regex
        String[] symbols = extractSymbolsFromRegex(pattern.getPattern().pattern());
        String currentStartSymbol = symbols[0];
        String currentEndSymbol = symbols[1];
        
        if (currentStartSymbol == null || currentStartSymbol.isEmpty()) {
            currentStartSymbol = "$";
        }
        if (currentEndSymbol == null || currentEndSymbol.isEmpty()) {
            currentEndSymbol = "$";
        }
        
        etStartSymbol.setText(currentStartSymbol);
        etEndSymbol.setText(currentEndSymbol);

        // Update example when symbols change
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRangeSelectionExample(tvExample, etStartSymbol.getText().toString(), 
                             etEndSymbol.getText().toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        };
        
        etStartSymbol.addTextChangedListener(textWatcher);
        etEndSymbol.addTextChangedListener(textWatcher);

        btnReset.setOnClickListener(v -> {
            etStartSymbol.setText("$");
            etEndSymbol.setText("$");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String startSymbol = etStartSymbol.getText().toString();
            String endSymbol = etEndSymbol.getText().toString();
            boolean isEnabled = switchEnabled.isChecked();

            if (startSymbol.isEmpty()) {
                Toast.makeText(requireContext(), R.string.msg_start_symbol_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (endSymbol.isEmpty()) {
                Toast.makeText(requireContext(), R.string.msg_end_symbol_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            // Forbidden symbols
            if (List.of("]", "[", "-", "\n", "\t").contains(startSymbol) || 
                List.of("]", "[", "-", "\n", "\t").contains(endSymbol)) {
                Toast.makeText(requireContext(), R.string.msg_symbol_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert symbols to regex
            String newRegex = PatternType.symbolsToRangeRegex(startSymbol, endSymbol);
            if (newRegex == null) {
                Toast.makeText(requireContext(), R.string.msg_pattern_create_symbol_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate regex
            try {
                java.util.regex.Pattern.compile(newRegex);
            } catch (Exception e) {
                Toast.makeText(requireContext(), R.string.msg_pattern_generated_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for duplicates
            long similarCount = patterns.stream()
                    .filter((c) -> c.getPattern().pattern().equals(newRegex)).count();
            if (similarCount >= 1 && !newRegex.equals(pattern.getPattern().pattern())) {
                Toast.makeText(requireContext(), R.string.msg_symbol_already_used, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            // Create new pattern with enabled state
            ParsePattern newPattern = new ParsePattern(pattern.getType(), newRegex, pattern.getExtras());
            newPattern.setEnabled(isEnabled);

            patterns.set(position, newPattern);
            savePatterns();
            patternsAdapter.updatePatterns(patterns);
            dialog.dismiss();

            String statusMsg = isEnabled ? "enabled" : "disabled";
            Toast.makeText(requireContext(), getString(R.string.msg_trigger_status_format, 
                    startSymbol + "..." + endSymbol, statusMsg),
                    Toast.LENGTH_SHORT).show();
        });

        BottomSheetHelper.applyBlur(dialog);
        dialog.show();
    }
    
    private void updateRangeSelectionExample(TextView tvExample, String startSymbol, String endSymbol) {
        if (startSymbol.isEmpty() || endSymbol.isEmpty()) {
            tvExample.setText(R.string.hint_example);
            return;
        }
        
        if (startSymbol.equals(endSymbol)) {
            // Same symbol case
            tvExample.setText("\"" + startSymbol + "要发送的文本" + endSymbol + "\" → 发送选中的文本给AI");
        } else {
            // Different symbols case
            tvExample.setText("\"" + startSymbol + "要发送的文本" + endSymbol + "\" → 发送选中的文本给AI");
        }
    }
    
    private String[] extractSymbolsFromRegex(String regex) {
        String[] result = new String[]{"$", "$"};
        
        if (regex == null || regex.isEmpty()) {
            return result;
        }
        
        // Remove the ending $
        if (regex.endsWith("$")) {
            regex = regex.substring(0, regex.length() - 1);
        }
        
        // Find the capturing group pattern (.+?)
        int groupStart = regex.indexOf("(.+?)");
        if (groupStart == -1) {
            return result;
        }
        
        String startSymbol = regex.substring(0, groupStart);
        String endSymbol = regex.substring(groupStart + 5); // 5 is length of "(.+?)"
        
        // Unescape the symbols
        startSymbol = unescapeRegex(startSymbol);
        endSymbol = unescapeRegex(endSymbol);
        
        result[0] = startSymbol;
        result[1] = endSymbol;
        
        return result;
    }
    
    private String unescapeRegex(String escaped) {
        if (escaped == null || escaped.isEmpty()) {
            return escaped;
        }
        
        // Replace escaped special characters
        return escaped.replace("\\$", "$")
                     .replace("\\.", ".")
                     .replace("\\^", "^")
                     .replace("\\*", "*")
                     .replace("\\+", "+")
                     .replace("\\?", "?")
                     .replace("\\(", "(")
                     .replace("\\)", ")")
                     .replace("\\[", "[")
                     .replace("\\]", "]")
                     .replace("\\{", "{")
                     .replace("\\}", "}")
                     .replace("\\|", "|")
                     .replace("\\\\", "\\");
    }

    private void syncConfig() {
        Intent intent = new Intent("tn.eluea.kgpt.DIALOG_RESULT");
        String commandsRaw = SPManager.getInstance().getGenerativeAICommandsRaw();
        intent.putExtra("tn.eluea.kgpt.command.LIST", commandsRaw);
        String patternsRaw = SPManager.getInstance().getParsePatternsRaw();
        intent.putExtra("tn.eluea.kgpt.pattern.LIST", patternsRaw);
        requireContext().sendBroadcast(intent);
    }
}
