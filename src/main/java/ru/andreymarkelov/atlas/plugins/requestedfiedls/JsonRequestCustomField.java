package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.templaterenderer.TemplateRenderer;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.field.SimpleHttpConfig;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.manager.RequestFieldDataManager;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.util.JsonHttpRunner;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class JsonRequestCustomField extends GenericTextCFType {
    private final RequestFieldDataManager requestFieldDataManager;
    private final TemplateRenderer renderer;

    public JsonRequestCustomField(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
            JiraAuthenticationContext jiraAuthenticationContext,
            RequestFieldDataManager requestFieldDataManager,
            TemplateRenderer renderer) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.requestFieldDataManager = requestFieldDataManager;
        this.renderer = renderer;
    }

    @Override
    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SimpleHttpConfig(renderer, requestFieldDataManager, false));
        return configurationItemTypes;
    }

    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField customField, FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, customField, fieldLayoutItem);
        if (issue == null) {
            return map;
        }

        FieldConfig fieldConfig = customField.getRelevantConfig(issue);
        if (fieldConfig != null) {
            JSONFieldData data = requestFieldDataManager.getJSONFieldData(fieldConfig);
            if (data != null) {
                JsonHttpRunner runner = new JsonHttpRunner(data, customField.getDefaultValue(issue));
                map.put("runner", runner);
            } else {
                map.put("notconfigured", Boolean.TRUE);
            }
        }

        return map;
    }
}