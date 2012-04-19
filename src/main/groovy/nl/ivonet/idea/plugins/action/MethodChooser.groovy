package nl.ivonet.idea.plugins.action

import com.intellij.psi.PsiClass

class MethodChooser {

    static final String JAVA_EQUALS_METHOD = 'equals'
    static final String JAVA_HASH_CODE_METHOD = 'hashCode'

    String chooseEqualsMethodName(PsiClass psiClass) {
//        PsiUtil.isLanguageLevel6OrHigher(psiClass) ? JAVA_EQUALS_METHOD : GUAVA_EQUALS_METHOD
        JAVA_EQUALS_METHOD
    }

    String chooseHashCodeMethodName(PsiClass psiClass) {
//        PsiUtil.isLanguageLevel6OrHigher(psiClass) ? JAVA_7_HASH_CODE_METHOD : JAVA_HASH_CODE_METHOD
        JAVA_HASH_CODE_METHOD
    }
}
