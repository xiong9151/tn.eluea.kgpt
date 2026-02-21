/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.core.ui.dialog.box;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.regex.Pattern;

import tn.eluea.kgpt.R;
import tn.eluea.kgpt.core.data.ConfigContainer;
import tn.eluea.kgpt.core.ui.dialog.DialogBoxManager;
import tn.eluea.kgpt.core.ui.dialog.DialogType;
import tn.eluea.kgpt.text.parse.ParsePattern;
import tn.eluea.kgpt.text.parse.PatternType;

import android.view.ContextThemeWrapper;
import tn.eluea.kgpt.util.MaterialYouManager;

public class RangeSelectionEditDialogBox extends DialogBox {
    public RangeSelectionEditDialogBox(DialogBoxManager dialogManager, Activity parent,
            Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardPatterns();

        tn.eluea.kgpt.ui.main.FloatingBottomSheet sheet = new tn.eluea.kgpt.ui.main.FloatingBottomSheet(getContext());

        // Use Activity Context directly to ensure Theme and Dynamic Colors are
        // preserved and TextInputLayout works
        Context themedContext = getContext();

        View layout = android.view.LayoutInflater.from(themedContext).inflate(R.layout.dialog_range_selection_edit, null);

        TextInputEditText startSymbolEditText = layout.findViewById(R.id.edit_start_symbol);
        TextInputEditText endSymbolEditText = layout.findViewById(R.id.edit_end_symbol);
        TextInputLayout startInputLayout = layout.findViewById(R.id.input_layout_start_symbol);
        TextInputLayout endInputLayout = layout.findViewById(R.id.input_layout_end_symbol);
        TextView tvTitle = layout.findViewById(R.id.tv_title);
        TextView tvDescription = layout.findViewById(R.id.tv_description);
        TextView tvExample = layout.findViewById(R.id.tv_example);
        com.google.android.material.materialswitch.MaterialSwitch switchEnabled = layout
                .findViewById(R.id.switch_enabled);
        MaterialButton btnCancel = layout.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = layout.findViewById(R.id.btn_save);
        MaterialButton btnReset = layout.findViewById(R.id.btn_reset);
        ImageView headerIcon = layout.findViewById(R.id.iv_header_icon);
        View headerIconContainer = layout.findViewById(R.id.icon_container);

        if (headerIcon != null) {
            tn.eluea.kgpt.core.ui.dialog.utils.DialogUiUtils.applyMaterialYouTints(themedContext, headerIcon,
                    headerIconContainer);
        }

        final int patternIndex = getConfig().focusPatternIndex;
        if (patternIndex < 0 || patternIndex >= getConfig().patterns.size()) {
            // Invalid index, shouldn't happen
            try {
                sheet.dismiss();
            } catch (Exception e) {
            }
            return sheet;
        }
        ParsePattern pattern = getConfig().patterns.get(patternIndex);

        tvTitle.setText(themedContext.getString(R.string.dialog_title_edit_pattern,
                themedContext.getString(pattern.getType().titleResId)));
        
        // Set enabled state
        switchEnabled.setChecked(pattern.isEnabled());
        tn.eluea.kgpt.core.ui.dialog.utils.DialogUiUtils.applySwitchTheme(themedContext, switchEnabled);

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
        
        startSymbolEditText.setText(currentStartSymbol);
        endSymbolEditText.setText(currentEndSymbol);

        // Update example when symbols change
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateExample(tvExample, startSymbolEditText.getText().toString(), 
                             endSymbolEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        
        startSymbolEditText.addTextChangedListener(textWatcher);
        endSymbolEditText.addTextChangedListener(textWatcher);

        btnReset.setOnClickListener(v -> {
            startSymbolEditText.setText("$");
            endSymbolEditText.setText("$");
        });

        btnCancel.setOnClickListener(v -> {
            sheet.dismiss();
            switchToDialog(DialogType.EditPatternList);
        });

        btnSave.setOnClickListener(v -> {
            String startSymbol = startSymbolEditText.getText().toString();
            String endSymbol = endSymbolEditText.getText().toString();
            boolean isEnabled = switchEnabled.isChecked();

            if (startSymbol.isEmpty()) {
                Toast.makeText(getContext(), R.string.msg_start_symbol_empty, Toast.LENGTH_LONG).show();
                return;
            }
            
            if (endSymbol.isEmpty()) {
                Toast.makeText(getContext(), R.string.msg_end_symbol_empty, Toast.LENGTH_LONG).show();
                return;
            }

            // Forbidden symbols
            if (List.of("]", "[", "-", "\n", "\t").contains(startSymbol) || 
                List.of("]", "[", "-", "\n", "\t").contains(endSymbol)) {
                Toast.makeText(getContext(), R.string.msg_symbol_not_allowed, Toast.LENGTH_LONG).show();
                return;
            }

            // Convert symbols to regex
            String newRegex = PatternType.symbolsToRangeRegex(startSymbol, endSymbol);
            if (newRegex == null) {
                Toast.makeText(getContext(), R.string.msg_pattern_create_symbol_failed, Toast.LENGTH_LONG).show();
                return;
            }

            // Validate regex
            try {
                Pattern.compile(newRegex);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.msg_pattern_generated_invalid, Toast.LENGTH_LONG).show();
                return;
            }

            // Check for duplicates
            long similarCount = getConfig().patterns.stream()
                    .filter((c) -> c.getPattern().pattern().equals(newRegex)).count();
            if (similarCount >= 1 && !newRegex.equals(pattern.getPattern().pattern())) {
                Toast.makeText(getContext(), R.string.msg_symbol_already_used, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // Create new pattern with enabled state
            ParsePattern newPattern = new ParsePattern(pattern.getType(), newRegex, pattern.getExtras());
            newPattern.setEnabled(isEnabled);

            getConfig().patterns.remove(patternIndex);
            getConfig().patterns.add(patternIndex, newPattern);

            // Save immediately to ContentProvider and notify listeners
            getConfig().saveToProvider();

            // Send broadcast to notify TextParser of the change
            android.content.Intent broadcastIntent = new android.content.Intent(
                    tn.eluea.kgpt.ui.UiInteractor.ACTION_DIALOG_RESULT);
            broadcastIntent.putExtra(tn.eluea.kgpt.ui.UiInteractor.EXTRA_PATTERN_LIST,
                    tn.eluea.kgpt.text.parse.ParsePattern.encode(getConfig().patterns));
            getContext().sendBroadcast(broadcastIntent);

            String statusMsg = isEnabled ? "enabled" : "disabled";
            Toast.makeText(getContext(), getContext().getString(R.string.msg_trigger_status_format, 
                    startSymbol + "..." + endSymbol, statusMsg),
                    Toast.LENGTH_SHORT).show();

            // Go back to pattern list instead of closing
            sheet.dismiss();
            switchToDialog(DialogType.EditPatternList);
        });

        // Tints
        tn.eluea.kgpt.core.ui.dialog.utils.DialogUiUtils.applyButtonTheme(themedContext, btnSave);

        // Back Button
        View btnBackHeader = layout.findViewById(R.id.btn_back_header);
        if (btnBackHeader != null) {
            btnBackHeader.setOnClickListener(v -> {
                sheet.dismiss();
                switchToDialog(DialogType.EditPatternList);
            });
        }

        sheet.setContentView(layout);
        return sheet;
    }

    private void updateExample(TextView tvExample, String startSymbol, String endSymbol) {
        if (startSymbol.isEmpty() || endSymbol.isEmpty()) {
            tvExample.setText(R.string.hint_example);
            return;
        }
        
        if (startSymbol.equals(endSymbol)) {
            // Same symbol case
            tvExample.setText("\"" + startSymbol + "text to send" + endSymbol + "\" → Sends selected text to AI");
        } else {
            // Different symbols case
            tvExample.setText("\"" + startSymbol + "text to send" + endSymbol + "\" → Sends selected text to AI");
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
}