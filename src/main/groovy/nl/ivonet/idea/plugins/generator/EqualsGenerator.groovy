package nl.ivonet.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.annotations.NotNull

class EqualsGenerator {

    PsiMethod equalsMethod(@NotNull List<PsiField> equalsPsiFields, PsiClass psiClass, String equalsMethodName) {
        if (!equalsPsiFields.isEmpty()) {
            PsiElementFactory factory = getFactory(equalsPsiFields[0])
            StringBuilder methodText = new StringBuilder()
            methodText << "@Override public boolean ${equalsMethodName}(final Object obj) {"
            methodText << ' if (obj == null) {return false;}'
            methodText << ' if (getClass() != obj.getClass()) {return false;}'
            methodText << " final ${psiClass.name} other = (${psiClass.name}) obj;"
            methodText << ' return new org.apache.commons.lang.builder.EqualsBuilder()'
            println "psiClass.superclass      = $psiClass.superClass"
            println "psiClass.superclazz.name = $psiClass.superClass.name"

            if (psiClass != null && !psiClass.superClass.name.equals("Object")) {
                methodText << '.appendSuper(super.equals(obj))'
            }
            equalsPsiFields.eachWithIndex { field, index ->
                methodText << ".append(this.${field.name}, other.${field.name})"
//                if (index < equalsPsiFields.size() - 1) {
//                }
            }
            methodText << '.isEquals();}'
            factory.createMethodFromText(methodText.toString(), null, LanguageLevel.JDK_1_6)
        } else {
            null
        }
    }

    private PsiElementFactory getFactory(PsiField psiField) {
        JavaPsiFacade.getInstance(psiField.project).elementFactory
    }
}


