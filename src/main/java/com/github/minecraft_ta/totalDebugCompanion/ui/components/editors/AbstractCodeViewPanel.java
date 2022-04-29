package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics.ASTCache;
import com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics.CustomJavaLinkGenerator;
import com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting.CustomJavaTokenMaker;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.BottomInformationBar;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SearchEverywherePopup;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.jindex.IndexedMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class AbstractCodeViewPanel extends JPanel {

    public static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"), "Unable to load font"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(JETBRAINS_MONO_FONT);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    protected final RSyntaxTextArea editorPane = new RSyntaxTextArea() {
        @Override
        public boolean getUnderlineForToken(Token t) {
            return false;
        }
    };
    protected final RTextScrollPane editorScrollPane = new RTextScrollPane(editorPane);

    protected final BottomInformationBar bottomInformationBar = new BottomInformationBar();
    private final String identifier;

    protected JComponent headerComponent;

    public AbstractCodeViewPanel(String identifier, String className) {
        super(new BorderLayout());
        this.identifier = identifier;

        this.editorScrollPane.getGutter().setBorder(new Gutter.GutterBorder(0, 5, 0, 0));
        this.editorScrollPane.getGutter().setForeground(Color.GRAY);
        this.editorScrollPane.getGutter().setBackground(UIManager.getColor("TextPane.background"));
        this.editorScrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.editorPane.setAnimateBracketMatching(false);
        this.editorPane.setPaintMatchedBracketPair(true);
        this.editorPane.setMatchedBracketBGColor(Color.GRAY);
        this.editorPane.setMatchedBracketBorderColor(null);
        this.editorPane.setCurrentLineHighlightColor(Color.decode("#4e5052"));
        this.editorPane.setBackground(UIManager.getColor("TextPane.background"));
        this.editorPane.setForeground(UIManager.getColor("EditorPane.foreground"));
        this.editorPane.setSelectionColor(UIManager.getColor("EditorPane.selectionBackground"));
        this.editorPane.setHyperlinkForeground(Color.decode("#7cc0f7"));
        this.editorPane.setLinkGenerator(new CustomJavaLinkGenerator(identifier, this.bottomInformationBar));
        this.editorPane.addHyperlinkListener(e -> {}); //Empty listener to circumvent RSyntaxTextArea bug
        this.editorPane.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;

            ASTCache.update(identifier, className, UIUtils.getText(this.editorPane));
        });
        this.editorPane.addCaretListener(e -> this.editorPane.getCaret().setVisible(true));

        this.editorPane.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_JAVA);
        CodeUtils.initJavaColors(this.editorPane.getSyntaxScheme());
        try {
            var document = this.editorPane.getDocument();
            var field = document.getClass().getDeclaredField("tokenMaker");
            field.setAccessible(true);
            ((CustomJavaTokenMaker) field.get(document)).setASTKey(identifier, this.editorPane);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        this.editorPane.getActionMap().put(FindImplementationsAction.KEY, new FindImplementationsAction());
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl T"), FindImplementationsAction.KEY);

        add(this.editorScrollPane, BorderLayout.CENTER);
        add(this.bottomInformationBar, BorderLayout.SOUTH);

        updateFonts();

        PropertyChangeListener fontSizeListener = event -> updateFonts();
        GlobalConfig.getInstance().addPropertyChangeListener("fontSize", fontSizeListener);
        addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && getParent() == null) {
                ASTCache.removeFromCache(identifier);
                GlobalConfig.getInstance().removePropertyChangeListener("fontSize", fontSizeListener);
            }
        });
    }

    public void centerViewportOnOffset(int offset) {
        SwingUtilities.invokeLater(() -> UIUtils.centerViewportOnRange(this.editorScrollPane, offset, offset));
    }

    public void setHeaderComponent(JComponent component) {
        removeHeaderComponent();
        this.headerComponent = component;
        add(component, BorderLayout.NORTH);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            //Adjust scroll bar to keep it in place
            var verticalScrollBar = editorScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue((int) (verticalScrollBar.getValue() + component.getPreferredSize().getHeight()));
        });
    }

    public void removeHeaderComponent() {
        if (this.headerComponent == null)
            return;

        synchronized (getTreeLock()) {
            var i = 0;
            for (; i < getComponentCount(); i++) {
                var component = getComponent(i);
                if (component == this.headerComponent) {
                    remove(i);
                    revalidate();
                    repaint();
                    //Adjust scroll bar to keep it in place
                    var verticalScrollBar = editorScrollPane.getVerticalScrollBar();
                    verticalScrollBar.setValue(verticalScrollBar.getValue() - component.getHeight());
                    break;
                }
            }
        }

        this.headerComponent = null;
    }

    protected void updateFonts() {
        var newFont = JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize"));
        this.editorPane.setFractionalFontMetricsEnabled(true);
        this.editorPane.setFont(newFont);

        this.editorScrollPane.getGutter().setLineNumberFont(newFont);
    }

    private class FindImplementationsAction extends TextAction {

        private static final String KEY = "findImplementations";

        public FindImplementationsAction() {
            super(KEY);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (!CompanionApp.SERVER.isClientConnected()) {
                bottomInformationBar.setFailureInfoText("Not connected to game client!");
                return;
            }

            var textArea = (RSyntaxTextArea) getTextComponent(event);

            try {
                var fromCache = ASTCache.getFromCache(identifier);
                if (fromCache == null)
                    return;

                var offset = textArea.getCaretPosition();
                var elements = fromCache.getTypeRoot().codeSelect(offset, 0);
                if (elements == null || elements.length == 0)
                    return;
                if (elements.length > 1) {
                    System.err.println("Multiple elements found at offset " + offset + ": " + Arrays.toString(elements));
                    return;
                }

                var el = elements[0];
                if (el.getClass() == SourceMethod.class || el.getClass() == SourceType.class) {
                    switch (el) {
                        case SourceMethod sr -> {
                            var className = CodeUtils.splitTypeName(sr.getDeclaringType().getFullyQualifiedName());
                            var declaringClass = SearchEverywherePopup.CLASS_INDEX.findClass(className[0], className[1]);
                            if (declaringClass == null)
                                return;

                            var targetDescriptor = CodeUtils.minimalizeMethodIdentifier(sr.getSignature());
                            for (IndexedMethod method : declaringClass.getMethods()) {
                                if (method.getName().equals(sr.getElementName()) && CodeUtils.minimalizeMethodIdentifier(method.getDescriptorString()).equals(targetDescriptor)) {
                                    var implementations = method.findImplementations(false);
                                    System.out.println(Arrays.stream(implementations).map(m -> m.getDeclaringClass().getNameWithPackage() + "." + m.getName()).toList());
                                    return;
                                }
                            }

                            System.err.println("Method not found: " + sr.getSignature());
                            return;
                        }
                        case SourceType st -> {
                            var className = CodeUtils.splitTypeName(st.getFullyQualifiedName());
                            var indexedClass = SearchEverywherePopup.CLASS_INDEX.findClass(className[0], className[1]);
                            if (indexedClass == null)
                                return;

                            var list = Arrays.stream(indexedClass.findImplementations(false)).map(c -> c.getNameWithPackage()).toList();
                            System.out.println(list.size() + " - "
                            );
                        }
                        default -> {

                        }
                    }
                    ;

                }
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
        }
    }
}
