package com.example.todolist.user.persistent.entity

import com.example.todolist.todo.persistent.entity.TodoEntity
import com.example.todolist.user.controller.api.data.SignUpUserRequest
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.GenerationType.IDENTITY
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.DynamicUpdate
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@DynamicUpdate
@Entity
@Table(name = "user")
class UserEntity(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", unique = true)
    var userId: String,

    val password: String,

    val name: String,

    @Column(name = "nick_name", unique = true)
    var nickName: String,

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "user_id")
    val todoEntities: List<TodoEntity> = listOf(),

    val createdAt: LocalDateTime = now(),

    var updatedAt: LocalDateTime? = null,

    var deletedAt: LocalDateTime? = null,
) {
    @Transient
    val isLive = deletedAt == null

    @Transient
    val isLeave = deletedAt != null

    fun validPassword(password: String, passwordEncoder: PasswordEncoder) {
        require(passwordEncoder.matches(password, this.password)) { "비밀번호가 일치하지 않습니다" }
    }

    fun leave() {
        userId = id.toString()
        nickName = id.toString()
        deletedAt = now()
    }

    companion object {
        fun of(signUpUserRequest: SignUpUserRequest, passwordEncoder: PasswordEncoder): UserEntity {
            return with(signUpUserRequest) {
                require(StringUtils.equals(password, rePassword)) { "비밀번호가 일치하지 않습니다." }
                require(!StringUtils.isNumeric(userId)) { "아이디는 영문+숫자 조합이여야 합니다." }
                require(!StringUtils.isNumeric(nickName)) { "닉네임은 영문+숫자 조합이여야 합니다." }

                UserEntity(
                    userId = userId,
                    password = passwordEncoder.encode(password),
                    name = name,
                    nickName = nickName,
                )
            }
        }
    }
}
