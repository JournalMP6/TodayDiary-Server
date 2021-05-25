package com.mptsix.todaydiary.data.user

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.stream.Collectors

@Document("user")
data class User(
    @Id
    var id: ObjectId = ObjectId(),
    // For Logging-In
    var userId: String,
    var userPassword: String,

    // For Registration
    var userName: String,
    var userDateOfBirth: String,
    var userPasswordQuestion: String,
    var userPasswordAnswer: String,

    // For Internal Permission Setup
    var roles: Set<String> = setOf()
): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return roles.stream()
            .map { role: String? ->
                SimpleGrantedAuthority(
                    role
                )
            }
            .collect(Collectors.toList())
    }

    override fun getPassword() = userPassword
    override fun getUsername(): String? = userName
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}