package nl.ivonet.idea.plugins.wizard;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.generation.GenerateEqualsHelper;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.refactoring.classMembers.MemberInfoChange;
import com.intellij.refactoring.classMembers.MemberInfoModel;
import com.intellij.refactoring.classMembers.MemberInfoTooltipManager;
import com.intellij.refactoring.ui.MemberSelectionPanel;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GenerateEqualsHashCodeApacheCommonsWizard extends AbstractWizard {
    private final PsiClass myClass;

    private final MemberSelectionPanel myEqualsPanel;
    private final MemberSelectionPanel myHashCodePanel;
    private final HashMap myFieldsToHashCode;

    private final int myTestBoxedStep;
    private final int myEqualsStepCode;

    private final List<MemberInfo> myClassFields;
    private static final MyMemberInfoFilter MEMBER_INFO_FILTER = new MyMemberInfoFilter();

    public GenerateEqualsHashCodeApacheCommonsWizard(final Project project, final PsiClass aClass,
                                                     final boolean needEquals, final boolean needHashCode) {
        super(CodeInsightBundle.message("generate.equals.hashcode.wizard.title"), project);
        myClass = aClass;

        myClassFields = MemberInfo.extractClassMembers(myClass, MEMBER_INFO_FILTER, false);
        for (final MemberInfo myClassField : myClassFields) {
            myClassField.setChecked(true);
        }
        int testBoxedStep = 0;
        if (needEquals) {
            myEqualsPanel = new MemberSelectionPanel(
                    CodeInsightBundle.message("generate.equals.hashcode.equals.fields.chooser.title"),
                    myClassFields, null);
            myEqualsPanel.getTable().setMemberInfoModel(new EqualsMemberInfoModel());
            testBoxedStep += 1;
        } else {
            myEqualsPanel = null;
        }
        if (needHashCode) {
            final List<MemberInfo> hashCodeMemberInfos;
            if (needEquals) {
                myFieldsToHashCode = createFieldToMemberInfoMap(true);
                hashCodeMemberInfos = Collections.emptyList();
            } else {
                hashCodeMemberInfos = myClassFields;
                myFieldsToHashCode = null;
            }
            myHashCodePanel = new MemberSelectionPanel(
                    CodeInsightBundle.message("generate.equals.hashcode.hashcode.fields.chooser.title"),
                    hashCodeMemberInfos, null);
            myHashCodePanel.getTable().setMemberInfoModel(new HashCodeMemberInfoModel());
            if (needEquals) {
                updateHashCodeMemberInfos(myClassFields);
            }
            testBoxedStep++;
        } else {
            myHashCodePanel = null;
            myFieldsToHashCode = null;
        }
        myTestBoxedStep = testBoxedStep;

        final MyTableModelListener listener = new MyTableModelListener();
        if (myEqualsPanel != null) {
            myEqualsPanel.getTable().getModel().addTableModelListener(listener);
            addStep(new MyStep(myEqualsPanel));
            myEqualsStepCode = 0;
        } else {
            myEqualsStepCode = -1;
        }

        if (myHashCodePanel != null) {
            myHashCodePanel.getTable().getModel().addTableModelListener(listener);
            addStep(new MyStep(myHashCodePanel));
        }

        init();
        updateStatus();
    }

    public PsiField[] getEqualsFields() {
        if (myEqualsPanel != null) {
            return memberInfosToFields(myEqualsPanel.getTable().getSelectedMemberInfos());
        } else {
            return null;
        }
    }

    public PsiField[] getHashCodeFields() {
        if (myHashCodePanel != null) {
            return memberInfosToFields(myHashCodePanel.getTable().getSelectedMemberInfos());
        } else {
            return null;
        }
    }

    private static PsiField[] memberInfosToFields(final Collection<MemberInfo> infos) {
        final ArrayList<PsiField> list = new ArrayList<PsiField>();
        for (final MemberInfo info : infos) {
            list.add((PsiField) info.getMember());
        }
        return list.toArray(new PsiField[list.size()]);
    }

    protected void doNextAction() {
        if (getCurrentStep() == myEqualsStepCode && myEqualsPanel != null) {
            equalsFieldsSelected();
        }

        super.doNextAction();
        updateStatus();
    }

    protected void updateStep() {
        super.updateStep();
        final Component stepComponent = getCurrentStepComponent();
        if (stepComponent instanceof MemberSelectionPanel) {
            ((MemberSelectionPanel) stepComponent).getTable().requestFocus();
        }
    }

    protected String getHelpID() {
        return "editing.altInsert.equals";
    }

    private void equalsFieldsSelected() {
        final Collection<MemberInfo> selectedMemberInfos = myEqualsPanel.getTable().getSelectedMemberInfos();
        updateHashCodeMemberInfos(selectedMemberInfos);
    }

    @Override
    protected void doOKAction() {
        if (myEqualsPanel != null) {
            equalsFieldsSelected();
        }
        super.doOKAction();
    }

    private HashMap<PsiElement, MemberInfo> createFieldToMemberInfoMap(final boolean checkedByDefault) {
        final Collection<MemberInfo> memberInfos = MemberInfo
                .extractClassMembers(myClass, MEMBER_INFO_FILTER, false);
        final HashMap<PsiElement, MemberInfo> result = new HashMap<PsiElement, MemberInfo>();
        for (final MemberInfo memberInfo : memberInfos) {
            memberInfo.setChecked(checkedByDefault);
            result.put(memberInfo.getMember(), memberInfo);
        }
        return result;
    }

    private void updateHashCodeMemberInfos(final Collection<MemberInfo> equalsMemberInfos) {
        if (myHashCodePanel == null) {
            return;
        }
        final List<MemberInfo> hashCodeFields = new ArrayList<MemberInfo>();

        for (final MemberInfo equalsMemberInfo : equalsMemberInfos) {
            hashCodeFields.add((MemberInfo) myFieldsToHashCode.get(equalsMemberInfo.getMember()));
        }

        myHashCodePanel.getTable().setMemberInfos(hashCodeFields);
    }

    private void updateStatus() {
        if (getCurrentStep() == myEqualsStepCode) {
            getNextButton().setEnabled(anyChecked(myClassFields));
        } else {
            getNextButton().setEnabled(anyChecked(myHashCodePanel.getTable().getSelectedMemberInfos()));
        }
        if (getNextButton().isEnabled()) {
            getRootPane().setDefaultButton(getNextButton());
        }
    }

    private boolean anyChecked(final Collection<MemberInfo> memberInfos) {
        boolean anyChecked = false;
        for (final MemberInfo classField : memberInfos) {
            if (classField.isChecked()) {
                anyChecked = true;
                break;
            }
        }
        return anyChecked;
    }

    public JComponent getPreferredFocusedComponent() {
        final Component stepComponent = getCurrentStepComponent();
        if (stepComponent instanceof MemberSelectionPanel) {
            return ((MemberSelectionPanel) stepComponent).getTable();
        } else {
            return null;
        }
    }

    private class MyTableModelListener implements TableModelListener {
        public void tableChanged(final TableModelEvent e) {
            updateStatus();
        }
    }

    private static class MyStep extends StepAdapter {
        final MemberSelectionPanel myPanel;

        public MyStep(final MemberSelectionPanel panel) {
            myPanel = panel;
        }

        public Icon getIcon() {
            return null;
        }

        public JComponent getComponent() {
            return myPanel;
        }

    }

    private static class MyMemberInfoFilter implements MemberInfoBase.Filter<PsiMember> {
        public boolean includeMember(final PsiMember element) {
            return element instanceof PsiField && !element.hasModifierProperty(PsiModifier.STATIC);
        }
    }

    private static class EqualsMemberInfoModel implements MemberInfoModel<PsiMember, MemberInfo> {
        MemberInfoTooltipManager<PsiMember, MemberInfo> myTooltipManager = new MemberInfoTooltipManager<PsiMember,
                MemberInfo>(
                new MemberInfoTooltipManager.TooltipProvider<PsiMember, MemberInfo>() {
                    public String getTooltip(final MemberInfo memberInfo) {
                        if (checkForProblems(memberInfo) == OK) {
                            return null;
                        }
                        if (!(memberInfo.getMember() instanceof PsiField)) {
                            return CodeInsightBundle.message("generate.equals.hashcode.internal.error");
                        }
                        final PsiType type = ((PsiField) memberInfo.getMember()).getType();
                        if (GenerateEqualsHelper.isNestedArray(type)) {
                            return CodeInsightBundle.message(
                                    "generate.equals.warning.equals.for.nested.arrays.not.supported");
                        }
                        if (GenerateEqualsHelper.isArrayOfObjects(type)) {
                            return CodeInsightBundle
                                    .message("generate.equals.warning.generated.equals.could.be.incorrect");
                        }
                        return null;
                    }
                });

        public boolean isMemberEnabled(final MemberInfo member) {
            if (!(member.getMember() instanceof PsiField)) {
                return false;
            }
            final PsiType type = ((PsiField) member.getMember()).getType();
            return !GenerateEqualsHelper.isNestedArray(type);
        }

        public boolean isCheckedWhenDisabled(final MemberInfo member) {
            return false;
        }

        public boolean isAbstractEnabled(final MemberInfo member) {
            return false;
        }

        public boolean isAbstractWhenDisabled(final MemberInfo member) {
            return false;
        }

        public Boolean isFixedAbstract(final MemberInfo member) {
            return null;
        }

        public int checkForProblems(@NotNull final MemberInfo member) {
            if (!(member.getMember() instanceof PsiField)) {
                return ERROR;
            }
            final PsiType type = ((PsiField) member.getMember()).getType();
            if (GenerateEqualsHelper.isNestedArray(type)) {
                return ERROR;
            }
            if (GenerateEqualsHelper.isArrayOfObjects(type)) {
                return WARNING;
            }
            return OK;
        }

        public void memberInfoChanged(final MemberInfoChange<PsiMember, MemberInfo> event) {
        }

        public String getTooltipText(final MemberInfo member) {
            return myTooltipManager.getTooltip(member);
        }
    }

    private static class HashCodeMemberInfoModel implements MemberInfoModel<PsiMember, MemberInfo> {
        private final MemberInfoTooltipManager<PsiMember, MemberInfo> myTooltipManager = new
                MemberInfoTooltipManager<PsiMember, MemberInfo>(
                new MemberInfoTooltipManager.TooltipProvider<PsiMember, MemberInfo>() {
                    public String getTooltip(final MemberInfo memberInfo) {
                        if (isMemberEnabled(memberInfo)) {
                            return null;
                        }
                        if (!(memberInfo.getMember() instanceof PsiField)) {
                            return CodeInsightBundle.message("generate.equals.hashcode.internal.error");
                        }
                        final PsiType type = ((PsiField) memberInfo.getMember()).getType();
                        if (!(type instanceof PsiArrayType)) {
                            return null;
                        }
                        return CodeInsightBundle.message(
                                "generate.equals.hashcode.warning.hashcode.for.arrays.is.not.supported");
                    }
                });

        public boolean isMemberEnabled(final MemberInfo member) {
            final PsiMember psiMember = member.getMember();
            return psiMember instanceof PsiField;
        }

        public boolean isCheckedWhenDisabled(final MemberInfo member) {
            return false;
        }

        public boolean isAbstractEnabled(final MemberInfo member) {
            return false;
        }

        public boolean isAbstractWhenDisabled(final MemberInfo member) {
            return false;
        }

        public Boolean isFixedAbstract(final MemberInfo member) {
            return null;
        }

        public int checkForProblems(@NotNull final MemberInfo member) {
            return OK;
        }

        public void memberInfoChanged(final MemberInfoChange<PsiMember, MemberInfo> event) {
        }

        public String getTooltipText(final MemberInfo member) {
            return myTooltipManager.getTooltip(member);
        }
    }
}
