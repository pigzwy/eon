package com.eon.auth.support.security;

import com.eon.auth.support.user.UserAuthorityService;
import com.eon.auth.support.user.UserAuthorityService.UserAuthoritySnapshot;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JdbcTemplate 驱动的 UserDetailsService 实现。
 */
public class JdbcUserDetailsServiceAdapter implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;
    private final UserAuthorityService userAuthorityService;

    public JdbcUserDetailsServiceAdapter(JdbcTemplate jdbcTemplate, UserAuthorityService userAuthorityService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userAuthorityService = userAuthorityService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRecord record = fetchUser(username);
        UserAuthoritySnapshot snapshot = userAuthorityService.loadAuthoritySnapshot(username);
        return User.withUsername(username)
                .password(record.passwordHash())
                .authorities(toAuthorities(snapshot))
                .accountLocked(!record.active())
                .disabled(!record.active())
                .accountExpired(false)
                .credentialsExpired(false)
                .build();
    }

    private UserRecord fetchUser(String username) {
        try {
            return jdbcTemplate.queryForObject(QUERY_USER_SQL, new UserRowMapper(), username);
        } catch (EmptyResultDataAccessException ex) {
            throw new UsernameNotFoundException("用户不存在或已被禁用");
        }
    }

    private Collection<? extends GrantedAuthority> toAuthorities(UserAuthoritySnapshot snapshot) {
        Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
        if (snapshot.hasUser()) {
            snapshot.roles().forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            snapshot.permissions().forEach(permission -> grantedAuthorities.add(new SimpleGrantedAuthority(permission)));
        }
        if (grantedAuthorities.isEmpty()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        }
        return grantedAuthorities;
    }

    private static final String QUERY_USER_SQL = "SELECT id, password_hash, is_active FROM users WHERE username = ? ORDER BY id LIMIT 1";

    private record UserRecord(Long id, String passwordHash, boolean active) {
    }

    private static class UserRowMapper implements RowMapper<UserRecord> {
        @Override
        public UserRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String passwordHash = rs.getString("password_hash");
            boolean active = rs.getBoolean("is_active");
            if (!active) {
                throw new UsernameNotFoundException("账号已被禁用");
            }
            return new UserRecord(id, passwordHash, active);
        }
    }
}
