import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.ui.treeStructure.Tree;
import com.sun.istack.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;

final class SelectedDataWindow {
    final private JPanel panel;
    private int declaredVariablesNumber = 0;
    private int accessedVariablesNumber = 0;
    private int thrownExceptionsNumber = 0;

    SelectedDataWindow(@NotNull final ASTNode topASTNode, final int selectionStart, final int selectionEnd) {
        final Tree astTreeView = new Tree(BuildAstTree(topASTNode, selectionStart, selectionEnd));
        astTreeView.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
        final JTextPane statisticsView = new JTextPane();
        statisticsView.setFont(statisticsView.getFont().deriveFont(14f));
        statisticsView.setEditable(false);
        statisticsView.setText(String.format(
                "Variables declared: %d\nVariable accesses: %d\nExceptions thrown: %d",
                declaredVariablesNumber, accessedVariablesNumber, thrownExceptionsNumber));

        panel = new JPanel(new BorderLayout());
        panel.add(astTreeView);
        panel.add(statisticsView, BorderLayout.SOUTH);
    }

    private MutableTreeNode BuildAstTree(@NotNull ASTNode topASTNode, int selectionStart, int selectionEnd) {
        if (topASTNode.getElementType().toString().equals("CODE_BLOCK") &&
                !topASTNode.getTextRange().equalsToRange(selectionStart, selectionEnd + 1)) {
            final DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Statements");
            for (ASTNode childNode : topASTNode.getChildren(TokenSet.andNot(TokenSet.ANY, TokenSet.WHITE_SPACE))) {
                if (childNode.getTextRange().getEndOffset() <= selectionStart ||
                        childNode.getTextRange().getStartOffset() > selectionEnd)
                    continue;
                topNode.add(fillTree(childNode));
            }
            return topNode.getChildCount() == 1 ? topNode.getNextNode() : topNode;
        } else {
            return fillTree(topASTNode);
        }
    }

    private MutableTreeNode fillTree(@NotNull final ASTNode node) {
        // Not really sure on how to do it more efficiently as I couldn't find neither
        // derivatives of ElementType or PSIElement, nor methods which let me find out
        // which token is represented by current node
        final String elementName = node.getElementType().toString();
        switch (elementName) {
            case "LOCAL_VARIABLE": {
                ++declaredVariablesNumber;
                break;
            }
            case "REFERENCE_EXPRESSION": {
                ++accessedVariablesNumber;
                break;
            }
            case "METHOD_CALL_EXPRESSION": { // Not sure if this should be treated as access to variable
                --accessedVariablesNumber;
                break;
            }
            case "THROW_STATEMENT": {
                ++thrownExceptionsNumber;
                break;
            }
            default:
                break;
        }

        final DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(node.getElementType());
        for (ASTNode childNode : node.getChildren(TokenSet.andNot(TokenSet.ANY, TokenSet.WHITE_SPACE)))
            currentNode.add(fillTree(childNode));
        return currentNode;
    }

    JComponent getContent() {
        return panel;
    }

}
