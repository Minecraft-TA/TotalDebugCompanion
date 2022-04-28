package com.github.minecraft_ta.totalDebugCompanion.messages.codeView;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics.ASTCache;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.scnet.message.AbstractMessage;
import com.github.tth05.scnet.util.ByteBufferInputStream;
import com.github.tth05.scnet.util.ByteBufferOutputStream;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public class DecompileOrOpenMessage extends AbstractMessage {

    private static final String CU_NAME = "Name";
    private static final Pattern TYPE_PATTERN = Pattern.compile("(T\\w+;)|(L[\\w/$]+;)");

    private String name;
    private int targetType;
    private String targetIdentifier;

    public DecompileOrOpenMessage() {
    }

    public DecompileOrOpenMessage(String name) {
        this(name, -1, null);
    }

    public DecompileOrOpenMessage(String name, int targetType, String targetIdentifier) {
        this.name = name;
        this.targetType = targetType;
        this.targetIdentifier = targetIdentifier == null ? "" : targetIdentifier;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(this.name);
        messageStream.writeInt(this.targetType);
        messageStream.writeString(this.targetIdentifier);
    }

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.name = messageStream.readString();
        this.targetType = messageStream.readInt();
        this.targetIdentifier = fixMethodIdentifier(messageStream.readString());
    }

    public static void handle(DecompileOrOpenMessage message) {
        var filePath = Paths.get(message.name);
        if (!Files.exists(filePath) || !FileUtils.isSubPathOf(CompanionApp.getRootPath(), filePath))
            return;

        int offset = 0;
        if (message.targetType != -1) {
            var ast = ASTCache.rawParse(CU_NAME, CodeView.readCode(filePath));
            var firstType = ast.types().get(0);
            if (!(firstType instanceof TypeDeclaration) && !(firstType instanceof EnumDeclaration)) {
                System.err.println("Failed to get type declaration");
                return;
            }

            var type = (AbstractTypeDeclaration) firstType;

            if (message.targetType == IJavaElement.METHOD) {
                var targetMethod = findTargetMethod(message, type);
                if (targetMethod.isEmpty()) {
                    System.err.println("Failed to find target method");
                    return;
                }

                offset = targetMethod.get().getStartPosition();
            } else if (message.targetType == IJavaElement.FIELD) {
                var targetField = findTargetField(message, type);

                if (targetField.isEmpty()) {
                    System.err.println("Failed to find target field");
                    return;
                }

                offset = targetField.getAsInt();
            } else {
                System.err.println("Unknown target type");
                return;
            }
        }

        var window = MainWindow.INSTANCE;
        var finalOffset = offset;
        var codeView = window.getEditorTabs().focusOrCreateIfAbsent(
                CodeView.class,
                (cv) -> cv.getPath().equals(filePath),
                () -> new CodeView(filePath, finalOffset)
        ).join();
        codeView.centerViewportOnOffset(offset);

        UIUtils.focusWindow(window);
    }

    private static Optional<MethodDeclaration> findTargetMethod(DecompileOrOpenMessage message, AbstractTypeDeclaration type) {
        //noinspection unchecked
        return ((List<BodyDeclaration>) type.bodyDeclarations()).stream()
                .filter(decl -> decl instanceof MethodDeclaration)
                .map(decl -> (MethodDeclaration) decl)
                .filter((m) -> fixMethodIdentifier(m.resolveBinding().getKey()).equals(message.targetIdentifier))
                .findFirst();
    }

    private static OptionalInt findTargetField(DecompileOrOpenMessage message, AbstractTypeDeclaration type) {
        var declarations = type instanceof TypeDeclaration ? type.bodyDeclarations() : ((EnumDeclaration) type).enumConstants();
        //noinspection unchecked
        return declarations.stream()
                .filter(decl -> decl instanceof FieldDeclaration || decl instanceof EnumConstantDeclaration)
                .filter(decl -> {
                    if (decl instanceof EnumConstantDeclaration enumConstant) {
                        return enumConstant.getName().getIdentifier().equals(message.targetIdentifier);
                    } else {
                        FieldDeclaration field = (FieldDeclaration) decl;
                        //noinspection unchecked
                        return field.fragments().stream().anyMatch(frag -> ((VariableDeclarationFragment) frag).getName().getIdentifier().equals(message.targetIdentifier));
                    }
                }).mapToInt(decl -> {
                    if (decl instanceof EnumConstantDeclaration enumConstant)
                        return enumConstant.getStartPosition();
                    else
                        return ((FieldDeclaration) decl).getStartPosition();
                }).findFirst();
    }

    /**
     * Fix the identifier for a method. This removes the most amount of information possible from the given method
     * identifier without loosing any uniqueness. Can be used to make comparisons between method identifiers easier.
     *
     * @param key the original key
     * @return the fixed key
     */
    private static String fixMethodIdentifier(String key) {
        var builder = new StringBuilder(key);

        // Remove exception data
        var exceptionIndex = builder.indexOf("|");
        if (exceptionIndex != -1)
            builder.delete(exceptionIndex, builder.length());

        // Remove useless super class data
        var percentIndex = builder.lastIndexOf("%");
        if (percentIndex != -1)
            builder.delete(percentIndex, builder.length());

        // Remove class name, class type parameters
        var dotIndex = builder.indexOf(".");
        if (dotIndex != -1)
            builder.delete(0, dotIndex + 1);

        // Remove any type parameters
        var genericIndex = -1;
        while ((genericIndex = builder.indexOf("<")) != -1) {
            var endIndex = genericIndex + 1;
            var count = 1;
            //Find closing '>'
            for (; count != 0 && endIndex < builder.length(); endIndex++) {
                var c = builder.charAt(endIndex);
                if (c == '<')
                    count++;
                else if (c == '>')
                    count--;
            }
            if (count != 0 || endIndex == -1)
                break;

            builder.delete(genericIndex, endIndex);
        }

        // Replace unwanted CU_NAME from JDT and reduce any type or generic type to just java.lang.Object
        return TYPE_PATTERN.matcher(builder.toString().replace(CU_NAME + "~", "")).replaceAll("Ljava/lang/Object;");
    }
}
