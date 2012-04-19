package nl.ivonet.idea.plugins.action

import com.intellij.codeInsight.generation.actions.BaseGenerateAction
import org.picocontainer.MutablePicoContainer
import org.picocontainer.defaults.DefaultPicoContainer
import nl.ivonet.idea.plugins.factory.GenerateEqualsHashCodeApacheCommonsWizardFactory
import nl.ivonet.idea.plugins.generator.EqualsGenerator
import nl.ivonet.idea.plugins.generator.HashCodeGenerator

class GenerateEqualsHashCodeApacheCommonsAction extends BaseGenerateAction {

    private static MutablePicoContainer picoContainer = new DefaultPicoContainer()

    private static GenerateEqualsHashApacheCommonsHandler handler

    static {
        picoContainer.registerComponentImplementation(HashCodeGenerator)
        picoContainer.registerComponentImplementation(EqualsGenerator)
        picoContainer.registerComponentImplementation(MethodChooser)
        picoContainer.registerComponentImplementation(GenerateEqualsHashCodeApacheCommonsWizardFactory)
        picoContainer.registerComponentImplementation(GenerateEqualsHashApacheCommonsHandler)
        handler = picoContainer.getComponentInstanceOfType(GenerateEqualsHashApacheCommonsHandler)
    }


    protected GenerateEqualsHashCodeApacheCommonsAction() {
        super(handler)
    }
}
