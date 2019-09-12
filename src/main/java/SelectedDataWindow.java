import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.ui.treeStructure.Tree;
import com.sun.istack.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;

class SelectedDataWindow {
    private JPanel m_Panel;
    private int m_DeclaredVariablesNumber = 0;
    private int m_AccessedVariablesNumber = 0;
    private int m_ThrownExceptionsNumber  = 0;

    SelectedDataWindow(@NotNull final ASTNode topASTNode, final int selectionStart, final int selectionEnd)
    {
        MutableTreeNode topTreeNode;
        if (topASTNode.getElementType().toString().equals("CODE_BLOCK") &&
                !topASTNode.getTextRange().equalsToRange(selectionStart, selectionEnd + 1)) {
            DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Statements");
            for (ASTNode childNode: topASTNode.getChildren(TokenSet.andNot(TokenSet.ANY, TokenSet.WHITE_SPACE))) {
                if (childNode.getTextRange().getEndOffset() <= selectionStart ||
                    childNode.getTextRange().getStartOffset() > selectionEnd)
                    continue;
                topNode.add(fillTree(childNode));
            }
            topTreeNode = topNode.getChildCount() == 1 ? topNode.getNextNode() : topNode;
        } else {
            topTreeNode = fillTree(topASTNode);
        }

        Tree astTreeView = new Tree(topTreeNode);
        astTreeView.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
        JTextPane statisticsView = new JTextPane();
        statisticsView.setFont(statisticsView.getFont().deriveFont(14f));
        statisticsView.setEditable(false);
        statisticsView.setText(String.format(
                "Variables declared: %d\nVariable accesses: %d\nExceptions thrown: %d",
                m_DeclaredVariablesNumber, m_AccessedVariablesNumber, m_ThrownExceptionsNumber));

        m_Panel = new JPanel(new BorderLayout());
        m_Panel.add(astTreeView);
        m_Panel.add(statisticsView, BorderLayout.SOUTH);
    }

    private MutableTreeNode fillTree(@NotNull final ASTNode node)
    {
        // Not really sure on how to do it more efficiently as I couldn't find neither
        // derivatives of ElementType or PSIElement, nor methods which let me find out
        // which token is represented by current node
        final String elementName = node.getElementType().toString();
        switch (elementName) {
            case "LOCAL_VARIABLE": {
                ++m_DeclaredVariablesNumber;
                break;
            }
            case "REFERENCE_EXPRESSION": {
                ++m_AccessedVariablesNumber;
                break;
            }
            case "METHOD_CALL_EXPRESSION": { // Not sure if this should be treated as access to variable
                --m_AccessedVariablesNumber;
                break;
            }
            case "THROW_STATEMENT": {
                ++m_ThrownExceptionsNumber;
                break;
            }
            default:
                break;
        }

        DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(node.getElementType());
        for (ASTNode childNode: node.getChildren(TokenSet.andNot(TokenSet.ANY, TokenSet.WHITE_SPACE)))
            currentNode.add(fillTree(childNode));
        return currentNode;
    }

    JComponent getContent() {
        return m_Panel;
    }

}
