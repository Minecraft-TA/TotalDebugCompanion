package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import org.eclipse.lsp4j.SignatureHelp;

import javax.swing.*;
import java.awt.*;

public class SignatureHelpPopup extends BasePopup {

    private final JLabel label = new JLabel();
    {
        this.label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public SignatureHelpPopup() {
        add(this.label, BorderLayout.CENTER);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY.darker()));
        pack();
    }

    public void apply(SignatureHelp signatureHelp) {
        var text = new StringBuilder("<html>");
        var rawMaxLength = 0;
        var lineCount = 0;

        var signatures = signatureHelp.getSignatures();
        for (int i = 0; i < signatures.size(); i++) {
            var signature = signatures.get(i);
            var parameters = signature.getParameters();
            var isActiveSignature = i == signatureHelp.getActiveSignature();
            var lineLength = 0;

            text.append("<p style='border-bottom: %spx solid gray;'>".formatted(i != signatures.size() - 1 ? "1" : "0"));

            for (int j = 0; j < parameters.size(); j++) {
                var isActiveParameter = j == signatureHelp.getActiveParameter();
                var color = isActiveSignature && isActiveParameter ? "rgb(187, 187, 187)" : "rgb(127, 127, 127)";

                var label = parameters.get(j).getLabel().getLeft() + (j != parameters.size() - 1 ? ", " : "");
                lineLength += label.length();
                text.append("<span style='color: %s;'>%s</span>".formatted(color, label));
            }
            text.append("</p>");

            if (lineLength > rawMaxLength) rawMaxLength = lineLength;
            lineCount++;
        }

        text.append("</html>");
        this.label.setText(text.toString());

        var fontMetrics = this.label.getFontMetrics(this.label.getFont());
        this.label.setPreferredSize(new Dimension(fontMetrics.stringWidth("9".repeat(rawMaxLength)) + 4, fontMetrics.getHeight() * lineCount));
        pack();
    }

    @Override
    public void setFont(Font f) {
        this.label.setFont(f);
    }
}
