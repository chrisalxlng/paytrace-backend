package dev.christopherlang.paytrace.features.user.data;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "is_demo")
    private boolean isDemo;

}
