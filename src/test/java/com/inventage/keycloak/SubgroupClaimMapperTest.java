package com.inventage.keycloak;

import org.junit.jupiter.api.Test;
import org.keycloak.models.*;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link SubgroupClaimMapper}.
 */
public class SubgroupClaimMapperTest {

    // ---- Statics

    private static final String CLAIM = "mySampleClaimName";


    // ---- Fields

    private final SubgroupClaimMapper subGroupClaimMapper = new SubgroupClaimMapper();


    // ---- Methods

    @Test
    public void shouldAddEmptyClaimIfNoGroupMembership() {
        // Arrange
        final ProtocolMapperModel mappingModel = createMappingModelWithGroupName("myGroup");
        final KeycloakSession keycloakSession = createKeycloakSessionWithGroups(List.of());
        final UserSessionModel userSession = givenUserSession(List.of());

        // Act
        final AccessToken userInfo = subGroupClaimMapper.transformUserInfoToken(new AccessToken(), mappingModel, keycloakSession, userSession, null);

        // Assert
        assertThat(userInfo.getOtherClaims().get(CLAIM)).isEqualTo(List.of());
    }

    @Test
    public void shouldAddListOfGroups() {
        // Arrange
        Group parent = new Group("parent");
        Group child1 = new Group("child1");
        Group child2 = new Group("child2");
        Group child3 = new Group("child3");
        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(child3);


        final ProtocolMapperModel mappingModel = createMappingModelWithGroupName("/parent");
        final KeycloakSession keycloakSession = createKeycloakSessionWithGroups(List.of(parent));
        final UserSessionModel userSession = givenUserSession(List.of(child1, child2));

        // Act
        final AccessToken userInfo = subGroupClaimMapper.transformUserInfoToken(new AccessToken(), mappingModel, keycloakSession, userSession, null);

        // Assert
        assertThat(userInfo.getOtherClaims().get(CLAIM)).isEqualTo(List.of("child1", "child2"));
    }

    private ProtocolMapperModel createMappingModelWithGroupName(String groupName) {
        final Map<String, String> configuration = new HashMap<>();
        configuration.put("config.group", groupName);
        configuration.put("claim.name", CLAIM);
        configuration.put(INCLUDE_IN_USERINFO, "true");

        final ProtocolMapperModel mappingModel = new ProtocolMapperModel();
        mappingModel.setConfig(configuration);
        return mappingModel;
    }

    private KeycloakSession createKeycloakSessionWithGroups(List<GroupModel> groups) {
        GroupProvider groupProvider = Mockito.mock(GroupProvider.class);
        when(groupProvider.getGroupsStream(any())).thenReturn(groups.stream());

        KeycloakSession keycloakSession = Mockito.mock(KeycloakSession.class);
        when(keycloakSession.groups()).thenReturn(groupProvider);

        return keycloakSession;
    }

    private UserSessionModel givenUserSession(List<GroupModel> groups) {
        UserModel user = Mockito.mock(UserModel.class);
        when(user.getGroupsStream()).thenReturn(groups.stream());
        UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
        when(userSession.getUser()).thenReturn(user);
        return userSession;
    }

    // ---- Inner classes

    private static class Group implements GroupModel {

        private String name;
        private GroupModel parent;
        private final List<GroupModel> children = new ArrayList<>();

        Group(String name) {
            this.name = name;
        }

        @Override
        public String getId() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void setSingleAttribute(String name, String value) {

        }

        @Override
        public void setAttribute(String name, List<String> values) {

        }

        @Override
        public void removeAttribute(String name) {

        }

        @Override
        public String getFirstAttribute(String name) {
            return null;
        }

        @Override
        public Stream<String> getAttributeStream(String name) {
            return null;
        }

        @Override
        public Map<String, List<String>> getAttributes() {
            return null;
        }

        @Override
        public GroupModel getParent() {
            return parent;
        }

        @Override
        public String getParentId() {
            return parent.getId();
        }

        @Override
        public Stream<GroupModel> getSubGroupsStream() {
            return children.stream();
        }

        @Override
        public void setParent(GroupModel group) {
            parent = group;
        }

        @Override
        public void addChild(GroupModel subGroup) {
            children.add(subGroup);
            subGroup.setParent(this);
        }

        @Override
        public void removeChild(GroupModel subGroup) {
            children.remove(subGroup);
            subGroup.setParent(null);
        }

        @Override
        public Stream<RoleModel> getRealmRoleMappingsStream() {
            return null;
        }

        @Override
        public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
            return null;
        }

        @Override
        public boolean hasRole(RoleModel role) {
            return false;
        }

        @Override
        public void grantRole(RoleModel role) {

        }

        @Override
        public Stream<RoleModel> getRoleMappingsStream() {
            return null;
        }

        @Override
        public void deleteRoleMapping(RoleModel role) {

        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Group && getName() != null) {
                return getName().equals(((Group) obj).getName());
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            if (getName() != null) {
                return getName().hashCode();
            }
            return super.hashCode();
        }
    }


}
