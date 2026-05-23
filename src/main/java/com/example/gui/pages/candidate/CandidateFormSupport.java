package com.example.gui.pages.candidate;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

final class CandidateFormSupport {

    static final int MIN_BIRTH_YEAR = 1950;
    static final int MAX_BIRTH_YEAR = 2008;

    private CandidateFormSupport() {
    }

    static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(220, 36));
        field.putClientProperty("JTextField.placeholderText", "");
        return field;
    }

    static JTextField createNumericTextField(int limit) {
        JTextField field = createTextField();

        AbstractDocument doc = (AbstractDocument) field.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) {
                    return;
                }
                String filtered = string.replaceAll("[^0-9]", "");
                if (filtered.isEmpty()) {
                    return;
                }

                int currentLength = fb.getDocument().getLength();
                if (currentLength + filtered.length() <= limit) {
                    super.insertString(fb, offset, filtered, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                if (string == null) {
                    return;
                }
                String filtered = string.replaceAll("[^0-9]", "");

                int currentLength = fb.getDocument().getLength();
                int newLength = (currentLength - length) + filtered.length();

                if (newLength <= limit) {
                    super.replace(fb, offset, length, filtered, attrs);
                }
            }
        });

        return field;
    }

    static JFormattedTextField createDateTextField() {
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(dateMask);
            field.setPreferredSize(new Dimension(220, 36));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            return field;
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    static void addField(JPanel panel, GridBagConstraints base, int row, int col, String labelText,
                         JComponent field, String fieldKey, boolean isRequired,
                         String helperText, Map<String, JLabel> errorLabels) {
        String htmlLabel = isRequired ? "<html>" + labelText + " <font color='red'>*</font></html>" : labelText;

        GridBagConstraints labelConstraints = (GridBagConstraints) base.clone();
        labelConstraints.gridx = col;
        labelConstraints.gridy = row;
        labelConstraints.gridwidth = 1;
        labelConstraints.weightx = 0.5;
        labelConstraints.insets = new Insets(8, 8, 4, 8);

        JLabel label = new JLabel(htmlLabel);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, labelConstraints);

        JPanel fieldBox = new JPanel(new BorderLayout(0, 2));
        fieldBox.setOpaque(false);
        fieldBox.add(field, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        if (helperText != null && !helperText.isBlank()) {
            JLabel helper = new JLabel(helperText);
            helper.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            helper.setForeground(new Color(110, 117, 125));
            helper.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            bottomPanel.add(helper);
        }

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        errorLabel.setForeground(Color.RED);
        bottomPanel.add(errorLabel);

        if (errorLabels != null) {
            errorLabels.put(fieldKey, errorLabel);
        }

        fieldBox.add(bottomPanel, BorderLayout.SOUTH);

        GridBagConstraints fieldConstraints = (GridBagConstraints) base.clone();
        fieldConstraints.gridx = col;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(0, 8, 8, 8);

        panel.add(fieldBox, fieldConstraints);
    }

    static void addReadOnlyField(JPanel panel, GridBagConstraints base, int row, int col, String labelText, JComponent field) {
        GridBagConstraints labelConstraints = (GridBagConstraints) base.clone();
        labelConstraints.gridx = col;
        labelConstraints.gridy = row;
        labelConstraints.gridwidth = 1;
        labelConstraints.weightx = 0.5;
        labelConstraints.insets = new Insets(8, 8, 4, 8);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, labelConstraints);

        JPanel fieldBox = new JPanel(new BorderLayout(0, 2));
        fieldBox.setOpaque(false);
        fieldBox.add(field, BorderLayout.CENTER);

        GridBagConstraints fieldConstraints = (GridBagConstraints) base.clone();
        fieldConstraints.gridx = col;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(0, 8, 8, 8);
        fieldConstraints.ipady = 8;

        panel.add(fieldBox, fieldConstraints);
    }

    static String formatDisplay(String value) {
        return (value == null || value.trim().isEmpty()) ? "--" : value;
    }

    static String formatDateForDisplay(String dateValue) {
        if (dateValue == null || dateValue.trim().isEmpty()) {
            return "";
        }

        String trimmed = dateValue.trim();
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("dd/MM/yyyy");
            dbFormat.setLenient(false);
            Date parsed = dbFormat.parse(trimmed);
            return new SimpleDateFormat("dd/MM/yyyy").format(parsed);
        } catch (ParseException ex) {
            return trimmed;
        }
    }

    static String formatDateForStorage(String input) {
        Date parsed = parseDate(input);
        if (parsed == null) {
            return input == null ? "" : input.trim();
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(parsed);
    }

    static Date parseDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            format.setLenient(false);
            return format.parse(input.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    static int getYear(Date date) {
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(date));
    }

    static boolean isValidBirthYearRange(Date date) {
        int year = getYear(date);
        return year >= MIN_BIRTH_YEAR && year <= MAX_BIRTH_YEAR;
    }
}