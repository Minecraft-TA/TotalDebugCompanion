package com.github.minecraft_ta.totalDebugCompanion.jdt.completion;

import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.CompilationUnitImpl;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class CustomJavaCompletionProvider extends DefaultCompletionProvider {

    public CustomJavaCompletionProvider() {
        setAutoActivationRules(true, ".");
    }

    @Override
    public List<Completion> getCompletions(JTextComponent comp) {
        var latch = new CountDownLatch(1);
        var completions = new ArrayList<CompletionProposal>();
        try {
            new CompilationUnitImpl("SomeName", UIUtils.getText(comp)).codeComplete(comp.getCaretPosition(), new CompletionRequestor() {
                        @Override
                        public void accept(CompletionProposal proposal) {
                            completions.add(proposal);
                        }
                    },
                    new IProgressMonitor() {
                        @Override
                        public void beginTask(String name, int totalWork) {

                        }

                        @Override
                        public void done() {
                            latch.countDown();
                        }

                        @Override
                        public void internalWorked(double work) {

                        }

                        @Override
                        public boolean isCanceled() {
                            return false;
                        }

                        @Override
                        public void setCanceled(boolean value) {

                        }

                        @Override
                        public void setTaskName(String name) {

                        }

                        @Override
                        public void subTask(String name) {

                        }

                        @Override
                        public void worked(int work) {

                        }
                    });
            latch.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return completions.stream()
                .sorted(new CompletionProposalComparator())
                .map(c -> new BasicCompletion(this, new String(c.getCompletion())))
                .collect(Collectors.toList());
    }

    private static class CompletionProposalComparator implements Comparator<CompletionProposal> {

        @Override
        public int compare(CompletionProposal a, CompletionProposal b) {
            int res = b.getRelevance() - a.getRelevance();
            if (res == 0)
                res = a.getKind() - b.getKind();
            if (res == 0) {
                char[] completion1 = a.getCompletion();
                char[] completion2 = a.getCompletion();

                res = CharOperation.compareTo(completion1, completion2);
                if (res == 0)
                    res = completion1.length - completion2.length;
            }
            return res;
        }
    }
}
