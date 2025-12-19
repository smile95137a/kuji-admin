package com.group.admin.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.entity.User;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 認證過濾器（通用版）
 * 支援後台 AdminUser 和前台 User 的認證
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final AdminUserMapper adminUserMapper;
	private final AdminUserRoleMapper adminUserRoleMapper;
	private final RoleMapper roleMapper;
	private final UserMapper userMapper;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, AdminUserMapper adminUserMapper,
			AdminUserRoleMapper adminUserRoleMapper, RoleMapper roleMapper, UserMapper userMapper) {
		this.jwtUtil = jwtUtil;
		this.adminUserMapper = adminUserMapper;
		this.adminUserRoleMapper = adminUserRoleMapper;
		this.roleMapper = roleMapper;
		this.userMapper = userMapper;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, 
									@NonNull HttpServletResponse response, 
									@NonNull FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			if (jwtUtil.validateToken(token)) {
				String username = jwtUtil.getUsername(token);

				List<SimpleGrantedAuthority> authorities = new ArrayList<>();

				// 1) 使用 Example 模式查詢是否為後台管理員
				AdminUserExample adminExample = new AdminUserExample();
				adminExample.createCriteria().andUsernameEqualTo(username);
				List<AdminUser> admins = adminUserMapper.selectByExample(adminExample);
				
				if (!admins.isEmpty()) {
					AdminUser admin = admins.get(0);
					// 使用 Example 模式查詢角色
					AdminUserRoleExample roleExample = new AdminUserRoleExample();
					roleExample.createCriteria().andAdminUserIdEqualTo(admin.getId());
					List<AdminUserRole> roles = adminUserRoleMapper.selectByExample(roleExample);
					
					if (roles != null) {
						authorities.addAll(roles.stream().map(r -> {
							Role role = roleMapper.selectByPrimaryKey(r.getRoleId());
							String name = role != null ? role.getName() : r.getRoleId();
							return new SimpleGrantedAuthority("ROLE_" + name);
						}).collect(Collectors.toList()));
					}

					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
							authorities);
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				} else {
					// 2) 使用 Example 模式查詢前台用戶
					UserExample userExample = new UserExample();
					userExample.createCriteria().andEmailEqualTo(username);
					List<User> users = userMapper.selectByExample(userExample);
					
					if (!users.isEmpty()) {
						authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
						UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username,
								null, authorities);
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}


