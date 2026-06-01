package dev.christopherlang.paytrace.common;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import dev.christopherlang.paytrace.features.user.domain.UserSyncService;

@Component
public class JwtSyncConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserSyncService userSyncService;
    private final Set<String> synchronizedUsers = ConcurrentHashMap.newKeySet();

    public JwtSyncConverter(UserSyncService userSyncService) {
        this.userSyncService = userSyncService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String userId = jwt.getSubject();

        if (!synchronizedUsers.contains(userId)) {
            userSyncService.syncUser(jwt);
            synchronizedUsers.add(userId);
        }

        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        return new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt));
    }

}
