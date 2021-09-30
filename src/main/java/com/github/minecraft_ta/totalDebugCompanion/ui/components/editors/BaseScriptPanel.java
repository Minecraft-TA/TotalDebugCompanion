package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.model.BaseScriptView;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.event.DocumentEvent;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BaseScriptPanel extends ScriptPanel {

    private static final Predicate<String> VALIDATOR;
    static {
        VALIDATOR = Stream.of(
                "(?s)(\\n|;)abstract\\s+class\\s+BaseScript(.*?)\\{(.*?)public\\s+abstract\\s+void\\s+run\\(\\s*\\)\\s*throws\\s+Throwable\\s*;(.*?)\\}"
        ).map(s -> Pattern.compile(s).asPredicate()).reduce(Predicate::and).get();
    }

    public BaseScriptPanel(BaseScriptView scriptView) {
        super(scriptView);
        removeHeaderComponent();

        this.editorPane.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;

            var result = VALIDATOR.test(UIUtils.getText(this.editorPane));
            if (!result) {
                this.bottomInformationBar.setFailureInfoText("BaseScript is not in required format");
            } else {
                this.bottomInformationBar.setDefaultInfoText("BaseScript is valid");
            }
        });
    }

    @Override
    public boolean canSave() {
        var result = VALIDATOR.test(UIUtils.getText(this.editorPane));
        if (!result) {
            this.bottomInformationBar.setFailureInfoText("BaseScript is not in required format");
        } else {
            this.bottomInformationBar.setSuccessInfoText("Successfully updated BaseScript");
        }
        return result;
    }
}
