package nl.ivonet.idea.plugins.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass

import spock.lang.Specification
import nl.ivonet.idea.plugins.wizard.GenerateEqualsHashCodeApacheCommonsWizard

class GenerateEqualsHashCodeApacheCommonsWizardFactoryTest extends Specification {

    GenerateEqualsHashCodeApacheCommonsWizardFactory factory = new GenerateEqualsHashCodeApacheCommonsWizardFactory()

    GenerateEqualsHashCodeApacheCommonsWizard mock = Mock()

    def setup() {
        GenerateEqualsHashCodeApacheCommonsWizard.metaClass.constructor = {Project project, PsiClass aClass, boolean needEquals, boolean needHashCode -> mock }
    }

    def "creates object using constructor"() {
        when:
        GenerateEqualsHashCodeApacheCommonsWizard wizard = factory.createWizard(Mock(Project), Mock(PsiClass), true, true)

        then:
        wizard == mock
    }
}
