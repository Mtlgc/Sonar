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
