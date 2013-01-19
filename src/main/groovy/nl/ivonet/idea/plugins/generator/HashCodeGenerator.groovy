package nl.ivonet.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.*
import org.jetbrains.annotations.NotNull

class HashCodeGenerator {

    PsiMethod hashCodeMethod(@NotNull List<PsiField> hashCodePsiFields, PsiClass psiClass, String hashCodeMethodName) {
        if (!hashCodePsiFields.isEmpty()) {
            PsiElementFactory factory = getFactory(hashCodePsiFields[0])
            StringBuilder methodText = new StringBuilder()
            methodText << "@Override public int ${hashCodeMethodName}() {"
            methodText << ' return new org.apache.commons.lang.builder.HashCodeBuilder()'
            if (!psiClass.superClass.name.equals('Object')) {
                methodText << '.appendSuper(super.hashCode())'
            }
            hashCodePsiFields.eachWithIndex { field, index ->
                if (field != null) {
                    methodText << ".append(this.${field.name})"
                }
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
