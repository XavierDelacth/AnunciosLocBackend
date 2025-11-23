package AnunciosLocBackend.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_profile", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "profile_key", "profile_value_normalized"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "profile_key", nullable = false, length = 100)
    private String profileKey;

    @Column(name = "profile_value", nullable = false, length = 200)
    private String profileValue;

    @Column(name = "profile_value_normalized", nullable = false, length = 200)
    private String profileValueNormalized;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        Long thisUserId = this.user != null ? this.user.getId() : null;
        Long thatUserId = that.user != null ? that.user.getId() : null;
        return Objects.equals(profileKey, that.profileKey)
                && Objects.equals(profileValueNormalized, that.profileValueNormalized)
                && Objects.equals(thisUserId, thatUserId);
    }

    @Override
    public int hashCode() {
        Long userId = this.user != null ? this.user.getId() : null;
        return Objects.hash(profileKey, profileValueNormalized, userId);
    }
}
