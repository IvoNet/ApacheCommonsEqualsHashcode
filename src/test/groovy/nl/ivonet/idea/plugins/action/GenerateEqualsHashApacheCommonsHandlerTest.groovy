package nl.ivonet.idea.plugins.action

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.generation.ClassMember
import com.intellij.codeInsight.generation.GenerateEqualsHelper
import com.intellij.codeInsight.generation.OverrideImplementUtil
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.MethodSignature
import javax.swing.Icon

import nl.ivonet.idea.plugins.generator.EqualsGenerator
import nl.ivonet.idea.plugins.generator.HashCodeGenerator

import spock.lang.Specification
import nl.ivonet.idea.plugins.wizard.GenerateEqualsHashCodeApacheCommonsWizard
import nl.ivonet.idea.plugins.factory.GenerateEqualsHashCodeApacheCommonsWizardFactory

class GenerateEqualsHashApacheCommonsHandlerTest extends Specification {

    private static final int OK_EXIT_CODE = DialogWrapper.OK_EXIT_CODE
    private static final int NOT_OK_EXIT_CODE = DialogWrapper.OK_EXIT_CODE + 1

    GenerateEqualsHashApacheCommonsHandler actionHandler

    HashCodeGenerator guavaHashCodeGenerator = Mock()
    EqualsGenerator guavaEqualsGenerator = Mock()
    MethodChooser methodChooser = Mock()
    GenerateEqualsHashCodeApacheCommonsWizardFactory factory = Mock()

    PsiClass psiClass = Mock()
    Project project = Mock()
    Editor editor = Mock()
    MethodSignature equalsMethodSignature = Mock()
    MethodSignature hashCodeMethodSignature = Mock()
    PsiMethodImpl equalsMethod = Mock()
    PsiMethodImpl hashCodeMethod = Mock()
    Application application = Mock()
    HintManager hintManager = Mock()
    GenerateEqualsHashCodeApacheCommonsWizard wizard = Mock()
    PsiField[] wizardEqualsFields = [Mock(PsiField)]
    PsiField[] wizardHashCodeFields = [Mock(PsiField)]
    ClassMember classMember = Mock()
    ClassMember[] result

    def setup() {
        actionHandler = new GenerateEqualsHashApacheCommonsHandler(guavaHashCodeGenerator, guavaEqualsGenerator, methodChooser, factory)

        GenerateEqualsHelper.metaClass.'static'.getEqualsSignature = { Project project, GlobalSearchScope scope -> equalsMethodSignature }
        GenerateEqualsHelper.metaClass.'static'.getHashCodeSignature = { hashCodeMethodSignature }
        GenerateEqualsHelper.metaClass.'static'.findMethod = {PsiClass psiClass, MethodSignature methodSignature -> null}
        CodeInsightBundle.metaClass.'static'.message = {String key -> 'anyString'}
        Messages.metaClass.'static'.getQuestionIcon = {Mock(Icon)}
        ApplicationManager.metaClass.'static'.getApplication = {application}
        HintManager.metaClass.'static'.getInstance = {hintManager}
        factory.createWizard(project, psiClass, true, true) >> wizard
    }

    def "does not display wizard when methods exist and user decides not to delete them"() {
        equalsAndHashCodeExist()
        userClicksNoInDeleteDialog()

        when:
        result = actionHandler.chooseOriginalMembers(psiClass, project, editor)

        then:
        interaction {
            wizardIsNotDisplayed()
        }
        fieldsFromWizardAreNotAssigned()
    }

    def "does not display wizard when methods exist but deletion is not successful"() {
        equalsAndHashCodeExist()
        userClicksYesInDeleteDialog()
        deletionNotSuccessful()

        when:
        result = actionHandler.chooseOriginalMembers(psiClass, project, editor)

        then:
        interaction {
            wizardIsNotDisplayed()
        }
        fieldsFromWizardAreNotAssigned()
    }

    def "displays error message when class has only static fields"() {
        equalsAndHashCodeExist()
        userClicksYesInDeleteDialog()
        deletionSuccessful()
        classHasOnlyStaticFields()

        when:
        result = actionHandler.chooseOriginalMembers(psiClass, project, editor)

        then:
        interaction {
            errorMessageIsDisplayed()
            wizardIsNotDisplayed()
        }
        fieldsFromWizardAreNotAssigned()
    }

    def "shows wizard but does not create methods because user clicks cancel"() {
        classHasNoStaticField()
        userClicksCancelInWizard()

        when:
        result = actionHandler.chooseOriginalMembers(psiClass, project, editor)

        then:
        fieldsFromWizardAreNotAssigned()
        interaction {
            wizardIsDisplayed()
        }
    }

    def "chosen fields from wizard are assigned"() {
        classHasNoStaticField()
        userClicksOkInWizard()
        wizardHasChosenFields()

        when:
        result = actionHandler.chooseOriginalMembers(psiClass, project, editor)

        then:
        fieldsFromWizardAreAssigned()
        interaction {
            wizardIsDisplayed()
        }
    }

    def "returns no original members"() {
        when:
        def result = actionHandler.getAllOriginalMembers(psiClass)

        then:
        result == null
    }

    def "generates no member prototypes"() {
        when:
        def result = actionHandler.generateMemberPrototypes(psiClass, classMember)

        then:
        result == null
    }

    def "assigns fields to null on cleanup"() {
        fieldsAreAssigned()

        when:
        actionHandler.cleanup()

        then:
        fieldsAreNotAssigned()
    }


    def "returns list with generated methods as list of GenerationInfo objects"() {
        String equalsMethodName = 'equalsMethodName'
        methodChooser.chooseEqualsMethodName(psiClass) >> equalsMethodName
        String hashCodeMethodName = 'hashCodeMethodName'
        methodChooser.chooseHashCodeMethodName(psiClass) >> hashCodeMethodName

        guavaEqualsGenerator.equalsMethod(null, psiClass, equalsMethodName) >> equalsMethod
        guavaHashCodeGenerator.hashCodeMethod(null, psiClass, hashCodeMethodName) >> hashCodeMethod

        def list = Mock(List)
        OverrideImplementUtil.metaClass.'static'.convert2GenerationInfos = { Collection collection ->
            if (collection == [hashCodeMethod, equalsMethod]) {
                return list
            }
            null
        }

        when:
        def result = actionHandler.generateMemberPrototypes(psiClass, [classMember] as ClassMember[])

        then:
        result == list
    }


    def fieldsAreAssigned() {
        actionHandler.equalsFields = [Mock(PsiField)]
        actionHandler.hashCodeFields = [Mock(PsiField)]
    }

    def userClicksOkInWizard() {
        wizard.isOK() >> true
    }

    def userClicksCancelInWizard() {
        wizard.isOK() >> false
    }

    def classHasNoStaticField() {
        PsiField field = Mock()
        field.hasModifierProperty(PsiModifier.STATIC) >> false
        PsiField[] fields = [field]
        psiClass.fields >> fields
    }

    def classHasOnlyStaticFields() {
        PsiField field = Mock()
        field.hasModifierProperty(PsiModifier.STATIC) >> true
        PsiField[] fields = [field]
        psiClass.fields >> fields
    }

    def deletionNotSuccessful() {
        application.runWriteAction(_ as DeleteExistingMethodsComputable) >> false
    }

    def deletionSuccessful() {
        application.runWriteAction(_ as DeleteExistingMethodsComputable) >> true
    }

    def userClicksYesInDeleteDialog() {
        Messages.metaClass.'static'.showYesNoDialog = {Project project, String message, String title, Icon icon -> OK_EXIT_CODE}
    }

    def userClicksNoInDeleteDialog() {
        Messages.metaClass.'static'.showYesNoDialog = {Project project, String message, String title, Icon icon -> NOT_OK_EXIT_CODE}
    }

    def equalsAndHashCodeExist() {
        GenerateEqualsHelper.metaClass.'static'.findMethod = {PsiClass psiClass, MethodSignature methodSignature ->
            if (methodSignature == equalsMethodSignature) {
                equalsMethod
            } else {
                hashCodeMethod
            }
        }
    }

    def wizardHasChosenFields() {
        wizard.equalsFields >> wizardEqualsFields
        wizard.hashCodeFields >> wizardHashCodeFields
    }

    def wizardIsNotDisplayed() {
        0 * factory._
    }

    def wizardIsDisplayed() {
        1 * wizard.show()
    }

    def errorMessageIsDisplayed() {
        1 * hintManager.showErrorHint(editor, actionHandler.ONLY_STATIC_FIELDS_ERROR)
    }

    void fieldsAreNotAssigned() {
        assert actionHandler.equalsFields == null
        assert actionHandler.hashCodeFields == null
    }

    void fieldsFromWizardAreAssigned() {
        assert result != null
        assert actionHandler.equalsFields == wizardEqualsFields
        assert actionHandler.hashCodeFields == wizardHashCodeFields
    }

    void fieldsFromWizardAreNotAssigned() {
        assert result == null
        assert actionHandler.equalsFields == null
        assert actionHandler.hashCodeFields == null
    }
}
