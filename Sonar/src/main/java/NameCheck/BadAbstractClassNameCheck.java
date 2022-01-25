package NameCheck;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.model.ModifiersUtils;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.util.regex.Pattern;

import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "S00118", repositoryKey = "squid")
@Rule(key = "S118")
public class BadAbstractClassNameCheck extends BaseTreeVisitor implements JavaFileScanner {

    private static final String DEFAULT_FORMAT = "^Abstract[A-Z][a-zA-Z0-9]*$";

    @RuleProperty(
            key = "format",
            description = "Regular expression used to check the abstract class names against.",
            defaultValue = "" + DEFAULT_FORMAT)
    public String format = DEFAULT_FORMAT;

    private Pattern pattern = null;
    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        if (pattern == null) {
            pattern = Pattern.compile(format, Pattern.DOTALL);
        }
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitClass(ClassTree tree) {
        IdentifierTree simpleName = tree.simpleName();
        if (tree.is(Tree.Kind.CLASS) && simpleName != null) {
            if (pattern.matcher(simpleName.name()).matches()) {
                if (!isAbstract(tree)) {
                    context.reportIssue(this, simpleName, "Make this class abstract or rename it, since it matches the regular expression '" + format + "'.");
                }
                else{
                    context.reportIssue(this, simpleName, "Rename this abstract class name to match the regular expression '" + format + "'.");
                }
            } else {
                if (isAbstract(tree)) {
                    context.reportIssue(this, simpleName, "Rename this abstract class name to match the regular expression '" + format + "'.");
                }
            }
        }
        super.visitClass(tree);
    }

    private static boolean isAbstract(ClassTree tree) {
        return ModifiersUtils.hasModifier(tree.modifiers(), Modifier.ABSTRACT);
    }

}
