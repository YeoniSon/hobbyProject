package com.example.dto.request.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostEditRequest {
    private Long CategoryId;
    private String Title;
    private String Content;
}
