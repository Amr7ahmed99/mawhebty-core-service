package io.mawhebty.dtos;

import io.mawhebty.models.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindOrCreateUserDto {
    private User user;
    private Boolean isNew;
}
