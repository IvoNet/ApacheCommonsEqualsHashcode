package nl.ivonet.idea.plugins.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import nl.ivonet.idea.plugins.wizard.GenerateEqualsHashCodeApacheCommonsWizard

class GenerateEqualsHashCodeApacheCommonsWizardFactory {

    GenerateEqualsHashCodeApacheCommonsWizard createWizard(Project project, PsiClass aClass, boolean needEquals, boolean needHashCode) {
        new GenerateEqualsHashCodeApacheCommonsWizard(project, aClass, needEquals, needHashCode)
    }
}
