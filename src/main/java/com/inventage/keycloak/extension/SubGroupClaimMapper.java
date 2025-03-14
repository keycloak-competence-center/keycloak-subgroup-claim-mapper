package com.inventage.keycloak.extension;

import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.keycloak.protocol.oidc.mappers.ClaimsParameterWithValueIdTokenMapper.CLAIM_NAME;

/*
 * Accepts a group as argument and writes all the subgroups where the user is member into the claim value.
 */
public class SubGroupClaimMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    // ---- Statics

    private static final String MAPPER_ID = "oidc-sub-group-claim-mapper";
    private static final String GROUP_PROPERTY_NAME = "config.group";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, SubGroupClaimMapper.class);

        // Inclusion filter prefix
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(GROUP_PROPERTY_NAME);
        property.setLabel("Group");
        property.setType(ProviderConfigProperty.GROUP_TYPE);
        property.setHelpText("The group which sub groups should be used as claim value.");
        configProperties.add(property);
    }


    // ---- Methods

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Sub Group Claim Mapper";
    }

    @Override
    public String getHelpText() {
        return "Takes a parent group as argument and adds their child groups as claim values.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }

    @Override
    protected void setClaim(final IDToken token,
                            final ProtocolMapperModel mappingModel,
                            final UserSessionModel userSession,
                            final KeycloakSession keycloakSession,
                            final ClientSessionContext clientSessionCtx) {

        final UserModel user = userSession.getUser();
        final String claimName = mappingModel.getConfig().get(CLAIM_NAME);
        final String configuredGroupName = mappingModel.getConfig().get(GROUP_PROPERTY_NAME);
         if (configuredGroupName == null) {
            return;
        }

        final Optional<GroupModel> optionalGroupModel = keycloakSession.groups()
            .getGroupsStream(userSession.getRealm()).filter(groupModel -> fullPathOf(groupModel).equals(configuredGroupName)).findFirst();

        if (optionalGroupModel.isEmpty()) {
            token.getOtherClaims().put(claimName, List.of());
            return;
        }

        final GroupModel parentGroupModel = optionalGroupModel.get();
        final Set<GroupModel> subGroups = parentGroupModel.getSubGroupsStream().collect(toSet());
        final List<GroupModel> userGroups = user.getGroupsStream().filter(subGroups::contains).toList();
        final List<String> userGroupNames = userGroups.stream().map(GroupModel::getName).collect(toList());

        token.getOtherClaims().put(claimName, userGroupNames);
    }

    private String fullPathOf(GroupModel groupModel) {
        if (groupModel.getParent() != null) {
            return fullPathOf(groupModel.getParent()) + "/" + groupModel.getName();
        }
        else {
            return "/" + groupModel.getName();
        }
    }

}
