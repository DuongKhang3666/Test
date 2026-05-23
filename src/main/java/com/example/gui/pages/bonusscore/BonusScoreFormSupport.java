package com.example.gui.pages.bonusscore;

import com.example.gui.components.ComboBox;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.Map;

public final class BonusScoreFormSupport {

	static final String[] MAJOR_CODES = {
			"IT01", "IT02", "IT03",
			"BA02", "BA03",
			"MD01", "MD02",
			"KT01", "KT03",
			"CN11", "CN12"
	};

	static final String[] SUBJECT_GROUPS = {
			"A00", "A01", "A02",
			"B00", "B01", "B03", "B08",
			"D01", "D05", "D07"
	};

	static final String[] METHODS = {
			"Xét học bạ",
			"Xét điểm thi",
			"IELTS",
			"TOEFL",
			"TOEIC",
			"SAT",
			"HSGS",
			"Không áp dụng"
	};

	private BonusScoreFormSupport() {
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

	static JTextField createDecimalTextField() {
		JTextField field = createTextField();

		AbstractDocument doc = (AbstractDocument) field.getDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			private String sanitize(String text) {
				if (text == null) {
					return "";
				}
				return text.replace(',', '.').replaceAll("[^0-9.]", "");
			}

			private boolean isValid(String value) {
				if (value == null || value.isEmpty()) {
					return true;
				}
				if (!value.matches("^\\d*(\\.\\d{0,2})?$")) {
					return false;
				}
				return value.chars().filter(ch -> ch == '.').count() <= 1;
			}

			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				if (string == null) {
					return;
				}
				String filtered = sanitize(string);
				if (filtered.isEmpty()) {
					return;
				}

				String current = fb.getDocument().getText(0, fb.getDocument().getLength());
				String next = current.substring(0, offset) + filtered + current.substring(offset);
				if (isValid(next)) {
					super.insertString(fb, offset, filtered, attr);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
				String filtered = sanitize(string);
				String current = fb.getDocument().getText(0, fb.getDocument().getLength());
				String next = current.substring(0, offset) + filtered + current.substring(offset + length);
				if (isValid(next)) {
					super.replace(fb, offset, length, filtered, attrs);
				}
			}
		});

		return field;
	}

	static JTextField createBoundedDecimalTextField(int maxInteger, int maxFractionDigits, double maxValue) {
		JTextField field = createTextField();

		AbstractDocument doc = (AbstractDocument) field.getDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			private String sanitize(String text) {
				if (text == null) {
					return "";
				}
				// Only allow '.' as decimal separator; do not auto-convert commas.
				return text.replaceAll("[^0-9.]", "");
			}

			private boolean isValid(String value) {
				if (value == null || value.isEmpty()) {
					return true;
				}

				// Require leading digit before decimal point (no ".5" style inputs)
				if (value.startsWith(".")) {
					return false;
				}

				String normalized = value;
				long dots = normalized.chars().filter(ch -> ch == '.').count();
				if (dots > 1) {
					return false;
				}

				String[] parts = normalized.split("\\.", -1);
				String intPart = parts.length > 0 ? parts[0] : "";
				String fracPart = parts.length > 1 ? parts[1] : "";

				// Integer part must be digits only and within maxInteger
				if (!intPart.isEmpty() && !intPart.matches("^\\d+$")) {
					return false;
				}
				int maxIntLen = String.valueOf(Math.max(0, maxInteger)).length();
				if (intPart.length() > maxIntLen) {
					return false;
				}
				int intValue = 0;
				if (!intPart.isEmpty()) {
					try {
						intValue = Integer.parseInt(intPart);
					} catch (NumberFormatException ex) {
						return false;
					}
				}
				if (intValue > maxInteger) {
					return false;
				}

				// Fraction part must be digits only and limited in length
				if (!fracPart.isEmpty() && !fracPart.matches("^\\d+$")) {
					return false;
				}
				if (fracPart.length() > maxFractionDigits) {
					return false;
				}

				// If maxValue is an integer and integer part already at that ceiling,
				// then fractional digits must be all zeros
				double floor = Math.floor(maxValue);
				boolean maxValueIsInteger = Math.abs(maxValue - floor) < 1e-9;
				if (maxValueIsInteger && intValue == (int) floor && !fracPart.isEmpty()) {
					for (int i = 0; i < fracPart.length(); i++) {
						if (fracPart.charAt(i) != '0') {
							return false;
						}
					}
				}

				// Enforce maxValue when parseable (avoid blocking intermediate states like "2.")
				if (!normalized.equals(".") && !normalized.endsWith(".")) {
					try {
						double v = Double.parseDouble(normalized);
						return v <= maxValue + 1e-9;
					} catch (NumberFormatException ex) {
						return false;
					}
				}

				return true;
			}

			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				if (string == null) {
					return;
				}
				String filtered = sanitize(string);
				if (filtered.isEmpty()) {
					return;
				}

				String current = fb.getDocument().getText(0, fb.getDocument().getLength());
				String next = current.substring(0, offset) + filtered + current.substring(offset);
				if (isValid(next)) {
					super.insertString(fb, offset, filtered, attr);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
				String filtered = sanitize(string);
				String current = fb.getDocument().getText(0, fb.getDocument().getLength());
				String next = current.substring(0, offset) + filtered + current.substring(offset + length);
				if (isValid(next)) {
					super.replace(fb, offset, length, filtered, attrs);
				}
			}
		});

		return field;
	}

	static ComboBox<String> createMajorCombo() {
		return new ComboBox<>(new String[]{"-- Chọn mã ngành --", "IT01", "IT02", "IT03", "BA02", "BA03", "MD01", "MD02", "KT01", "KT03", "CN11", "CN12"});
	}

	static ComboBox<String> createSubjectGroupCombo() {
		return new ComboBox<>(new String[]{"-- Chọn tổ hợp --", "A00", "A01", "A02", "B00", "B01", "B03", "B08", "D01", "D05", "D07"});
	}

	static ComboBox<String> createMethodCombo() {
		return new ComboBox<>(new String[]{"-- Chọn phương thức --", "Xét học bạ", "Xét điểm thi", "IELTS", "TOEFL", "TOEIC", "SAT", "HSGS", "Không áp dụng"});
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

	public static void addReadOnlyField(JPanel panel, GridBagConstraints base, int row, int col, String labelText, JComponent field) {
		GridBagConstraints labelConstraints = (GridBagConstraints) base.clone();
		labelConstraints.gridx = col;
		labelConstraints.gridy = row;
		labelConstraints.gridwidth = 1;
		labelConstraints.weightx = Math.max(0.0, base.weightx);
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
		fieldConstraints.weightx = Math.max(0.0, base.weightx);
		fieldConstraints.insets = new Insets(0, 8, 8, 8);
		fieldConstraints.ipady = 8;

		panel.add(fieldBox, fieldConstraints);
	}

	static String formatDisplay(String value) {
		return (value == null || value.trim().isEmpty()) ? "--" : value;
	}

	static String formatScore(Double value) {
		return value == null ? "0.00" : String.format(java.util.Locale.ROOT, "%.2f", value);
	}

	static Double parseScore(String value) {
		if (value == null || value.trim().isEmpty()) {
			return 0.0;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
