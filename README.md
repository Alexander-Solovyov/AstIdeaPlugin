## Solution description

Plugin consists of two main classes: **ShowAstAction** and **SelectedDataWindow**.

In order to output AST this plugin uses *PsiFile* which it can get from *event* when an *actionPerformed* is called.

### ShowAstAction
A simple class derived from AnAction. It has two main purposes. The first one is two check if there is a text selection which is checked via *SelectionModel* of *editor*. This functionality is implemented in method *update*.

The second purpose is to create **SelectedDataWindow** instance and pass all the data it needs when the corresponding button is pressed. This functionality is implemented in method *actionPerformed*. The most of it's code if needed to obtain all the data needed and show message if something went wrong. When obtaining data it uses *SelectionModel* and *PsiTreeUtil.findCommonContext* to get the lower parent AST node. Then it creates **SelectedDataWindow** instance and gets a content of new *ToolWindow*, thus making requested data visible. 

### SelectedDataWindow
This class is needed to build a form content which later can be requested and placed in *toolWindow*. This class has constructor which builds content, package-private method for requesting content and one recursive method which is needed to go through AST. 

#### Constructor
When constructor is called it's first action is to check, whether a full block of code is selected or only it's part. Based on result of this check the analysis starts either from the given AST node or to make a new parent node for display purposes and to do independent analysis for nodes children. Without it much more code than selected could've been analyzed. After these actions constructor creates all the objects needed to display data.
<details>
  <summary>Example showing why a check on "full block" was needed</summary>

Full method:
```java
void someShadyMethod() {
  int x = getX();
  int y = getY();
  Call1(x, y);
  Call2(x, x, y, x);
}
```

Selected code:
```java
  int y = getY();
  Call1(x, y);
```

Analyzed part from the given AST Node:
```java
{
  int x = getX();
  int y = getY();
  Call1(x, y);
  Call2(x, x, y, x);
}
```
</details>

#### FillTree method
This method is needed to recursively go through AST while building nodes and counting the variables declarations/accesses and number of exceptions thrown. The later is made by checking AST nodes types. After this the new Node is constructed and recursive call for all the childrens is being made. Results of this calls are added as child Nodes to the node created earlier in this method. After this the very same node is returned as a result.

<details>
  <summary>Current AST nodes type check</summary>
In the current implementation it was made by using string representation of *IElementType* which is not the best choice, but it works. Sadly I couldn't find a better way to do it as neither was I able to find the answer to this question on the web, official FAQ included, nor was I able to find classes representing these elements by myself.
</details>
