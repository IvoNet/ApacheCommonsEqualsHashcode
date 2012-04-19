package nl.ivonet.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.annotations.NotNull

class HashCodeGenerator {

    PsiMethod hashCodeMethod(@NotNull List<PsiField> hashCodePsiFields, String hashCodeMethodName) {
        if (!hashCodePsiFields.isEmpty()) {
            PsiElementFactory factory = getFactory(hashCodePsiFields[0])
            StringBuilder methodText = new StringBuilder()
            methodText << "@Override public int ${hashCodeMethodName}() {"
            methodText << ' return new org.apache.commons.lang.builder.HashCodeBuilder().appendSuper(super.hashCode())'
            hashCodePsiFields.eachWithIndex { field, index ->
                methodText << ".append(this.${field.name})"
//                if (index < hashCodePsiFields.size() - 1) {
//                }
            }
            methodText << '.toHashCode();}'
            factory.createMethodFromText(methodText.toString(), null, LanguageLevel.JDK_1_6)
        } else {
            null
        }
    }

    private PsiElementFactory getFactory(PsiField psiField) {
        JavaPsiFacade.getInstance(psiField.project).elementFactory
    }
}
