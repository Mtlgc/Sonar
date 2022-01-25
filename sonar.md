<center style="font-size:36px">Sonar插件开发</center>

### Sonar简介

<span style="font-size:18px">Sonar是一个用于代码质量管理的开源平台，用于管理源代码的质量，可以从7个维度检测代码质量</span>

- <span style="font-size:18px">糟糕的复杂度分布</span>

  ![img](https://img-blog.csdn.net/20130914163710187?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVudGVybm80/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- <span style="font-size:18px">重复</span>

  ![img](https://img-blog.csdn.net/20130914163613875?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVudGVybm80/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- <span style="font-size:18px">缺乏单元测试</span>

  ![img](https://img-blog.csdn.net/20130914163851718?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVudGVybm80/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- <span style="font-size:18px">没有代码标准</span>

- <span style="font-size:18px">没有足够的注释</span>

- <span style="font-size:18px">潜在的bug</span>

  ![img](https://img-blog.csdn.net/20130914163925390?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVudGVybm80/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- <span style="font-size:18px">糟糕的设计</span>



### 根据需求编写插件

```java
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

```

### 单元测试

```java
import NameCheck.BadAbstractClassNameCheck;
import org.junit.Test;
import org.sonar.java.checks.verifier.CheckVerifier;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class BadAbstractClassNameCheckTest {
    @Test
    public void test(){
        BadAbstractClassNameCheck check = new BadAbstractClassNameCheck();
        JavaCheckVerifier.verify("src/main/java/AbstractTestClass.java",check);
    }
}

```

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9lcnZUQ2lid2F1akdVMTlSU21nOGhXSkZ5UnJwTHlzaWJOYnFKN0hXUmVpYkZ5NWZwRUZOWUV0VW1LNkVLWHJDbjFpY285RGhncVJwaWEzeEZpYmNya09DT25KUS82NDA_d3hfZm10PWpwZWc?x-oss-process=image/format,png)

<span style="font-size:20px">在图中可以看到有几个比较重要的对象：</span>

<span style="font-size:20px">visitorsBridge对象：用于保存通过规则对被测代码扫描后的结果</span>

<span style="font-size:20px">astScanner 对象：提供扫描被测代码的解析功能并生成抽象语法树--**Tree ast** 对象。</span>

<span style="font-size:20px">讲解一下抽象语法树，以下是被扫描的代码</span>

```java
class BadMethodName {
    private String id;
    public BadMethodName() {}
 
    void Bad() { // Noncompliant [[sc=8;ec=11]] {{Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'.}}
      }
  
    void good(String id) {
    	System.out.println("Test");
    }
 
}

```

<span style="font-size:20px">上面代码的抽象语法数，可以判断代码中的类是否为接口、类名、类变量、类中的方法</span>

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9lcnZUQ2lid2F1akdVMTlSU21nOGhXSkZ5UnJwTHlzaWJObFdRaWJtRGFUODE4cUtWb3RnbVVuWVQ0MlZVQnNtYlR0S0ZmY2NKUXZpYjJZVlJnMU1Cb21vb3cvNjQwP3d4X2ZtdD1qcGVn?x-oss-process=image/format,png)

<span style="font-size:20px">针对类变量的语法树如下：可以获取到变量名称、类型、修饰类型。</span>

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9lcnZUQ2lid2F1akdVMTlSU21nOGhXSkZ5UnJwTHlzaWJONXFpY3F4RUprbElWTjJ5dVZRYzFnNFlIQktmZm1pYnpPbVlWSmRnN1BXOEVpYWptU1E5SXE5MXhRLzY0MD93eF9mbXQ9anBlZw?x-oss-process=image/format,png)

<span style="font-size:20px">针对方法生成的语法树如下：可以获取到方法名、返回值、修饰符、是否是构造函数和代码块，所有在 **{ code }** 内的又会被解析成一个 **blockTree**。</span>

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9lcnZUQ2lid2F1akdVMTlSU21nOGhXSkZ5UnJwTHlzaWJONldURWlhUEQ1Szd4OEc1OVl5ejBqUkhnbUpnSXdvdXJBa1liQzdYUktPaWFaSEJuSHl5b1Rwb0EvNjQwP3d4X2ZtdD1qcGVn?x-oss-process=image/format,png)