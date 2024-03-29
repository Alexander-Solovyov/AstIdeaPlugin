import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.sun.istack.NotNull;

final public class ShowAstAction extends AnAction {
    public ShowAstAction() {
        super("Build AST");
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        final Project project = event.getProject();
        final Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null || !editor.getSelectionModel().hasSelection()) {
            Messages.showMessageDialog(project, "No Text Selected!", "Warning", Messages.getWarningIcon());
            return;
        }

        final PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);

        if (psiFile == null) {
            Messages.showMessageDialog(project, "Can't get PSI view", "Warning", Messages.getWarningIcon());
            return;
        }

        final PsiElement firstElement = psiFile.findElementAt(editor.getSelectionModel().getSelectionStart());
        final PsiElement lastElement = psiFile.findElementAt(editor.getSelectionModel().getSelectionEnd() - 1);
        if (firstElement == null || lastElement == null) {
            Messages.showInfoMessage("PSI elements missed for selection!", "PSI Elements Missed");
            return;
        }
        final PsiElement rootElement = PsiTreeUtil.findCommonContext(firstElement, lastElement);
        if (rootElement == null) {
            Messages.showInfoMessage("Couldn't find top PSI element for selection!", "Top PSI Element Missed");
            return;
        }

        final SelectedDataWindow window = new SelectedDataWindow(
                rootElement.getNode(), editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd() - 1);
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("SelectedAst.ShowData");
        if (toolWindow == null) {
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(
                    "SelectedAst.ShowData", false, ToolWindowAnchor.RIGHT);
            toolWindow.setTitle("AST and Statistics");
        }
        final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        final Content content = contentFactory.createContent(window.getContent(), "AST and Statistics", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
        toolWindow.show(null);
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        final Project project = event.getProject();
        final Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().setEnabled(
                project != null && editor != null && editor.getSelectionModel().hasSelection());
    }
}
